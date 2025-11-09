import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import SnippetList from '../components/snippet/SnippetList';
import { getUserSnippets } from '../services/api';
import styles from './DashboardPage.module.css';

const DashboardPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  
  const [snippets, setSnippets] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [stats, setStats] = useState({
    total: 0,
    public: 0,
    private: 0
  });
  const [sortBy, setSortBy] = useState('createdAt');
  const [filterVisibility, setFilterVisibility] = useState('ALL');
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    if (location.state?.message) {
      setSuccessMessage(location.state.message);
      // Clear the message from location state
      window.history.replaceState({}, document.title);
      setTimeout(() => setSuccessMessage(null), 5000);
    }
  }, [location]);

  useEffect(() => {
    if (user?.username) {
      fetchUserSnippets();
    }
  }, [user, currentPage, sortBy, filterVisibility]);

  const fetchUserSnippets = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const params = {
        page: currentPage,
        size: 12,
        sort: sortBy
      };

      if (filterVisibility !== 'ALL') {
        params.visibility = filterVisibility;
      }

      const data = await getUserSnippets(user.username, params);
      setSnippets(data.content || []);
      setTotalPages(data.totalPages || 0);
      
      // Calculate stats
      const allSnippets = data.content || [];
      setStats({
        total: data.totalElements || allSnippets.length,
        public: allSnippets.filter(s => s.visibility === 'PUBLIC').length,
        private: allSnippets.filter(s => s.visibility === 'PRIVATE').length
      });
    } catch (err) {
      console.error('Error fetching user snippets:', err);
      setError('Failed to load your snippets. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateSnippet = () => {
    navigate('/snippets/new');
  };

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSortChange = (e) => {
    setSortBy(e.target.value);
    setCurrentPage(0);
  };

  const handleFilterChange = (e) => {
    setFilterVisibility(e.target.value);
    setCurrentPage(0);
  };

  return (
    <div className={styles.dashboardPage}>
      <div className={styles.header}>
        <div className={styles.headerContent}>
          <h1 className={styles.title}>My Dashboard</h1>
          <p className={styles.subtitle}>
            Welcome back, <strong>{user?.username}</strong>!
          </p>
        </div>
        <button onClick={handleCreateSnippet} className={styles.createButton}>
          + Create Snippet
        </button>
      </div>

      {successMessage && (
        <div className={styles.successAlert}>
          <span className={styles.successIcon}>✓</span>
          <span>{successMessage}</span>
        </div>
      )}

      <div className={styles.statsSection}>
        <div className={styles.statCard}>
          <div className={styles.statValue}>{stats.total}</div>
          <div className={styles.statLabel}>Total Snippets</div>
        </div>
        <div className={styles.statCard}>
          <div className={styles.statValue}>{stats.public}</div>
          <div className={styles.statLabel}>Public</div>
        </div>
        <div className={styles.statCard}>
          <div className={styles.statValue}>{stats.private}</div>
          <div className={styles.statLabel}>Private</div>
        </div>
      </div>

      <div className={styles.controlsSection}>
        <div className={styles.filterGroup}>
          <label className={styles.filterLabel}>Filter:</label>
          <select
            value={filterVisibility}
            onChange={handleFilterChange}
            className={styles.filterSelect}
          >
            <option value="ALL">All Snippets</option>
            <option value="PUBLIC">Public Only</option>
            <option value="PRIVATE">Private Only</option>
          </select>
        </div>

        <div className={styles.filterGroup}>
          <label className={styles.filterLabel}>Sort by:</label>
          <select
            value={sortBy}
            onChange={handleSortChange}
            className={styles.filterSelect}
          >
            <option value="createdAt">Date Created</option>
            <option value="updatedAt">Last Updated</option>
            <option value="title">Title</option>
            <option value="language">Language</option>
          </select>
        </div>
      </div>

      <div className={styles.snippetsSection}>
        <h2 className={styles.sectionTitle}>Your Snippets</h2>
        <SnippetList
          snippets={snippets}
          isLoading={isLoading}
          error={error}
          emptyMessage="You haven't created any snippets yet. Click 'Create Snippet' to get started!"
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={handlePageChange}
        />
      </div>
    </div>
  );
};

export default DashboardPage;
