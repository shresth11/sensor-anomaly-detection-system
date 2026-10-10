# Testing Documentation & Verification Guide

## 1. Test Suite Overview

The backend is verified using **JUnit 5**, **Spring Boot Test**, and **Spring MVC MockMvc**.

Total Test Count: **23 Automated Tests** (100% Pass Rate).

| Test Class | Category | Test Count | Key Scenarios Covered |
|---|---|:---:|---|
| `StatisticalZScoreDetectorTest` | Unit Test | 6 | Empty data, normal readings, outlier spike, uniform data (zero std dev), threshold sensitivity, NaN/Infinity handling |
| `IsolationForestDetectorTest` | Unit Test | 3 | Outlier isolation score > 0.60, uniform distribution handling, empty list safety |
| `SensorDataParserServiceTest` | Unit Test | 6 | Valid CSV parsing, case-insensitive headers, missing columns, invalid non-numbers, blank timestamps, empty payload |
| `AnalysisServiceTest` | Unit Test | 2 | Orchestration flow, stats aggregation (mean, std dev, min, max, count), report caching, empty dataset rejection |
| `SensorAnalysisControllerTest` | Integration Test | 6 | GET `/api/health`, GET `/api/sample-data`, GET `/api/analyze/sample`, POST `/api/analyze` (success & empty 400), GET `/api/report` |

---

## 2. Test Execution Command

Run the complete backend test suite from the `backend/` folder:

```powershell
# Set JAVA_HOME and execute tests
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.2"
mvn clean test
```

### Verified Test Run Output

```text
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in com.sensordetection.controller.SensorAnalysisControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in com.sensordetection.detector.IsolationForestDetectorTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in com.sensordetection.detector.StatisticalZScoreDetectorTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in com.sensordetection.service.AnalysisServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in com.sensordetection.service.SensorDataParserServiceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 3. Key Edge Cases Tested

1. **Zero Standard Deviation (Uniform Readings):**
   When all sensor readings are identical (e.g. pressure holds constant at 101.3 kPa), population standard deviation $\sigma = 0.0$. Division by zero is avoided; $z = 0.0$ is assigned, and readings are flagged as `NORMAL`.

2. **NaN and Non-Finite Numbers:**
   Malicious or corrupted telemetry containing `NaN` or `Infinity` is caught during parsing. In the detector, if any non-finite value passes, it is flagged as `ANOMALY` with an explanatory diagnostic message rather than causing an unhandled arithmetic exception.

3. **Missing Columns:**
   If a client uploads a CSV missing the `value` or `timestamp` column, `SensorDataParserService` immediately throws an `IllegalArgumentException` indicating the exact missing header, returning an HTTP 400 Bad Request.

4. **Multi-Scale Multi-Sensor Telemetry:**
   Temperature (~25°C), Pressure (~101 kPa), and Vibration (~0.03 g) are grouped into independent partitions. Outlier spikes in vibration do not pollute temperature calculations.
