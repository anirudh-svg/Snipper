import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './SnippetCard.module.css';

const SnippetCard = ({ snippet }) => {
  const navigate = useNavigate();

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
    if (diffDays < 365) return `${Math.floor(diffDays / 30)} months ago`;
    return `${Math.floor(diffDays / 365)} years ago`;
  };

  const truncateContent = (content, maxLength = 150) => {
    if (content.length <= maxLength) return content;
    return content.substring(0, maxLength) + '...';
  };

  const handleClick = () => {
    navigate(`/snippets/${snippet.id}`);
  };

  return (
    <div className={styles.snippetCard} onClick={handleClick}>
      <div className={styles.header}>
        <h3 className={styles.title}>{snippet.title}</h3>
        <span className={`${styles.visibilityBadge} ${styles[snippet.visibility.toLowerCase()]}`}>
          {snippet.visibility}
        </span>
      </div>

      {snippet.description && (
        <p className={styles.description}>
          {truncateContent(snippet.description)}
        </p>
      )}

      <div className={styles.preview}>
        <code className={styles.code}>
          {truncateContent(snippet.content, 100)}
        </code>
      </div>

      <div className={styles.footer}>
        <div className={styles.metadata}>
          <span className={styles.language}>
            {snippet.language.charAt(0).toUpperCase() + snippet.language.slice(1)}
          </span>
          <span className={styles.separator}>•</span>
          <span className={styles.author}>{snippet.authorUsername}</span>
          <span className={styles.separator}>•</span>
          <span className={styles.date}>{formatDate(snippet.createdAt)}</span>
        </div>

        {snippet.tags && snippet.tags.length > 0 && (
          <div className={styles.tags}>
            {snippet.tags.slice(0, 3).map((tag, index) => (
              <span key={index} className={styles.tag}>
                {tag}
              </span>
            ))}
            {snippet.tags.length > 3 && (
              <span className={styles.moreTag}>+{snippet.tags.length - 3}</span>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default SnippetCard;
