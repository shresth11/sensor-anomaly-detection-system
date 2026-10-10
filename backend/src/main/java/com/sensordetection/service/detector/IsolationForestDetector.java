package com.sensordetection.service.detector;

import com.sensordetection.model.AnomalyResult;
import com.sensordetection.model.DetectorType;
import com.sensordetection.model.SensorReading;
import com.sensordetection.model.SensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Unsupervised Machine Learning Anomaly Detector using Isolation Forest (Liu, Ting, Zhou - 2008).
 *
 * <p>Machine Learning Principles:</p>
 * <ol>
 *   <li><b>Non-parametric:</b> Does not assume any underlying probability distribution
 *       (unlike Z-score which assumes a Gaussian bell curve).</li>
 *   <li><b>Tree Isolation:</b> Anomalies are "few and different". In recursive binary partition
 *       trees, anomalies require significantly fewer random splits to isolate (shorter path lengths)
 *       compared to dense clusters of normal data.</li>
 *   <li><b>Ensemble Averaging:</b> An ensemble of Isolation Trees (iTrees) is constructed to
 *       smooth out variance in path lengths.</li>
 * </ol>
 *
 * <p>Mathematical Formulation:</p>
 * <ul>
 *   <li>Average path length of unsuccessful search in Binary Search Tree (BST):
 *       c(n) = 2 * (ln(n - 1) + 0.5772156649) - (2 * (n - 1) / n) for n &gt; 2</li>
 *   <li>Anomaly Score: s(x, n) = 2^(-E(h(x)) / c(n))</li>
 *   <li>Interpretation:
 *       <ul>
 *         <li>s → 1: Definitely an anomaly (short path length).</li>
 *         <li>s ≈ 0.5: Normal observation (average path length).</li>
 *         <li>s → 0: Safe cluster core (long path length).</li>
 *       </ul>
 *   </li>
 * </ul>
 */
@Component
public class IsolationForestDetector implements AnomalyDetector {

    private static final Logger log = LoggerFactory.getLogger(IsolationForestDetector.class);

    // Standard Isolation Forest hyper-parameters
    private static final int DEFAULT_NUM_TREES = 100;
    private static final int SUBSAMPLE_SIZE = 256;
    private static final double DEFAULT_ANOMALY_THRESHOLD = 0.60;
    private static final double EULER_MASCHERONI_CONSTANT = 0.5772156649;

    @Override
    public DetectorType getType() {
        return DetectorType.ISOLATION_FOREST;
    }

    @Override
    public List<AnomalyResult> detect(List<SensorReading> readings, double threshold) {
        if (readings == null || readings.isEmpty()) {
            return Collections.emptyList();
        }

        // Configurable decision threshold: values > threshold are flagged as anomalies.
        // If user passes a standard z-threshold (e.g. 2.0 or 3.0), we normalize appropriately or use threshold in [0.5, 0.9] range.
        double activeThreshold = (threshold >= 0.1 && threshold <= 0.95) ? threshold : DEFAULT_ANOMALY_THRESHOLD;

        // Group by sensor type so that temperature, pressure, and vibration are modeled individually
        Map<SensorType, List<SensorReading>> groupedByType = readings.stream()
                .collect(Collectors.groupingBy(SensorReading::getSensorType));

        Map<Integer, AnomalyResult> resultsByIndex = new HashMap<>();
        Random random = new Random(42); // Seeded for deterministic test reproducibility

        for (Map.Entry<SensorType, List<SensorReading>> entry : groupedByType.entrySet()) {
            SensorType sensorType = entry.getKey();
            List<SensorReading> groupReadings = entry.getValue();

            List<Double> validValues = groupReadings.stream()
                    .map(SensorReading::getValue)
                    .filter(v -> !Double.isNaN(v) && Double.isFinite(v))
                    .collect(Collectors.toList());

            if (validValues.isEmpty() || validValues.size() < 3) {
                // Not enough samples to construct an Isolation Forest ensemble reliably
                for (SensorReading r : groupReadings) {
                    resultsByIndex.put(r.getOriginalIndex(), new AnomalyResult(
                            r.getTimestamp(), r.getSensorType(), r.getValue(),
                            0.5, false, "NORMAL",
                            "Insufficient sample size for Isolation Forest (n < 3)", r.getOriginalIndex()
                    ));
                }
                continue;
            }

            int n = validValues.size();
            double c_n = averagePathLengthBST(n);

            // Check if all values are identical
            double min = Collections.min(validValues);
            double max = Collections.max(validValues);
            if (Double.compare(min, max) == 0) {
                for (SensorReading r : groupReadings) {
                    resultsByIndex.put(r.getOriginalIndex(), new AnomalyResult(
                            r.getTimestamp(), r.getSensorType(), r.getValue(),
                            0.0, false, "NORMAL",
                            "Uniform baseline (all readings identical at " + round(min, 2) + ")", r.getOriginalIndex()
                    ));
                }
                continue;
            }

            // Train the Forest (Ensemble of Isolation Trees)
            int numTrees = DEFAULT_NUM_TREES;
            int maxDepth = (int) Math.ceil(Math.log(Math.min(n, SUBSAMPLE_SIZE)) / Math.log(2));
            List<IsolationTreeNode> forest = new ArrayList<>(numTrees);

            for (int t = 0; t < numTrees; t++) {
                List<Double> sample = drawSubsample(validValues, Math.min(n, SUBSAMPLE_SIZE), random);
                IsolationTreeNode root = buildTree(sample, 0, maxDepth, random);
                forest.add(root);
            }

            log.debug("Isolation Forest trained for {}: {} trees, maxDepth={}", sensorType, numTrees, maxDepth);

            // Compute Anomaly Score s(x, n) for each reading
            for (SensorReading r : groupReadings) {
                double val = r.getValue();

                if (Double.isNaN(val) || !Double.isFinite(val)) {
                    resultsByIndex.put(r.getOriginalIndex(), new AnomalyResult(
                            r.getTimestamp(), r.getSensorType(), val,
                            1.0, true, "ANOMALY", "Invalid non-finite reading", r.getOriginalIndex()
                    ));
                    continue;
                }

                // Calculate average path length across all trees
                double totalPathLength = 0.0;
                for (IsolationTreeNode tree : forest) {
                    totalPathLength += computePathLength(r.getValue(), tree, 0);
                }
                double expectedPathLength = totalPathLength / numTrees;

                // Anomaly Score s(x, n) = 2^(-E(h(x)) / c(n))
                double score = Math.pow(2.0, - (expectedPathLength / c_n));
                double roundedScore = round(score, 4);

                boolean isAnomaly = roundedScore >= activeThreshold;
                String status = isAnomaly ? "ANOMALY" : "NORMAL";

                String reason = isAnomaly
                        ? String.format("iForest score: %.4f ≥ threshold %.2f (Avg tree depth: %.2f vs BST avg: %.2f)",
                        roundedScore, activeThreshold, expectedPathLength, c_n)
                        : String.format("iForest score: %.4f < threshold %.2f (Nominal cluster depth: %.2f)",
                        roundedScore, activeThreshold, expectedPathLength);

                resultsByIndex.put(r.getOriginalIndex(), new AnomalyResult(
                        r.getTimestamp(), r.getSensorType(), val,
                        roundedScore, isAnomaly, status, reason, r.getOriginalIndex()
                ));
            }
        }

        // Return results maintaining original sequence
        List<AnomalyResult> orderedResults = new ArrayList<>(readings.size());
        for (SensorReading reading : readings) {
            AnomalyResult res = resultsByIndex.get(reading.getOriginalIndex());
            if (res != null) {
                orderedResults.add(res);
            }
        }
        return orderedResults;
    }

    /**
     * Average path length of unsuccessful search in a Binary Search Tree (BST) c(n).
     * Used to normalize path lengths.
     */
    public static double averagePathLengthBST(int n) {
        if (n <= 1) {
            return 0.0;
        }
        if (n == 2) {
            return 1.0;
        }
        double h_n_minus_1 = Math.log(n - 1) + EULER_MASCHERONI_CONSTANT;
        return 2.0 * h_n_minus_1 - (2.0 * (n - 1) / n);
    }

    /**
     * Recursively builds an Isolation Tree node.
     */
    private IsolationTreeNode buildTree(List<Double> data, int currentDepth, int maxDepth, Random random) {
        if (data.size() <= 1 || currentDepth >= maxDepth) {
            return new IsolationTreeNode(data.size());
        }

        double min = Collections.min(data);
        double max = Collections.max(data);

        // If all samples in this subset have identical values, splitting is not possible
        if (Double.compare(min, max) == 0) {
            return new IsolationTreeNode(data.size());
        }

        // Random split point between min and max
        double splitPoint = min + random.nextDouble() * (max - min);

        List<Double> left = new ArrayList<>();
        List<Double> right = new ArrayList<>();
        for (Double val : data) {
            if (val < splitPoint) {
                left.add(val);
            } else {
                right.add(val);
            }
        }

        // Prevent infinite recursion if all elements fall to one side
        if (left.isEmpty() || right.isEmpty()) {
            return new IsolationTreeNode(data.size());
        }

        IsolationTreeNode leftChild = buildTree(left, currentDepth + 1, maxDepth, random);
        IsolationTreeNode rightChild = buildTree(right, currentDepth + 1, maxDepth, random);
        return new IsolationTreeNode(splitPoint, leftChild, rightChild);
    }

    /**
     * Traverses the tree to find path length h(x) for instance x.
     */
    private double computePathLength(double x, IsolationTreeNode node, int currentDepth) {
        if (node.isLeaf()) {
            return currentDepth + averagePathLengthBST(node.getSize());
        }
        if (x < node.getSplitPoint()) {
            return computePathLength(x, node.getLeft(), currentDepth + 1);
        } else {
            return computePathLength(x, node.getRight(), currentDepth + 1);
        }
    }

    private List<Double> drawSubsample(List<Double> data, int sampleSize, Random random) {
        List<Double> copy = new ArrayList<>(data);
        Collections.shuffle(copy, random);
        return copy.subList(0, Math.min(sampleSize, copy.size()));
    }

    private static double round(double value, int places) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        return BigDecimal.valueOf(value)
                .setScale(places, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Binary node in an Isolation Tree.
     */
    private static class IsolationTreeNode {
        private final boolean isLeaf;
        private final double splitPoint;
        private final int size;
        private final IsolationTreeNode left;
        private final IsolationTreeNode right;

        // Leaf constructor
        public IsolationTreeNode(int size) {
            this.isLeaf = true;
            this.splitPoint = 0.0;
            this.size = size;
            this.left = null;
            this.right = null;
        }

        // Internal split node constructor
        public IsolationTreeNode(double splitPoint, IsolationTreeNode left, IsolationTreeNode right) {
            this.isLeaf = false;
            this.splitPoint = splitPoint;
            this.size = 0;
            this.left = left;
            this.right = right;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        public double getSplitPoint() {
            return splitPoint;
        }

        public int getSize() {
            return size;
        }

        public IsolationTreeNode getLeft() {
            return left;
        }

        public IsolationTreeNode getRight() {
            return right;
        }
    }
}
