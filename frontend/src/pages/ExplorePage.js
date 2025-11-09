import React, { useState, useEffect } from 'react';
import SnippetList from '../components/snippet/SnippetList';
import { getSnippets, searchSnippets } from '../services/api';
import styles from './ExplorePage.module.css';

const LANGUAGES = [
  'All', 'javascript', 'typescript', 'python', 'java', 'csharp', 'cpp', 'c',
  'go', 'rust', 'php', 'ruby', 'swift', 'kotlin', 'scala',
  'html', 'css', 'sql', 'bash', 'powershell', 'yaml', 'json', 'xml',
  'markdown', 'plaintext'
];

const ExplorePage = () => {
  const [snippets, setSnippets] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('All');
  const [tagFilter, setTagFilter] = useState('');

  useEffect(() => {
    fetchSnippets();
  }, [currentPage, selectedLanguage]);

  const fetchSnippets = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const params = {
        page: currentPage,
        size: 12,
        visibility: 'PUBLIC'
      };

      if (selectedLanguage !== 'All') {
        params.language = selectedLanguage;
      }

      if (tagFilter) {
        params.tags = tagFilter;
      }

      const data = await getSnippets(params);
      setSnippets(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      console.error('Error fetching snippets:', err);
      setError('Failed to load snippets. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    
    if (!searchQuery.trim()) {
      fetchSnippets();
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const params = {
        page: currentPage,
        size: 12,
        visibility: 'PUBLIC'
      };

      if (selectedLanguage !== 'All') {
        params.language = selectedLanguage;
      }

      if (tagFilter) {
        params.tags = tagFilter;
      }

      const data = await searchSnippets(searchQuery, params);
      setSnippets(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      console.error('Error searching snippets:', err);
      setError('Failed to search snippets. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLanguageChange = (language) => {
    setSelectedLanguage(language);
    setCurrentPage(0);
  };

  const handleTagFilterChange = (e) => {
    setTagFilter(e.target.value);
  };

  const handleApplyFilters = () => {
    setCurrentPage(0);
    fetchSnippets();
  };

  const handleClearFilters = () => {
    setSearchQuery('');
    setSelectedLanguage('All');
    setTagFilter('');
    setCurrentPage(0);
  };

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className={styles.explorePage}>
      <div className={styles.header}>
        <h1 className={styles.title}>Explore Snippets</h1>
        <p className={styles.subtitle}>
          Discover code snippets shared by the community
        </p>
      </div>

      <div className={styles.searchSection}>
        <form onSubmit={handleSearch} className={styles.searchForm}>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search snippets by title, description, or tags..."
            className={styles.searchInput}
          />
          <button type="submit" className={styles.searchButton}>
            Search
          </button>
        </form>
      </div>

      <div className={styles.filtersSection}>
        <div className={styles.filterGroup}>
          <label className={styles.filterLabel}>Language:</label>
          <select
            value={selectedLanguage}
            onChange={(e) => handleLanguageChange(e.target.value)}
            className={styles.filterSelect}
          >
            {LANGUAGES.map((lang) => (
              <option key={lang} value={lang}>
                {lang === 'All' ? 'All Languages' : lang.charAt(0).toUpperCase() + lang.slice(1)}
              </option>
            ))}
          </select>
        </div>

        <div className={styles.filterGroup}>
          <label className={styles.filterLabel}>Tags:</label>
          <input
            type="text"
            value={tagFilter}
            onChange={handleTagFilterChange}
            placeholder="Filter by tag"
            className={styles.filterInput}
          />
        </div>

        <div className={styles.filterActions}>
          <button onClick={handleApplyFilters} className={styles.applyButton}>
            Apply Filters
          </button>
          <button onClick={handleClearFilters} className={styles.clearButton}>
            Clear
          </button>
        </div>
      </div>

      <SnippetList
        snippets={snippets}
        isLoading={isLoading}
        error={error}
        emptyMessage="No public snippets found. Try adjusting your filters."
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={handlePageChange}
      />
    </div>
  );
};

export default ExplorePage;
