import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import SnippetEditor from '../components/snippet/SnippetEditor';
import { getSnippet, updateSnippet } from '../services/api';
import styles from './EditSnippetPage.module.css';

const EditSnippetPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [snippet, setSnippet] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isFetching, setIsFetching] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchSnippet = async () => {
      try {
        const data = await getSnippet(id);
        setSnippet(data);
      } catch (err) {
        console.error('Error fetching snippet:', err);
        setError('Failed to load snippet. Please try again.');
      } finally {
        setIsFetching(false);
      }
    };

    fetchSnippet();
  }, [id]);

  const handleSubmit = async (formData) => {
    setIsLoading(true);
    setError(null);

    try {
      await updateSnippet(id, formData);
      // Navigate back to the snippet detail page
      navigate(`/snippets/${id}`);
    } catch (err) {
      console.error('Error updating snippet:', err);
      setError(err.response?.data?.message || 'Failed to update snippet. Please try again.');
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    navigate(`/snippets/${id}`);
  };

  if (isFetching) {
    return (
      <div className={styles.editSnippetPage}>
        <div className={styles.loading}>Loading snippet...</div>
      </div>
    );
  }

  if (error && !snippet) {
    return (
      <div className={styles.editSnippetPage}>
        <div className={styles.errorAlert}>
          <span className={styles.errorIcon}>⚠️</span>
          <span>{error}</span>
        </div>
        <button onClick={() => navigate('/dashboard')} className={styles.backButton}>
          Back to Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className={styles.editSnippetPage}>
      <div className={styles.header}>
        <h1 className={styles.title}>Edit Snippet</h1>
        <p className={styles.subtitle}>
          Update your snippet details and content
        </p>
      </div>

      {error && (
        <div className={styles.errorAlert}>
          <span className={styles.errorIcon}>⚠️</span>
          <span>{error}</span>
        </div>
      )}

      {snippet && (
        <SnippetEditor
          initialData={snippet}
          onSubmit={handleSubmit}
          onCancel={handleCancel}
          isLoading={isLoading}
        />
      )}
    </div>
  );
};

export default EditSnippetPage;
