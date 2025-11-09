import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import SnippetViewer from '../components/snippet/SnippetViewer';
import { getSnippet, deleteSnippet } from '../services/api';
import styles from './SnippetDetailPage.module.css';

const SnippetDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [snippet, setSnippet] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    const fetchSnippet = async () => {
      try {
        const data = await getSnippet(id);
        setSnippet(data);
      } catch (err) {
        console.error('Error fetching snippet:', err);
        if (err.response?.status === 404) {
          setError('Snippet not found');
        } else if (err.response?.status === 403) {
          setError('You do not have permission to view this snippet');
        } else {
          setError('Failed to load snippet. Please try again.');
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchSnippet();
  }, [id]);

  const handleEdit = () => {
    navigate(`/snippets/${id}/edit`);
  };

  const handleDelete = () => {
    setShowDeleteConfirm(true);
  };

  const confirmDelete = async () => {
    setIsDeleting(true);
    try {
      await deleteSnippet(id);
      navigate('/dashboard', { 
        state: { message: 'Snippet deleted successfully' } 
      });
    } catch (err) {
      console.error('Error deleting snippet:', err);
      setError('Failed to delete snippet. Please try again.');
      setShowDeleteConfirm(false);
      setIsDeleting(false);
    }
  };

  const cancelDelete = () => {
    setShowDeleteConfirm(false);
  };

  const isOwner = snippet && user && snippet.authorUsername === user.username;

  if (isLoading) {
    return (
      <div className={styles.snippetDetailPage}>
        <div className={styles.loading}>
          <div className={styles.spinner}></div>
          <p>Loading snippet...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.snippetDetailPage}>
        <div className={styles.errorContainer}>
          <div className={styles.errorIcon}>⚠️</div>
          <h2 className={styles.errorTitle}>Error</h2>
          <p className={styles.errorMessage}>{error}</p>
          <button 
            onClick={() => navigate('/dashboard')} 
            className={styles.backButton}
          >
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.snippetDetailPage}>
      {showDeleteConfirm && (
        <div className={styles.modal}>
          <div className={styles.modalContent}>
            <h3 className={styles.modalTitle}>Delete Snippet</h3>
            <p className={styles.modalMessage}>
              Are you sure you want to delete this snippet? This action cannot be undone.
            </p>
            <div className={styles.modalActions}>
              <button
                onClick={confirmDelete}
                className={styles.confirmButton}
                disabled={isDeleting}
              >
                {isDeleting ? 'Deleting...' : 'Delete'}
              </button>
              <button
                onClick={cancelDelete}
                className={styles.cancelButton}
                disabled={isDeleting}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      <SnippetViewer
        snippet={snippet}
        onEdit={handleEdit}
        onDelete={handleDelete}
        isOwner={isOwner}
      />
    </div>
  );
};

export default SnippetDetailPage;
