import React from 'react';
import { Activity, ShieldAlert, Cpu } from 'lucide-react';

interface HeaderProps {
  backendConnected: boolean;
  detectorType: string;
}

export const Header: React.FC<HeaderProps> = ({ backendConnected, detectorType }) => {
  return (
    <header className="dashboard-header">
      <div className="header-brand">
        <div className="brand-icon-wrapper">
          <Activity className="brand-icon" size={28} />
        </div>
        <div>
          <h1 className="brand-title">Sensor Anomaly Detection System</h1>
          <p className="brand-subtitle">
            Autonomous Multi-Sensor Telemetry Verification & Outlier Analysis
          </p>
        </div>
      </div>

      <div className="header-meta">
        <div className="meta-badge disclaimer-badge">
          <ShieldAlert size={14} />
          <span>Educational Simulation</span>
        </div>

        <div className="meta-badge algorithm-badge">
          <Cpu size={14} />
          <span>{detectorType === 'ISOLATION_FOREST' ? 'Isolation Forest (ML)' : 'Statistical Z-Score'}</span>
        </div>

        <div className={`meta-badge status-badge ${backendConnected ? 'online' : 'checking'}`}>
          <span className="status-dot"></span>
          <span>{backendConnected ? 'API Connected' : 'Checking Backend'}</span>
        </div>
      </div>
    </header>
  );
};
