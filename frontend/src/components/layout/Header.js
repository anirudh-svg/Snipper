import React, { useState, useRef, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import styles from './Header.module.css';

const Header = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleLogout = async () => {
    await logout();
    setShowDropdown(false);
    navigate('/');
  };

  const isActiveLink = (path) => {
    return location.pathname === path;
  };

  const getUserInitials = (user) => {
    if (!user || !user.username) return 'U';
    return user.username.charAt(0).toUpperCase();
  };

  return (
    <header className={styles.header}>
      <nav className={styles.nav}>
        <Link to="/" className={styles.logo}>
          Snipper
        </Link>

        <ul className={styles.navLinks}>
          <li>
            <Link 
              to="/explore" 
              className={`${styles.navLink} ${isActiveLink('/explore') ? styles.active : ''}`}
            >
              Explore
            </Link>
          </li>
          {isAuthenticated && (
            <li>
              <Link 
                to="/dashboard" 
                className={`${styles.navLink} ${isActiveLink('/dashboard') ? styles.active : ''}`}
              >
                Dashboard
              </Link>
            </li>
          )}
        </ul>

        <div>
          {isAuthenticated ? (
            <div className={styles.userMenu} ref={dropdownRef}>
              <button 
                className={styles.userButton}
                onClick={() => setShowDropdown(!showDropdown)}
                aria-expanded={showDropdown}
                aria-haspopup="true"
              >
                <div className={styles.avatar}>
                  {getUserInitials(user)}
                </div>
                <span>{user?.username || 'User'}</span>
              </button>
              
              {showDropdown && (
                <div className={styles.dropdown}>
                  <Link 
                    to="/profile" 
                    className={styles.dropdownItem}
                    onClick={() => setShowDropdown(false)}
                  >
                    Profile
                  </Link>
                  <Link 
                    to="/dashboard" 
                    className={styles.dropdownItem}
                    onClick={() => setShowDropdown(false)}
                  >
                    My Snippets
                  </Link>
                  <button 
                    className={styles.dropdownItem}
                    onClick={handleLogout}
                  >
                    Sign Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className={styles.authButtons}>
              <Link to="/login" className={`${styles.authButton} ${styles.loginButton}`}>
                Sign In
              </Link>
              <Link to="/register" className={`${styles.authButton} ${styles.signupButton}`}>
                Sign Up
              </Link>
            </div>
          )}
        </div>
      </nav>
    </header>
  );
};

export default Header;