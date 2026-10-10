import React, { useState, useMemo } from 'react';
import { AnomalyResult } from '../types/sensor';
import { Search, AlertCircle, CheckCircle, ChevronLeft, ChevronRight } from 'lucide-react';

interface ReadingsTableProps {
  results: AnomalyResult[];
}

export const ReadingsTable: React.FC<ReadingsTableProps> = ({ results }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ANOMALY' | 'NORMAL'>('ALL');
  const [sensorFilter, setSensorFilter] = useState<string>('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;

  // Filter options
  const sensorTypes = useMemo(() => {
    const set = new Set<string>();
    results.forEach((r) => set.add(r.sensorType.toLowerCase()));
    return Array.from(set);
  }, [results]);

  const filteredResults = useMemo(() => {
    return results.filter((item) => {
      // Search term filter
      const matchesSearch =
        item.timestamp.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.sensorType.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.value.toString().includes(searchTerm) ||
        item.reason.toLowerCase().includes(searchTerm.toLowerCase());

      // Status filter
      const matchesStatus =
        statusFilter === 'ALL' || item.status === statusFilter;

      // Sensor type filter
      const matchesSensor =
        sensorFilter === 'ALL' || item.sensorType.toLowerCase() === sensorFilter;

      return matchesSearch && matchesStatus && matchesSensor;
    });
  }, [results, searchTerm, statusFilter, sensorFilter]);

  // Pagination calculation
  const totalPages = Math.max(1, Math.ceil(filteredResults.length / pageSize));
  const paginatedData = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return filteredResults.slice(start, start + pageSize);
  }, [filteredResults, currentPage, pageSize]);

  const handlePageChange = (newPage: number) => {
    if (newPage >= 1 && newPage <= totalPages) {
      setCurrentPage(newPage);
    }
  };

  return (
    <div className="table-card">
      <div className="table-header-group">
        <div className="table-title-area">
          <h3 className="table-title">Telemetry Audit Log</h3>
          <span className="table-subtitle">
            Showing {filteredResults.length} of {results.length} total entries
          </span>
        </div>

        {/* Filter Controls */}
        <div className="table-controls">
          {/* Search Input */}
          <div className="search-box">
            <Search size={16} className="search-icon" />
            <input
              type="text"
              placeholder="Search timestamp, sensor..."
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setCurrentPage(1);
              }}
              className="search-input"
            />
          </div>

          {/* Sensor Filter */}
          <select
            value={sensorFilter}
            onChange={(e) => {
              setSensorFilter(e.target.value);
              setCurrentPage(1);
            }}
            className="table-filter-select"
          >
            <option value="ALL">All Sensors</option>
            {sensorTypes.map((t) => (
              <option key={t} value={t}>
                {t.toUpperCase()}
              </option>
            ))}
          </select>

          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value as any);
              setCurrentPage(1);
            }}
            className="table-filter-select"
          >
            <option value="ALL">All Statuses</option>
            <option value="ANOMALY">Anomalies Only</option>
            <option value="NORMAL">Normal Only</option>
          </select>
        </div>
      </div>

      {/* Table Container */}
      <div className="table-container">
        <table className="telemetry-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Timestamp</th>
              <th>Sensor Type</th>
              <th>Telemetry Value</th>
              <th>Anomaly Score</th>
              <th>Status</th>
              <th>Algorithmic Rationale</th>
            </tr>
          </thead>
          <tbody>
            {paginatedData.length === 0 ? (
              <tr>
                <td colSpan={7} className="empty-table-cell">
                  No readings match the current filter criteria.
                </td>
              </tr>
            ) : (
              paginatedData.map((item, index) => {
                const globalIndex = (currentPage - 1) * pageSize + index + 1;
                const isAnomaly = item.anomalous;
                const typeClass = `badge-sensor-${item.sensorType.toLowerCase()}`;

                return (
                  <tr key={`${item.timestamp}-${item.sensorType}-${index}`} className={isAnomaly ? 'row-anomaly' : ''}>
                    <td className="index-cell">{globalIndex}</td>
                    <td className="time-cell">{item.timestamp.replace('T', ' ')}</td>
                    <td>
                      <span className={`badge ${typeClass}`}>
                        {item.sensorType.toUpperCase()}
                      </span>
                    </td>
                    <td className="value-cell">{item.value.toFixed(3)}</td>
                    <td className="score-cell">{item.score.toFixed(4)}</td>
                    <td>
                      <span className={`badge ${isAnomaly ? 'badge-danger' : 'badge-success'}`}>
                        {isAnomaly ? (
                          <>
                            <AlertCircle size={12} /> ANOMALY
                          </>
                        ) : (
                          <>
                            <CheckCircle size={12} /> NORMAL
                          </>
                        )}
                      </span>
                    </td>
                    <td className="reason-cell" title={item.reason}>
                      {item.reason}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Bar */}
      <div className="table-pagination">
        <span className="pagination-info">
          Page {currentPage} of {totalPages}
        </span>
        <div className="pagination-buttons">
          <button
            className="pagination-btn"
            onClick={() => handlePageChange(currentPage - 1)}
            disabled={currentPage === 1}
          >
            <ChevronLeft size={16} /> Prev
          </button>
          <button
            className="pagination-btn"
            onClick={() => handlePageChange(currentPage + 1)}
            disabled={currentPage === totalPages}
          >
            Next <ChevronRight size={16} />
          </button>
        </div>
      </div>
    </div>
  );
};
