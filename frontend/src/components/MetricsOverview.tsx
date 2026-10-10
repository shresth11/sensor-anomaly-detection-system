import React from 'react';
import { DashboardStats } from '../types/sensor';
import { Database, AlertTriangle, Percent, Gauge, CheckCircle2 } from 'lucide-react';

interface MetricsOverviewProps {
  stats: DashboardStats | null;
  datasetName: string | null;
}

export const MetricsOverview: React.FC<MetricsOverviewProps> = ({ stats, datasetName }) => {
  if (!stats) {
    return (
      <div className="metrics-grid">
        <div className="metric-card skeleton-card">
          <div className="metric-header">
            <span className="metric-label">Dataset Status</span>
            <Database size={18} className="metric-icon" />
          </div>
          <div className="metric-value">No Dataset Loaded</div>
          <p className="metric-subtext">Upload a CSV file or load sample telemetry to begin</p>
        </div>
      </div>
    );
  }

  const isAnomalous = stats.anomalyCount > 0;

  return (
    <div className="metrics-grid">
      {/* Total Readings */}
      <div className="metric-card">
        <div className="metric-header">
          <span className="metric-label">Total Telemetry Points</span>
          <Database size={18} className="metric-icon info-icon" />
        </div>
        <div className="metric-value">{stats.totalReadings.toLocaleString()}</div>
        <p className="metric-subtext">
          Dataset: <span className="highlight-text">{datasetName || stats.datasetName}</span>
        </p>
      </div>

      {/* Detected Anomalies */}
      <div className={`metric-card ${isAnomalous ? 'alert-card' : 'normal-card'}`}>
        <div className="metric-header">
          <span className="metric-label">Detected Anomalies</span>
          {isAnomalous ? (
            <AlertTriangle size={18} className="metric-icon alert-icon" />
          ) : (
            <CheckCircle2 size={18} className="metric-icon success-icon" />
          )}
        </div>
        <div className={`metric-value ${isAnomalous ? 'text-alert' : 'text-success'}`}>
          {stats.anomalyCount}
        </div>
        <p className="metric-subtext">
          {stats.normalCount} nominal readings verified
        </p>
      </div>

      {/* Percentage Flagged */}
      <div className="metric-card">
        <div className="metric-header">
          <span className="metric-label">Anomaly Ratio</span>
          <Percent size={18} className="metric-icon warning-icon" />
        </div>
        <div className="metric-value">{stats.anomalyPercentage.toFixed(1)}%</div>
        <div className="progress-bar-container">
          <div
            className="progress-bar-fill"
            style={{ width: `${Math.min(stats.anomalyPercentage, 100)}%` }}
          />
        </div>
      </div>

      {/* Average Telemetry Value & Distribution */}
      <div className="metric-card">
        <div className="metric-header">
          <span className="metric-label">Mean Value (μ)</span>
          <Gauge size={18} className="metric-icon purple-icon" />
        </div>
        <div className="metric-value">{stats.averageValue.toFixed(2)}</div>
        <p className="metric-subtext">
          Std Dev (σ): {stats.standardDeviation.toFixed(2)} | Min: {stats.minValue} | Max: {stats.maxValue}
        </p>
      </div>
    </div>
  );
};
