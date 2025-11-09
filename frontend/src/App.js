import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AppProvider } from './contexts/AppContext';
import AuthProviderWrapper from './components/auth/AuthProviderWrapper';
import Layout from './components/layout/Layout';
import ProtectedRoute from './components/common/ProtectedRoute';

// Pages
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import CreateSnippetPage from './pages/CreateSnippetPage';
import EditSnippetPage from './pages/EditSnippetPage';
import SnippetDetailPage from './pages/SnippetDetailPage';
import ExplorePage from './pages/ExplorePage';
import ProfilePage from './pages/ProfilePage';

import './App.css';

function App() {
  return (
    <AppProvider>
      <AuthProviderWrapper>
        <Router>
          <Routes>
          {/* Public routes */}
          <Route 
            path="/" 
            element={
              <Layout>
                <HomePage />
              </Layout>
            } 
          />
          
          {/* Auth routes (redirect to dashboard if already authenticated) */}
          <Route 
            path="/login" 
            element={<LoginPage />} 
          />
          
          <Route 
            path="/register" 
            element={<RegisterPage />} 
          />

          {/* Protected routes (require authentication) */}
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute requireAuth={true}>
                <Layout>
                  <DashboardPage />
                </Layout>
              </ProtectedRoute>
            } 
          />

          <Route 
            path="/explore" 
            element={
              <Layout>
                <ExplorePage />
              </Layout>
            } 
          />

          <Route 
            path="/snippets/new" 
            element={
              <ProtectedRoute requireAuth={true}>
                <Layout>
                  <CreateSnippetPage />
                </Layout>
              </ProtectedRoute>
            } 
          />

          <Route 
            path="/snippets/:id/edit" 
            element={
              <ProtectedRoute requireAuth={true}>
                <Layout>
                  <EditSnippetPage />
                </Layout>
              </ProtectedRoute>
            } 
          />

          <Route 
            path="/snippets/:id" 
            element={
              <Layout>
                <SnippetDetailPage />
              </Layout>
            } 
          />

          <Route 
            path="/profile" 
            element={
              <ProtectedRoute requireAuth={true}>
                <Layout>
                  <ProfilePage />
                </Layout>
              </ProtectedRoute>
            } 
          />

          {/* 404 route */}
          <Route 
            path="*" 
            element={
              <Layout>
                <div>
                  <h1>Page Not Found</h1>
                  <p>The page you're looking for doesn't exist.</p>
                </div>
              </Layout>
            } 
          />
        </Routes>
        </Router>
      </AuthProviderWrapper>
    </AppProvider>
  );
}

export default App;