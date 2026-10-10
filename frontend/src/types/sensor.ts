export type SensorType = 'temperature' | 'pressure' | 'vibration' | 'other';

export type DetectorType = 'STATISTICAL_ZSCORE' | 'ISOLATION_FOREST';

export interface SensorReading {
  timestamp: string;
  sensorType: string;
  value: number;
}

export interface AnomalyResult {
  timestamp: string;
  sensorType: string;
  value: number;
  score: number;
  anomalous: boolean;
  status: 'ANOMALY' | 'NORMAL';
  reason: string;
  originalIndex: number;
}

export interface DashboardStats {
  totalReadings: number;
  anomalyCount: number;
  normalCount: number;
  anomalyPercentage: number;
  averageValue: number;
  minValue: number;
  maxValue: number;
  standardDeviation: number;
  datasetName: string;
  detectorUsed: string;
  thresholdUsed: number;
  anomaliesBySensorType: Record<string, number>;
}

export interface AnalysisResponse {
  success: boolean;
  message: string;
  stats: DashboardStats;
  results: AnomalyResult[];
  detectorType: string;
  threshold: number;
  analyzedAt: string;
}

export interface SampleDataResponse {
  filename: string;
  totalCount: number;
  readings: SensorReading[];
}

export interface ApiErrorResponse {
  success: boolean;
  error: string;
  message: string;
  status: number;
  timestamp: string;
}
