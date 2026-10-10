package com.sensordetection.service.detector;

import com.sensordetection.model.AnomalyResult;
import com.sensordetection.model.DetectorType;
import com.sensordetection.model.SensorReading;

import java.util.List;

/**
 * Common contract for all anomaly detection strategies.
 * Design pattern: Strategy Pattern - allows seamless switching between
 * Statistical Z-Score baseline and Unsupervised Machine Learning (Isolation Forest).
 */
public interface AnomalyDetector {

    /**
     * Executes the detection algorithm across the provided sensor readings.
     *
     * @param readings  List of incoming telemetry readings.
     * @param threshold Configurable sensitivity threshold.
     * @return List of anomaly evaluation results preserving original order.
     */
    List<AnomalyResult> detect(List<SensorReading> readings, double threshold);

    /**
     * Returns the detector type identifier.
     */
    DetectorType getType();
}
