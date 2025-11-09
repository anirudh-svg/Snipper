import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import SnippetEditor from '../components/snippet/SnippetEditor';
import { createSnippet } from '../services/api';
import styles from './CreateSnippetPage.module.css';

const CreateSnippetPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (formData) => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await createSnippet(formData);
      // Navigate to the newly created snippet
      navigate(`/snippets/${response.id}`);
    } catch (err) {
      console.error('Error creating snippet:', err);
      setError(err.response?.data?.message || 'Failed to create snippet. Please try again.');
      setIsLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/dashboard');
  };

  return (
    <div className={styles.createSnippetPage}>
      <div className={styles.header}>
        <h1 className={styles.title}>Create New Snippet</h1>
        <p className={styles.subtitle}>
          Share your code with the community or keep it private for your own reference
        </p>
      </div>

      {error && (
        <div className={styles.errorAlert}>
          <span className={styles.errorIcon}>⚠️</span>
          <span>{error}</span>
        </div>
      )}

      <SnippetEditor
        onSubmit={handleSubmit}
        onCancel={handleCancel}
        isLoading={isLoading}
      />
    </div>
  );
};

export default CreateSnippetPage;
