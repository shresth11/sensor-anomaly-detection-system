import React, { useState, useEffect, useCallback } from 'react';
import { Header } from './components/Header';
import { MetricsOverview } from './components/MetricsOverview';
import { UploadControls } from './components/UploadControls';
import { TelemetryChart } from './components/TelemetryChart';
import { ReadingsTable } from './components/ReadingsTable';
import { EducationalGuide } from './components/EducationalGuide';
import { AnalysisResponse, DetectorType } from './types/sensor';
import { analyzeSample, analyzeUpload, fetchHealth } from './services/api';
import './App.css';

export const App: React.FC = () => {
  const [backendConnected, setBackendConnected] = useState<boolean>(false);
  const [detectorType, setDetectorType] = useState<DetectorType>('STATISTICAL_ZSCORE');
  const [threshold, setThreshold] = useState<number>(2.0);
  const [analysisData, setAnalysisData] = useState<AnalysisResponse | null>(null);
  const [datasetName, setDatasetName] = useState<string | null>(null);
  const [currentFile, setCurrentFile] = useState<File | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Check health and connectivity on mount
  useEffect(() => {
    let isMounted = true;
    fetchHealth()
      .then(() => {
        if (isMounted) setBackendConnected(true);
      })
      .catch(() => {
        if (isMounted) setBackendConnected(false);
      });
    return () => {
      isMounted = false;
    };
  }, []);

  // Clear banners after 6 seconds
  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => setSuccessMessage(null), 6000);
      return () => clearTimeout(timer);
    }
  }, [successMessage]);

  const handleLoadSample = useCallback(async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const response = await analyzeSample(threshold, detectorType);
      setAnalysisData(response);
      setDatasetName('sensor_sample_data.csv (Simulated Telemetry)');
      setCurrentFile(null);
      setSuccessMessage(
        `Successfully analyzed ${response.stats.totalReadings} readings using ${response.detectorType}. Flagged ${response.stats.anomalyCount} anomalies.`
      );
      setBackendConnected(true);
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to analyze sample dataset.');
    } finally {
      setLoading(false);
    }
  }, [threshold, detectorType]);

  const handleFileUpload = useCallback(
    async (file: File) => {
      setLoading(true);
      setErrorMessage(null);
      try {
        const response = await analyzeUpload(file, threshold, detectorType);
        setAnalysisData(response);
        setDatasetName(file.name);
        setCurrentFile(file);
        setSuccessMessage(
          `Successfully processed '${file.name}'. Evaluated ${response.stats.totalReadings} records with ${response.stats.anomalyCount} anomalies detected.`
        );
        setBackendConnected(true);
      } catch (err: any) {
        setErrorMessage(err.message || 'Failed to process uploaded file.');
      } finally {
        setLoading(false);
      }
    },
    [threshold, detectorType]
  );

  // If user adjusts threshold or algorithm and already has data loaded, re-analyze seamlessly
  useEffect(() => {
    if (!analysisData) return;

    if (currentFile) {
      handleFileUpload(currentFile);
    } else {
      handleLoadSample();
    }
  }, [threshold, detectorType]);

  const handleReset = () => {
    setAnalysisData(null);
    setDatasetName(null);
    setCurrentFile(null);
    setErrorMessage(null);
    setSuccessMessage(null);
  };

  return (
    <div className="dashboard-container">
      {/* Top Navigation & Status */}
      <Header backendConnected={backendConnected} detectorType={detectorType} />

      {/* Notifications / Alerts */}
      {errorMessage && (
        <div className="notification-banner error">
          <span>{errorMessage}</span>
          <button className="banner-close-btn" onClick={() => setErrorMessage(null)}>
            ×
          </button>
        </div>
      )}

      {successMessage && (
        <div className="notification-banner success">
          <span>{successMessage}</span>
          <button className="banner-close-btn" onClick={() => setSuccessMessage(null)}>
            ×
          </button>
        </div>
      )}

      {/* Key Metric Overview Cards */}
      <MetricsOverview
        stats={analysisData ? analysisData.stats : null}
        datasetName={datasetName}
      />

      {/* Telemetry Ingestion & Config Panel */}
      <UploadControls
        detectorType={detectorType}
        setDetectorType={setDetectorType}
        threshold={threshold}
        setThreshold={setThreshold}
        onLoadSample={handleLoadSample}
        onFileUpload={handleFileUpload}
        onReset={handleReset}
        loading={loading}
        hasResults={analysisData !== null}
      />

      {/* Interactive Visualisation Charts */}
      {analysisData && (
        <>
          <TelemetryChart results={analysisData.results} />
          <ReadingsTable results={analysisData.results} />
        </>
      )}

      {/* Engineering Knowledge & Mentorship Guide */}
      <EducationalGuide />
    </div>
  );
};
