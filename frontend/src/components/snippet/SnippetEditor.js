import React, { useState, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import styles from './SnippetEditor.module.css';

const LANGUAGES = [
  'javascript', 'typescript', 'python', 'java', 'csharp', 'cpp', 'c',
  'go', 'rust', 'php', 'ruby', 'swift', 'kotlin', 'scala',
  'html', 'css', 'sql', 'bash', 'powershell', 'yaml', 'json', 'xml',
  'markdown', 'plaintext'
];

const SnippetEditor = ({ initialData = {}, onSubmit, onCancel, isLoading = false }) => {
  const [formData, setFormData] = useState({
    title: initialData?.title || '',
    description: initialData?.description || '',
    content: initialData?.content || '',
    language: initialData?.language || 'javascript',
    visibility: initialData?.visibility || 'PUBLIC',
    tags: initialData?.tags ? initialData.tags.join(', ') : ''
  });

  const [showPreview, setShowPreview] = useState(false);
  const [errors, setErrors] = useState({});

  // Debug log
  useEffect(() => {
    console.log('SnippetEditor rendered with formData:', formData);
  }, [formData]);

  useEffect(() => {
    if (initialData) {
      setFormData({
        title: initialData.title || '',
        description: initialData.description || '',
        content: initialData.content || '',
        language: initialData.language || 'javascript',
        visibility: initialData.visibility || 'PUBLIC',
        tags: initialData.tags ? initialData.tags.join(', ') : ''
      });
    }
  }, [initialData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    console.log('handleChange called:', name, value); // Debug log
    setFormData(prev => {
      const newData = {
        ...prev,
        [name]: value
      };
      console.log('New form data:', newData); // Debug log
      return newData;
    });
    // Clear error for this field
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: null
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.title.trim()) {
      newErrors.title = 'Title is required';
    } else if (formData.title.length > 255) {
      newErrors.title = 'Title must be less than 255 characters';
    }

    if (!formData.content.trim()) {
      newErrors.content = 'Content is required';
    }

    if (!formData.language) {
      newErrors.language = 'Language is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const submitData = {
      ...formData,
      tags: formData.tags
        .split(',')
        .map(tag => tag.trim())
        .filter(tag => tag.length > 0)
    };

    onSubmit(submitData);
  };

  return (
    <form className={styles.editorForm} onSubmit={handleSubmit}>
      <div className={styles.formGroup}>
        <label htmlFor="title" className={styles.label}>
          Title <span className={styles.required}>*</span>
        </label>
        <input
          type="text"
          id="title"
          name="title"
          value={formData.title}
          onChange={handleChange}
          onInput={handleChange}
          className={`${styles.input} ${errors.title ? styles.inputError : ''}`}
          placeholder="Enter snippet title"
          disabled={isLoading}
        />
        {errors.title && <span className={styles.errorText}>{errors.title}</span>}
      </div>

      <div className={styles.formRow}>
        <div className={styles.formGroup}>
          <label htmlFor="language" className={styles.label}>
            Language <span className={styles.required}>*</span>
          </label>
          <select
            id="language"
            name="language"
            value={formData.language}
            onChange={handleChange}
            className={`${styles.select} ${errors.language ? styles.inputError : ''}`}
            disabled={isLoading}
          >
            {LANGUAGES.map(lang => (
              <option key={lang} value={lang}>
                {lang.charAt(0).toUpperCase() + lang.slice(1)}
              </option>
            ))}
          </select>
          {errors.language && <span className={styles.errorText}>{errors.language}</span>}
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="visibility" className={styles.label}>
            Visibility
          </label>
          <select
            id="visibility"
            name="visibility"
            value={formData.visibility}
            onChange={handleChange}
            className={styles.select}
            disabled={isLoading}
          >
            <option value="PUBLIC">Public</option>
            <option value="PRIVATE">Private</option>
          </select>
        </div>
      </div>

      <div className={styles.formGroup}>
        <label htmlFor="tags" className={styles.label}>
          Tags
        </label>
        <input
          type="text"
          id="tags"
          name="tags"
          value={formData.tags}
          onChange={handleChange}
          onInput={handleChange}
          className={styles.input}
          placeholder="Enter tags separated by commas (e.g., react, hooks, tutorial)"
          disabled={isLoading}
        />
        <span className={styles.helpText}>Separate tags with commas</span>
      </div>

      <div className={styles.formGroup}>
        <label htmlFor="description" className={styles.label}>
          Description
        </label>
        <textarea
          id="description"
          name="description"
          value={formData.description}
          onChange={handleChange}
          onInput={handleChange}
          className={styles.textarea}
          placeholder="Enter a brief description (supports Markdown)"
          rows="4"
          disabled={isLoading}
        />
      </div>

      <div className={styles.formGroup}>
        <div className={styles.editorHeader}>
          <label htmlFor="content" className={styles.label}>
            Code <span className={styles.required}>*</span>
          </label>
          <button
            type="button"
            className={styles.previewToggle}
            onClick={() => setShowPreview(!showPreview)}
            disabled={isLoading}
          >
            {showPreview ? 'Edit' : 'Preview'}
          </button>
        </div>

        {showPreview ? (
          <div className={styles.preview}>
            <div className={styles.previewContent}>
              <ReactMarkdown>{formData.content || '*No content to preview*'}</ReactMarkdown>
            </div>
          </div>
        ) : (
          <textarea
            id="content"
            name="content"
            value={formData.content}
            onChange={handleChange}
            onInput={handleChange}
            className={`${styles.codeTextarea} ${errors.content ? styles.inputError : ''}`}
            placeholder="Enter your code here..."
            rows="20"
            disabled={isLoading}
          />
        )}
        {errors.content && <span className={styles.errorText}>{errors.content}</span>}
      </div>

      <div className={styles.formActions}>
        <button
          type="submit"
          className={styles.submitButton}
          disabled={isLoading}
        >
          {isLoading ? 'Saving...' : 'Save Snippet'}
        </button>
        {onCancel && (
          <button
            type="button"
            className={styles.cancelButton}
            onClick={onCancel}
            disabled={isLoading}
          >
            Cancel
          </button>
        )}
      </div>
    </form>
  );
};

export default SnippetEditor;
