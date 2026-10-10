import React, { useState, useMemo } from 'react';
import { AnomalyResult } from '../types/sensor';
import {
  ResponsiveContainer,
  ComposedChart,
  Line,
  Scatter,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  CartesianGrid,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
} from 'recharts';
import { LineChart as ChartIcon, PieChart as PieIcon, BarChart2 } from 'lucide-react';

interface TelemetryChartProps {
  results: AnomalyResult[];
}

export const TelemetryChart: React.FC<TelemetryChartProps> = ({ results }) => {
  const [selectedSensor, setSelectedSensor] = useState<string>('all');

  // Discover distinct sensor types present in the results
  const sensorTypes = useMemo(() => {
    const types = new Set<string>();
    results.forEach((r) => types.add(r.sensorType.toLowerCase()));
    return Array.from(types);
  }, [results]);

  // Filtered readings based on active sensor filter tab
  const filteredData = useMemo(() => {
    if (selectedSensor === 'all') {
      return results;
    }
    return results.filter((r) => r.sensorType.toLowerCase() === selectedSensor);
  }, [results, selectedSensor]);

  // Transform data for Recharts: separate regular line value and anomalous highlight
  const chartData = useMemo(() => {
    return filteredData.map((item, idx) => ({
      index: idx,
      timestamp: item.timestamp.replace('T', ' '),
      shortTime: item.timestamp.includes('T') ? item.timestamp.split('T')[1] : item.timestamp,
      value: item.value,
      sensorType: item.sensorType,
      isAnomaly: item.anomalous,
      score: item.score,
      reason: item.reason,
      // Only populated if anomalous for the contrasting Scatter overlay
      anomalyValue: item.anomalous ? item.value : null,
    }));
  }, [filteredData]);

  // Summary counts for the pie chart
  const pieData = useMemo(() => {
    const anomalyCount = results.filter((r) => r.anomalous).length;
    const normalCount = results.length - anomalyCount;
    return [
      { name: 'Nominal', value: normalCount, color: '#10b981' },
      { name: 'Anomaly', value: anomalyCount, color: '#ef4444' },
    ];
  }, [results]);

  // Anomaly distribution by sensor type for the bar chart
  const barData = useMemo(() => {
    const counts: Record<string, { total: number; anomalies: number }> = {};
    results.forEach((r) => {
      const type = r.sensorType.toLowerCase();
      if (!counts[type]) counts[type] = { total: 0, anomalies: 0 };
      counts[type].total += 1;
      if (r.anomalous) counts[type].anomalies += 1;
    });

    return Object.entries(counts).map(([type, data]) => ({
      type: type.charAt(0).toUpperCase() + type.slice(1),
      anomalies: data.anomalies,
      total: data.total,
    }));
  }, [results]);

  if (results.length === 0) {
    return null;
  }

  return (
    <div className="analytics-section">
      {/* Time-Series Chart Card */}
      <div className="chart-card">
        <div className="chart-header">
          <div className="chart-title-group">
            <ChartIcon size={20} className="chart-icon" />
            <h3 className="chart-title">Telemetry Stream & Anomaly Scatter</h3>
          </div>

          {/* Sensor Type Filter Tabs */}
          <div className="sensor-tabs">
            <button
              className={`tab-btn ${selectedSensor === 'all' ? 'active' : ''}`}
              onClick={() => setSelectedSensor('all')}
            >
              All Readings ({results.length})
            </button>
            {sensorTypes.map((type) => (
              <button
                key={type}
                className={`tab-btn ${selectedSensor === type ? 'active' : ''}`}
                onClick={() => setSelectedSensor(type)}
              >
                {type.toUpperCase()}
              </button>
            ))}
          </div>
        </div>

        <div className="chart-wrapper">
          <ResponsiveContainer width="100%" height={380}>
            <ComposedChart
              data={chartData}
              margin={{ top: 20, right: 30, left: 10, bottom: 25 }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis
                dataKey="shortTime"
                stroke="#64748b"
                tick={{ fontSize: 12 }}
                tickMargin={10}
              />
              <YAxis
                stroke="#64748b"
                tick={{ fontSize: 12 }}
                domain={['auto', 'auto']}
              />
              <Tooltip
                content={({ active, payload }) => {
                  if (active && payload && payload.length) {
                    const data = payload[0].payload;
                    return (
                      <div className="custom-tooltip">
                        <div className="tooltip-header">
                          <span className="tooltip-time">{data.timestamp}</span>
                          <span
                            className={`badge ${data.isAnomaly ? 'badge-danger' : 'badge-success'}`}
                          >
                            {data.isAnomaly ? 'ANOMALY' : 'NORMAL'}
                          </span>
                        </div>
                        <div className="tooltip-body">
                          <p>
                            <strong>Sensor:</strong> {data.sensorType.toUpperCase()}
                          </p>
                          <p>
                            <strong>Telemetry Value:</strong> {data.value}
                          </p>
                          <p>
                            <strong>Anomaly Score:</strong> {data.score}
                          </p>
                          <p className="tooltip-reason">
                            <small>{data.reason}</small>
                          </p>
                        </div>
                      </div>
                    );
                  }
                  return null;
                }}
              />
              <Legend verticalAlign="top" height={36} />
              {/* Continuous Telemetry Line */}
              <Line
                type="monotone"
                dataKey="value"
                name="Sensor Telemetry"
                stroke="#0284c7"
                strokeWidth={2}
                dot={{ r: 3, fill: '#0284c7' }}
                activeDot={{ r: 6 }}
              />
              {/* Distinct Contrasting Red Anomaly Dots */}
              <Scatter
                dataKey="anomalyValue"
                name="Flagged Anomaly"
                fill="#ef4444"
                stroke="#991b1b"
                strokeWidth={2}
                shape="circle"
              />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Side Summary Distribution Charts */}
      <div className="distribution-grid">
        {/* Nominal vs Anomaly Pie/Donut Chart */}
        <div className="distribution-card">
          <div className="chart-title-group">
            <PieIcon size={18} className="chart-icon" />
            <h4 className="subchart-title">Status Breakdown</h4>
          </div>
          <div className="subchart-wrapper">
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={80}
                  paddingAngle={4}
                  dataKey="value"
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                >
                  {pieData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Anomalies by Sensor Category Bar Chart */}
        <div className="distribution-card">
          <div className="chart-title-group">
            <BarChart2 size={18} className="chart-icon" />
            <h4 className="subchart-title">Anomalies by Sensor Type</h4>
          </div>
          <div className="subchart-wrapper">
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={barData} margin={{ top: 20, right: 20, left: -10, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="type" tick={{ fontSize: 12 }} stroke="#64748b" />
                <YAxis allowDecimals={false} tick={{ fontSize: 12 }} stroke="#64748b" />
                <Tooltip />
                <Bar dataKey="anomalies" name="Anomalies" fill="#f43f5e" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};
