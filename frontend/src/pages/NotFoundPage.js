import React from 'react';
import { Link } from 'react-router-dom';

const NotFoundPage = () => {
  return (
    <div style={{ 
      textAlign: 'center', 
      padding: '4rem 2rem',
      maxWidth: '600px',
      margin: '0 auto'
    }}>
      <h1 style={{ 
        fontSize: '6rem', 
        margin: '0', 
        color: '#656d76',
        fontWeight: '300'
      }}>
        404
      </h1>
      <h2 style={{ 
        fontSize: '2rem', 
        marginBottom: '1rem', 
        color: '#24292f' 
      }}>
        Page Not Found
      </h2>
      <p style={{ 
        color: '#656d76', 
        marginBottom: '2rem',
        fontSize: '1.1rem',
        lineHeight: '1.6'
      }}>
        The page you're looking for doesn't exist or has been moved.
      </p>
      <Link 
        to="/" 
        style={{
          display: 'inline-block',
          padding: '0.75rem 2rem',
          backgroundColor: '#1f883d',
          color: 'white',
          textDecoration: 'none',
          borderRadius: '6px',
          fontWeight: '600',
          fontSize: '1rem'
        }}
      >
        Go Home
      </Link>
    </div>
  );
};

export default NotFoundPage;