import React from 'react';
import SnippetCard from './SnippetCard';
import styles from './SnippetList.module.css';

const SnippetList = ({ 
  snippets, 
  isLoading, 
  error, 
  emptyMessage = 'No snippets found',
  currentPage = 0,
  totalPages = 0,
  onPageChange
}) => {
  if (isLoading) {
    return (
      <div className={styles.loading}>
        <div className={styles.spinner}></div>
        <p>Loading snippets...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.error}>
        <span className={styles.errorIcon}>⚠️</span>
        <p>{error}</p>
      </div>
    );
  }

  if (!snippets || snippets.length === 0) {
    return (
      <div className={styles.empty}>
        <span className={styles.emptyIcon}>📝</span>
        <p>{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className={styles.snippetList}>
      <div className={styles.grid}>
        {snippets.map((snippet) => (
          <SnippetCard key={snippet.id} snippet={snippet} />
        ))}
      </div>

      {totalPages > 1 && (
        <div className={styles.pagination}>
          <button
            onClick={() => onPageChange(currentPage - 1)}
            disabled={currentPage === 0}
            className={styles.pageButton}
          >
            Previous
          </button>

          <div className={styles.pageInfo}>
            Page {currentPage + 1} of {totalPages}
          </div>

          <button
            onClick={() => onPageChange(currentPage + 1)}
            disabled={currentPage >= totalPages - 1}
            className={styles.pageButton}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};

export default SnippetList;
