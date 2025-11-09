import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { getProfile, updateProfile } from '../services/api';
import styles from './ProfilePage.module.css';

const ProfilePage = () => {
  const { updateUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [formData, setFormData] = useState({
    username: '',
    email: ''
  });

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const data = await getProfile();
      setProfile(data);
      setFormData({
        username: data.username || '',
        email: data.email || ''
      });
    } catch (err) {
      console.error('Error fetching profile:', err);
      setError('Failed to load profile. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSaving(true);
    setError(null);
    setSuccessMessage(null);

    try {
      const updatedProfile = await updateProfile(formData);
      setProfile(updatedProfile);
      
      // Update user in auth context if username changed
      if (updateUser) {
        updateUser(updatedProfile);
      }
      
      setSuccessMessage('Profile updated successfully!');
      setTimeout(() => setSuccessMessage(null), 5000);
    } catch (err) {
      console.error('Error updating profile:', err);
      setError(err.response?.data?.message || 'Failed to update profile. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  if (isLoading) {
    return (
      <div className={styles.profilePage}>
        <div className={styles.loading}>
          <div className={styles.spinner}></div>
          <p>Loading profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.profilePage}>
      <div className={styles.header}>
        <h1 className={styles.title}>Profile Settings</h1>
        <p className={styles.subtitle}>
          Manage your account information
        </p>
      </div>

      {successMessage && (
        <div className={styles.successAlert}>
          <span className={styles.successIcon}>✓</span>
          <span>{successMessage}</span>
        </div>
      )}

      {error && (
        <div className={styles.errorAlert}>
          <span className={styles.errorIcon}>⚠️</span>
          <span>{error}</span>
        </div>
      )}

      <div className={styles.content}>
        <div className={styles.profileCard}>
          <h2 className={styles.cardTitle}>Account Information</h2>
          
          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.formGroup}>
              <label htmlFor="username" className={styles.label}>
                Username
              </label>
              <input
                type="text"
                id="username"
                name="username"
                value={formData.username}
                onChange={handleChange}
                className={styles.input}
                disabled={isSaving}
                required
              />
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="email" className={styles.label}>
                Email Address
              </label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className={styles.input}
                disabled={isSaving}
                required
              />
            </div>

            <div className={styles.formActions}>
              <button
                type="submit"
                className={styles.saveButton}
                disabled={isSaving}
              >
                {isSaving ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </form>
        </div>

        <div className={styles.infoCard}>
          <h2 className={styles.cardTitle}>Account Details</h2>
          
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>User ID:</span>
            <span className={styles.infoValue}>{profile?.id}</span>
          </div>

          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>Member Since:</span>
            <span className={styles.infoValue}>
              {profile?.createdAt ? formatDate(profile.createdAt) : 'N/A'}
            </span>
          </div>

          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>Last Updated:</span>
            <span className={styles.infoValue}>
              {profile?.updatedAt ? formatDate(profile.updatedAt) : 'N/A'}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
