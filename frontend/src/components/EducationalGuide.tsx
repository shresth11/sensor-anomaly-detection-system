import React, { useState } from 'react';
import { BookOpen, HelpCircle, ShieldAlert, CheckCircle2 } from 'lucide-react';

export const EducationalGuide: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'statistical' | 'machineLearning' | 'architecture' | 'interview'>('statistical');

  return (
    <div className="guide-card">
      <div className="guide-header">
        <div className="guide-title-group">
          <BookOpen size={22} className="guide-icon" />
          <div>
            <h3 className="guide-title">Engineering Knowledge & Mentorship Guide</h3>
            <p className="guide-subtitle">
              Deep-dive explanation of algorithms, design patterns, and interview talking points
            </p>
          </div>
        </div>

        <div className="guide-tabs">
          <button
            className={`guide-tab-btn ${activeTab === 'statistical' ? 'active' : ''}`}
            onClick={() => setActiveTab('statistical')}
          >
            1. Statistical Baseline (Z-Score)
          </button>
          <button
            className={`guide-tab-btn ${activeTab === 'machineLearning' ? 'active' : ''}`}
            onClick={() => setActiveTab('machineLearning')}
          >
            2. Isolation Forest (ML)
          </button>
          <button
            className={`guide-tab-btn ${activeTab === 'architecture' ? 'active' : ''}`}
            onClick={() => setActiveTab('architecture')}
          >
            3. Full-Stack Architecture
          </button>
          <button
            className={`guide-tab-btn ${activeTab === 'interview' ? 'active' : ''}`}
            onClick={() => setActiveTab('interview')}
          >
            4. DRDO Interview Tips
          </button>
        </div>
      </div>

      <div className="guide-content">
        {/* TAB 1: STATISTICAL Z-SCORE */}
        {activeTab === 'statistical' && (
          <div className="guide-section">
            <h4 className="section-title">Parametric Statistical Anomaly Detection (Z-Score)</h4>
            <p>
              In telemetry monitoring, a <strong>Z-score</strong> measures how many standard deviations
              an individual sensor observation is from the distribution mean.
            </p>

            <div className="math-box">
              <div className="formula-item">
                <span className="formula-name">Mean (μ):</span>
                <code>μ = (1 / N) * ∑(x_i)</code>
              </div>
              <div className="formula-item">
                <span className="formula-name">Population Standard Deviation (σ):</span>
                <code>σ = √[ (1 / N) * ∑(x_i - μ)² ]</code>
              </div>
              <div className="formula-item">
                <span className="formula-name">Z-Score (z):</span>
                <code>z = (x_i - μ) / σ</code>
              </div>
              <div className="formula-item">
                <span className="formula-name">Decision Rule:</span>
                <code>Flag as ANOMALY if |z| &gt; threshold (default 2.0σ)</code>
              </div>
            </div>

            <div className="callout callout-warning">
              <ShieldAlert size={20} className="callout-icon" />
              <div>
                <strong>Important Interview Distinction (Not AI/ML):</strong>
                <p>
                  A threshold rule is a <em>statistical heuristic</em>, <strong>not</strong> an AI model.
                  Moreover, extreme outlier values heavily distort both the mean (μ) and standard deviation (σ).
                  If a temperature sensor produces a reading of 200°C alongside normal 25°C readings, the computed
                  standard deviation artificially blows up, which can cause other true anomalies to remain undetected (the masking effect).
                </p>
              </div>
            </div>

            <div className="callout callout-info">
              <CheckCircle2 size={20} className="callout-icon" />
              <div>
                <strong>Multi-Sensor Unit Normalization:</strong>
                <p>
                  Temperature (°C), Pressure (kPa), and Vibration (g) have completely different physical dimensions.
                  Our backend groups readings by <code>sensorType</code> before computing μ and σ,
                  preventing Kilopascals from dominating Celsius readings.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: ISOLATION FOREST ML */}
        {activeTab === 'machineLearning' && (
          <div className="guide-section">
            <h4 className="section-title">Isolation Forest (Unsupervised Machine Learning)</h4>
            <p>
              Proposed by Fei Tony Liu, Kai Ming Ting, and Zhi-Hua Zhou (2008), <strong>Isolation Forest</strong>
              is an established unsupervised machine learning ensemble that isolates anomalies instead of
              profiling normal data points.
            </p>

            <div className="concept-grid">
              <div className="concept-card">
                <h5>1. "Few and Different" Principle</h5>
                <p>
                  Anomalous points are isolated closer to the root of a random decision tree because they
                  require fewer random cuts to separate from the cluster.
                </p>
              </div>
              <div className="concept-card">
                <h5>2. Tree Ensemble (iTrees)</h5>
                <p>
                  We construct an ensemble of 100 binary trees with max depth <code>⌈log₂(n)⌉</code>.
                  Each internal node chooses a random split between <code>min(X)</code> and <code>max(X)</code>.
                </p>
              </div>
              <div className="concept-card">
                <h5>3. Anomaly Score s(x, n)</h5>
                <p>
                  <code>s(x, n) = 2^(-E(h(x)) / c(n))</code>, where <code>E(h(x))</code> is average tree depth,
                  and <code>c(n) = 2(ln(n-1) + 0.5772) - 2(n-1)/n</code> is BST average depth.
                  Scores approaching 1.0 are confident anomalies.
                </p>
              </div>
            </div>

            <div className="callout callout-success">
              <CheckCircle2 size={20} className="callout-icon" />
              <div>
                <strong>Why Pure Java Implementation?</strong>
                <p>
                  Rather than hiding the algorithm behind an opaque native C++ wrapper that breaks across operating systems,
                  our implementation in <code>IsolationForestDetector.java</code> exposes the clean recursive tree node logic,
                  BST harmonic depth calculation, and ensemble voting.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: ARCHITECTURE */}
        {activeTab === 'architecture' && (
          <div className="guide-section">
            <h4 className="section-title">Full-Stack Data Flow & Architecture</h4>
            <p>
              The application adheres to clean separation of concerns:
            </p>

            <div className="pipeline-steps">
              <div className="pipeline-step">
                <span className="step-num">1</span>
                <div>
                  <strong>Client Ingestion (React & TypeScript)</strong>
                  <p>User selects CSV or clicks sample dataset. Input validated before dispatching via FormData.</p>
                </div>
              </div>

              <div className="pipeline-step">
                <span className="step-num">2</span>
                <div>
                  <strong>REST Controller (Spring Boot)</strong>
                  <p><code>SensorAnalysisController</code> receives multipart upload, sanitizes filename, and passes stream.</p>
                </div>
              </div>

              <div className="pipeline-step">
                <span className="step-num">3</span>
                <div>
                  <strong>Safe CSV Parser (Apache Commons CSV)</strong>
                  <p><code>SensorDataParserService</code> enforces mandatory columns, rejects non-finite numbers, and creates immutable records.</p>
                </div>
              </div>

              <div className="pipeline-step">
                <span className="step-num">4</span>
                <div>
                  <strong>Strategy Pattern Detectors</strong>
                  <p><code>AnomalyDetector</code> interface enables swappable execution of Statistical vs Isolation Forest models.</p>
                </div>
              </div>

              <div className="pipeline-step">
                <span className="step-num">5</span>
                <div>
                  <strong>Thread-Safe Cache & Report Export</strong>
                  <p>Results cached in memory via <code>AtomicReference</code> for secure CSV downloads via <code>/api/report</code>.</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: INTERVIEW TIPS */}
        {activeTab === 'interview' && (
          <div className="guide-section">
            <h4 className="section-title">Mentorship: Talking Points for Internship Interviews</h4>
            <div className="qa-list">
              <div className="qa-item">
                <h5 className="qa-q"><HelpCircle size={16} /> Q: How did you handle sensor noise versus actual hardware failure?</h5>
                <p className="qa-a">
                  "In physical sensors, noise often appears as high-frequency single-point jitter, while failures manifest
                  as sustained out-of-spec excursions or zero-variance sensor freezing. In our system, the user can tune
                  the sensitivity threshold (e.g. 2.0σ to 3.5σ) or switch to Isolation Forest which evaluates
                  isolation depth without assuming Gaussian distribution."
                </p>
              </div>

              <div className="qa-item">
                <h5 className="qa-q"><HelpCircle size={16} /> Q: Why did you group readings by sensor type?</h5>
                <p className="qa-a">
                  "Each sensor measures different physical dimensions. If ambient temperature (24°C) and pressure (101.3 kPa)
                  are lumped into a single dataset, the combined standard deviation becomes artificially high, rendering
                  temperature outlier detection impossible. Partitioning ensures independent physical baselines."
                </p>
              </div>

              <div className="qa-item">
                <h5 className="qa-q"><HelpCircle size={16} /> Q: Why is this simulation appropriate for an academic portfolio?</h5>
                <p className="qa-a">
                  "We explicitly state that this is an educational simulation. We never claim it was tested on active
                  defense or classified equipment. Instead, it demonstrates mastery of core software engineering:
                  REST APIs, Bean Validation, Apache Commons CSV, Strategy Pattern, Unit Testing, and React visual telemetry."
                </p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
