import { AnalysisResponse, SampleDataResponse, DetectorType, ApiErrorResponse } from '../types/sensor';

const BASE_URL = '/api';

/**
 * Parses and throws structured API error if response is not ok.
 */
async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let errorMessage = `HTTP error! Status: ${res.status}`;
    try {
      const errorJson: ApiErrorResponse = await res.json();
      if (errorJson.message) {
        errorMessage = errorJson.message;
      }
    } catch {
      // Body was not JSON
    }
    throw new Error(errorMessage);
  }
  return res.json();
}

/**
 * Health check endpoint.
 */
export async function fetchHealth(): Promise<{ status: string }> {
  const res = await fetch(`${BASE_URL}/health`);
  return handleResponse(res);
}

/**
 * Retrieves the built-in sample sensor telemetry records.
 */
export async function fetchSampleData(): Promise<SampleDataResponse> {
  const res = await fetch(`${BASE_URL}/sample-data`);
  return handleResponse<SampleDataResponse>(res);
}

/**
 * Analyzes the built-in sample dataset.
 */
export async function analyzeSample(
  threshold: number = 2.0,
  detectorType: DetectorType = 'STATISTICAL_ZSCORE'
): Promise<AnalysisResponse> {
  const params = new URLSearchParams({
    threshold: threshold.toString(),
    detectorType,
  });
  const res = await fetch(`${BASE_URL}/analyze/sample?${params}`);
  return handleResponse<AnalysisResponse>(res);
}

/**
 * Uploads user CSV file and triggers anomaly analysis.
 */
export async function analyzeUpload(
  file: File,
  threshold: number = 2.0,
  detectorType: DetectorType = 'STATISTICAL_ZSCORE'
): Promise<AnalysisResponse> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('threshold', threshold.toString());
  formData.append('detectorType', detectorType);

  const res = await fetch(`${BASE_URL}/analyze`, {
    method: 'POST',
    body: formData,
  });
  return handleResponse<AnalysisResponse>(res);
}

/**
 * Returns direct download URL for the generated CSV report.
 */
export function getReportDownloadUrl(): string {
  return `${BASE_URL}/report`;
}
