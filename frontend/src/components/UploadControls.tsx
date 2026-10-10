import React, { useRef } from 'react';
import { DetectorType } from '../types/sensor';
import { UploadCloud, Download, RotateCcw, Play, Sliders } from 'lucide-react';
import { getReportDownloadUrl } from '../services/api';

interface UploadControlsProps {
  detectorType: DetectorType;
  setDetectorType: (val: DetectorType) => void;
  threshold: number;
  setThreshold: (val: number) => void;
  onLoadSample: () => void;
  onFileUpload: (file: File) => void;
  onReset: () => void;
  loading: boolean;
  hasResults: boolean;
}

export const UploadControls: React.FC<UploadControlsProps> = ({
  detectorType,
  setDetectorType,
  threshold,
  setThreshold,
  onLoadSample,
  onFileUpload,
  onReset,
  loading,
  hasResults,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      onFileUpload(e.target.files[0]);
    }
  };

  const handleDetectorChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const nextType = e.target.value as DetectorType;
    setDetectorType(nextType);
    // Adjust default threshold intelligently
    if (nextType === 'ISOLATION_FOREST') {
      setThreshold(0.60);
    } else {
      setThreshold(2.0);
    }
  };

  return (
    <div className="control-panel-card">
      <div className="panel-header">
        <div className="panel-title-group">
          <Sliders size={20} className="panel-icon" />
          <h2 className="panel-title">Telemetry Ingestion & Algorithm Configuration</h2>
        </div>
      </div>

      <div className="controls-grid">
        {/* Upload Zone */}
        <div className="upload-box-wrapper">
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept=".csv"
            style={{ display: 'none' }}
          />
          <div
            className="dropzone-area"
            onClick={() => fileInputRef.current?.click()}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') fileInputRef.current?.click();
            }}
          >
            <UploadCloud size={32} className="dropzone-icon" />
            <div className="dropzone-text">
              <span className="dropzone-main">Choose CSV File</span>
              <span className="dropzone-hint">Required columns: timestamp, sensorType, value</span>
            </div>
          </div>
        </div>

        {/* Algorithm & Sensitivity Options */}
        <div className="algorithm-settings">
          <div className="form-group">
            <label htmlFor="detector-select" className="form-label">
              Detection Strategy
            </label>
            <select
              id="detector-select"
              className="form-select"
              value={detectorType}
              onChange={handleDetectorChange}
              disabled={loading}
            >
              <option value="STATISTICAL_ZSCORE">
                Statistical Z-Score (Mean & σ Baseline)
              </option>
              <option value="ISOLATION_FOREST">
                Isolation Forest (Unsupervised ML Ensemble)
              </option>
            </select>
            <span className="field-hint">
              {detectorType === 'STATISTICAL_ZSCORE'
                ? 'Flags readings exceeding |z| = |x - μ| / σ'
                : 'Isolates outliers using random binary partition tree depths'}
            </span>
          </div>

          <div className="form-group">
            <div className="label-with-value">
              <label htmlFor="threshold-input" className="form-label">
                Sensitivity Threshold:
              </label>
              <span className="threshold-badge">{threshold.toFixed(2)}</span>
            </div>
            {detectorType === 'STATISTICAL_ZSCORE' ? (
              <input
                id="threshold-input"
                type="range"
                min="1.0"
                max="4.0"
                step="0.1"
                value={threshold}
                onChange={(e) => setThreshold(parseFloat(e.target.value))}
                className="form-range"
                disabled={loading}
              />
            ) : (
              <input
                id="threshold-input"
                type="range"
                min="0.45"
                max="0.85"
                step="0.05"
                value={threshold}
                onChange={(e) => setThreshold(parseFloat(e.target.value))}
                className="form-range"
                disabled={loading}
              />
            )}
            <div className="range-labels">
              <span>{detectorType === 'STATISTICAL_ZSCORE' ? '1.0σ (High sensitivity)' : '0.45 (Sensitive)'}</span>
              <span>{detectorType === 'STATISTICAL_ZSCORE' ? '4.0σ (Extreme only)' : '0.85 (Strict)'}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Action Buttons Toolbar */}
      <div className="action-toolbar">
        <button
          className="btn btn-primary"
          onClick={onLoadSample}
          disabled={loading}
        >
          {loading ? (
            <span className="spinner"></span>
          ) : (
            <Play size={16} />
          )}
          Load Sample Telemetry
        </button>

        {hasResults && (
          <a
            href={getReportDownloadUrl()}
            download="sensor_anomaly_report.csv"
            className="btn btn-secondary"
          >
            <Download size={16} />
            Download Analysis Report (CSV)
          </a>
        )}

        <button
          className="btn btn-ghost"
          onClick={onReset}
          disabled={loading || !hasResults}
        >
          <RotateCcw size={16} />
          Reset Dataset
        </button>
      </div>
    </div>
  );
};
