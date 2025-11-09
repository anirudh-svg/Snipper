import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useApp } from '../../contexts/AppContext';
import styles from './Sidebar.module.css';

const Sidebar = () => {
  const { state } = useApp();
  const { isAuthenticated } = state;
  const location = useLocation();

  const isActiveLink = (path) => {
    return location.pathname === path;
  };

  // Mock data for demonstration - in real app this would come from API
  const userStats = {
    totalSnippets: 12,
    publicSnippets: 8,
    privateSnippets: 4,
    totalViews: 156
  };

  const popularLanguages = [
    { name: 'JavaScript', count: 5 },
    { name: 'Python', count: 3 },
    { name: 'Java', count: 2 },
    { name: 'TypeScript', count: 2 }
  ];

  if (!isAuthenticated) {
    return (
      <aside className={styles.sidebar}>
        <div className={styles.sidebarContent}>
          <section className={styles.section}>
            <h3 className={styles.sectionTitle}>Explore</h3>
            <ul className={styles.navList}>
              <li className={styles.navItem}>
                <Link 
                  to="/explore" 
                  className={`${styles.navLink} ${isActiveLink('/explore') ? styles.active : ''}`}
                >
                  <span className={styles.icon}>🔍</span>
                  Browse Snippets
                </Link>
              </li>
              <li className={styles.navItem}>
                <Link 
                  to="/explore?sort=popular" 
                  className={`${styles.navLink} ${location.search.includes('sort=popular') ? styles.active : ''}`}
                >
                  <span className={styles.icon}>⭐</span>
                  Popular
                </Link>
              </li>
              <li className={styles.navItem}>
                <Link 
                  to="/explore?sort=recent" 
                  className={`${styles.navLink} ${location.search.includes('sort=recent') ? styles.active : ''}`}
                >
                  <span className={styles.icon}>🕒</span>
                  Recent
                </Link>
              </li>
            </ul>
          </section>

          <section className={styles.section}>
            <h3 className={styles.sectionTitle}>Languages</h3>
            <ul className={styles.languageList}>
              {popularLanguages.map((lang) => (
                <li key={lang.name} className={styles.languageItem}>
                  <Link 
                    to={`/explore?language=${lang.name.toLowerCase()}`}
                    className={`${styles.languageLink} ${location.search.includes(`language=${lang.name.toLowerCase()}`) ? styles.active : ''}`}
                  >
                    <span>{lang.name}</span>
                    <span className={styles.languageCount}>{lang.count}</span>
                  </Link>
                </li>
              ))}
            </ul>
          </section>
        </div>
      </aside>
    );
  }

  return (
    <aside className={styles.sidebar}>
      <div className={styles.sidebarContent}>
        <Link to="/snippets/new" className={styles.createButton}>
          <span>+</span>
          New Snippet
        </Link>

        <section className={styles.section}>
          <h3 className={styles.sectionTitle}>My Snippets</h3>
          <ul className={styles.navList}>
            <li className={styles.navItem}>
              <Link 
                to="/dashboard" 
                className={`${styles.navLink} ${isActiveLink('/dashboard') ? styles.active : ''}`}
              >
                <span className={styles.icon}>📊</span>
                Dashboard
              </Link>
            </li>
            <li className={styles.navItem}>
              <Link 
                to="/snippets" 
                className={`${styles.navLink} ${isActiveLink('/snippets') ? styles.active : ''}`}
              >
                <span className={styles.icon}>📝</span>
                All Snippets
              </Link>
            </li>
            <li className={styles.navItem}>
              <Link 
                to="/snippets?visibility=public" 
                className={`${styles.navLink} ${location.search.includes('visibility=public') ? styles.active : ''}`}
              >
                <span className={styles.icon}>🌐</span>
                Public
              </Link>
            </li>
            <li className={styles.navItem}>
              <Link 
                to="/snippets?visibility=private" 
                className={`${styles.navLink} ${location.search.includes('visibility=private') ? styles.active : ''}`}
              >
                <span className={styles.icon}>🔒</span>
                Private
              </Link>
            </li>
          </ul>
        </section>

        <section className={styles.section}>
          <h3 className={styles.sectionTitle}>Explore</h3>
          <ul className={styles.navList}>
            <li className={styles.navItem}>
              <Link 
                to="/explore" 
                className={`${styles.navLink} ${isActiveLink('/explore') ? styles.active : ''}`}
              >
                <span className={styles.icon}>🔍</span>
                Browse All
              </Link>
            </li>
            <li className={styles.navItem}>
              <Link 
                to="/explore?sort=popular" 
                className={`${styles.navLink} ${location.search.includes('sort=popular') ? styles.active : ''}`}
              >
                <span className={styles.icon}>⭐</span>
                Popular
              </Link>
            </li>
          </ul>
        </section>

        <div className={styles.stats}>
          <h4 className={styles.statsTitle}>Your Stats</h4>
          <div className={styles.statItem}>
            <span className={styles.statLabel}>Total Snippets</span>
            <span className={styles.statValue}>{userStats.totalSnippets}</span>
          </div>
          <div className={styles.statItem}>
            <span className={styles.statLabel}>Public</span>
            <span className={styles.statValue}>{userStats.publicSnippets}</span>
          </div>
          <div className={styles.statItem}>
            <span className={styles.statLabel}>Private</span>
            <span className={styles.statValue}>{userStats.privateSnippets}</span>
          </div>
          <div className={styles.statItem}>
            <span className={styles.statLabel}>Total Views</span>
            <span className={styles.statValue}>{userStats.totalViews}</span>
          </div>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;