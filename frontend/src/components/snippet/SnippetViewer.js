import React, { useEffect, useRef } from 'react';
import ReactMarkdown from 'react-markdown';
import styles from './SnippetViewer.module.css';

const SnippetViewer = ({ snippet, onEdit, onDelete, isOwner = false }) => {
  const codeRef = useRef(null);

  useEffect(() => {
    // Dynamically import Prism only when needed
    if (snippet && snippet.content && codeRef.current) {
      import('prismjs').then((Prism) => {
        // Import theme
        import('prismjs/themes/prism-tomorrow.css');
        
        // Import language support based on snippet language
        const languageMap = {
          'javascript': 'prism-javascript',
          'typescript': 'prism-typescript',
          'python': 'prism-python',
          'java': 'prism-java',
          'csharp': 'prism-csharp',
          'cpp': 'prism-cpp',
          'c': 'prism-c',
          'go': 'prism-go',
          'rust': 'prism-rust',
          'php': 'prism-php',
          'ruby': 'prism-ruby',
          'swift': 'prism-swift',
          'kotlin': 'prism-kotlin',
          'scala': 'prism-scala',
          'css': 'prism-css',
          'sql': 'prism-sql',
          'bash': 'prism-bash',
          'powershell': 'prism-powershell',
          'yaml': 'prism-yaml',
          'json': 'prism-json',
          'xml': 'prism-xml-doc',
          'markdown': 'prism-markdown'
        };

        const langComponent = languageMap[snippet.language.toLowerCase()];
        if (langComponent) {
          import(`prismjs/components/${langComponent}`).catch(() => {
            console.warn(`Language ${snippet.language} not supported`);
          });
        }

        // Highlight after a short delay
        setTimeout(() => {
          try {
            if (codeRef.current) {
              Prism.default.highlightElement(codeRef.current);
            }
          } catch (error) {
            console.error('Prism highlighting error:', error);
          }
        }, 100);
      }).catch((error) => {
        console.error('Failed to load Prism:', error);
      });
    }
  }, [snippet]);

  if (!snippet) {
    return null;
  }

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className={styles.snippetViewer}>
      <div className={styles.header}>
        <div className={styles.titleSection}>
          <h1 className={styles.title}>{snippet.title}</h1>
          <div className={styles.metadata}>
            <span className={styles.author}>
              By <strong>{snippet.authorUsername || 'Unknown'}</strong>
            </span>
            <span className={styles.separator}>•</span>
            <span className={styles.date}>
              {formatDate(snippet.createdAt)}
            </span>
            {snippet.updatedAt && snippet.updatedAt !== snippet.createdAt && (
              <>
                <span className={styles.separator}>•</span>
                <span className={styles.updated}>
                  Updated {formatDate(snippet.updatedAt)}
                </span>
              </>
            )}
          </div>
        </div>

        {isOwner && (
          <div className={styles.actions}>
            <button
              onClick={onEdit}
              className={styles.editButton}
              title="Edit snippet"
            >
              Edit
            </button>
            <button
              onClick={onDelete}
              className={styles.deleteButton}
              title="Delete snippet"
            >
              Delete
            </button>
          </div>
        )}
      </div>

      {snippet.description && (
        <div className={styles.description}>
          <ReactMarkdown>{snippet.description}</ReactMarkdown>
        </div>
      )}

      <div className={styles.metaInfo}>
        <div className={styles.metaItem}>
          <span className={styles.metaLabel}>Language:</span>
          <span className={styles.languageBadge}>
            {snippet.language.charAt(0).toUpperCase() + snippet.language.slice(1)}
          </span>
        </div>

        <div className={styles.metaItem}>
          <span className={styles.metaLabel}>Visibility:</span>
          <span className={`${styles.visibilityBadge} ${styles[snippet.visibility.toLowerCase()]}`}>
            {snippet.visibility}
          </span>
        </div>

        {snippet.tags && snippet.tags.length > 0 && (
          <div className={styles.metaItem}>
            <span className={styles.metaLabel}>Tags:</span>
            <div className={styles.tags}>
              {snippet.tags.map((tag, index) => (
                <span key={index} className={styles.tag}>
                  {tag}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className={styles.codeSection}>
        <div className={styles.codeHeader}>
          <span className={styles.codeTitle}>Code</span>
          <button
            onClick={() => {
              navigator.clipboard.writeText(snippet.content);
            }}
            className={styles.copyButton}
            title="Copy to clipboard"
          >
            Copy
          </button>
        </div>
        <pre className={styles.codeBlock}>
          <code ref={codeRef} className={`language-${snippet.language}`}>
            {snippet.content}
          </code>
        </pre>
      </div>
    </div>
  );
};

export default SnippetViewer;
