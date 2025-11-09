import React from 'react';
import { useLocation } from 'react-router-dom';
import { useApp } from '../../contexts/AppContext';
import Header from './Header';
import Sidebar from './Sidebar';
import styles from './Layout.module.css';

const Layout = ({ children, showSidebar = true }) => {
  const { state, actions } = useApp();
  const { loading, error } = state;
  const location = useLocation();

  // Determine if sidebar should be shown based on route
  const shouldShowSidebar = showSidebar && !['/login', '/register'].includes(location.pathname);

  const handleRetry = () => {
    // Reload the current page
    window.location.reload();
  };

  const handleDismissError = () => {
    actions.clearError();
  };

  return (
    <div className={styles.layout}>
      <Header />
      
      <main className={styles.main}>
        {shouldShowSidebar && <Sidebar />}
        
        <div className={shouldShowSidebar ? styles.contentWithSidebar : styles.contentFullWidth}>
          <div className={styles.container}>
            {/* Global Error Display */}
            {error && (
              <div className={styles.error}>
                <div className={styles.errorTitle}>Something went wrong</div>
                <div className={styles.errorMessage}>
                  {typeof error === 'string' ? error : error.message || 'An unexpected error occurred'}
                </div>
                <div className={styles.errorActions}>
                  <button 
                    className={`${styles.errorButton} ${styles.retryButton}`}
                    onClick={handleRetry}
                  >
                    Retry
                  </button>
                  <button 
                    className={`${styles.errorButton} ${styles.dismissButton}`}
                    onClick={handleDismissError}
                  >
                    Dismiss
                  </button>
                </div>
              </div>
            )}

            {/* Global Loading Display */}
            {loading && (
              <div className={styles.loading}>
                <div className={styles.spinner}></div>
                Loading...
              </div>
            )}

            {/* Page Content */}
            {!loading && children}
          </div>
        </div>
      </main>
    </div>
  );
};

export default Layout;