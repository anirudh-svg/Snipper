import React from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '../contexts/AppContext';

const HomePage = () => {
  const { state } = useApp();
  const { isAuthenticated } = state;

  return (
    <div style={{ textAlign: 'center', padding: '2rem 0' }}>
      <h1 style={{ fontSize: '3rem', marginBottom: '1rem', color: '#24292f' }}>
        Welcome to Snipper
      </h1>
      <p style={{ fontSize: '1.25rem', color: '#656d76', marginBottom: '2rem', maxWidth: '600px', margin: '0 auto 2rem' }}>
        Create, share, and discover code snippets with the developer community. 
        Build your personal collection of reusable code with Markdown support and syntax highlighting.
      </p>
      
      {!isAuthenticated ? (
        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', marginBottom: '3rem' }}>
          <Link 
            to="/register" 
            style={{
              padding: '0.75rem 2rem',
              backgroundColor: '#1f883d',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '6px',
              fontWeight: '600',
              fontSize: '1.1rem'
            }}
          >
            Get Started
          </Link>
          <Link 
            to="/explore" 
            style={{
              padding: '0.75rem 2rem',
              backgroundColor: 'transparent',
              color: '#24292f',
              textDecoration: 'none',
              borderRadius: '6px',
              fontWeight: '600',
              fontSize: '1.1rem',
              border: '1px solid #d0d7de'
            }}
          >
            Explore Snippets
          </Link>
        </div>
      ) : (
        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', marginBottom: '3rem' }}>
          <Link 
            to="/snippets/new" 
            style={{
              padding: '0.75rem 2rem',
              backgroundColor: '#1f883d',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '6px',
              fontWeight: '600',
              fontSize: '1.1rem'
            }}
          >
            Create Snippet
          </Link>
          <Link 
            to="/dashboard" 
            style={{
              padding: '0.75rem 2rem',
              backgroundColor: 'transparent',
              color: '#24292f',
              textDecoration: 'none',
              borderRadius: '6px',
              fontWeight: '600',
              fontSize: '1.1rem',
              border: '1px solid #d0d7de'
            }}
          >
            My Dashboard
          </Link>
        </div>
      )}

      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', 
        gap: '2rem', 
        maxWidth: '900px', 
        margin: '0 auto' 
      }}>
        <div style={{ 
          padding: '2rem', 
          backgroundColor: '#f6f8fa', 
          borderRadius: '8px',
          textAlign: 'left'
        }}>
          <h3 style={{ color: '#24292f', marginBottom: '1rem' }}>📝 Rich Editor</h3>
          <p style={{ color: '#656d76', lineHeight: '1.6' }}>
            Create snippets with our Markdown-powered editor featuring live preview, 
            syntax highlighting, and support for multiple programming languages.
          </p>
        </div>
        
        <div style={{ 
          padding: '2rem', 
          backgroundColor: '#f6f8fa', 
          borderRadius: '8px',
          textAlign: 'left'
        }}>
          <h3 style={{ color: '#24292f', marginBottom: '1rem' }}>🔍 Discover & Share</h3>
          <p style={{ color: '#656d76', lineHeight: '1.6' }}>
            Explore public snippets from the community, search by language or tags, 
            and share your own code with developers worldwide.
          </p>
        </div>
        
        <div style={{ 
          padding: '2rem', 
          backgroundColor: '#f6f8fa', 
          borderRadius: '8px',
          textAlign: 'left'
        }}>
          <h3 style={{ color: '#24292f', marginBottom: '1rem' }}>🚀 Production Ready</h3>
          <p style={{ color: '#656d76', lineHeight: '1.6' }}>
            Built with modern technologies and containerized for reliable deployment. 
            Secure authentication and scalable architecture.
          </p>
        </div>
      </div>
    </div>
  );
};

export default HomePage;