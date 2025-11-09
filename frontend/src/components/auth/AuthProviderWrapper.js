import React, { useEffect } from 'react';
import { AuthProvider, useAuth } from '../../contexts/AuthContext';
import { setRefreshTokenFunction } from '../../services/api';

// Inner component that has access to auth context
const AuthProviderSetup = ({ children }) => {
  const { refreshAuthToken } = useAuth();

  useEffect(() => {
    // Set the refresh token function in the API service
    setRefreshTokenFunction(refreshAuthToken);
  }, [refreshAuthToken]);

  return children;
};

// Wrapper component that provides auth context and sets up API integration
const AuthProviderWrapper = ({ children }) => {
  return (
    <AuthProvider>
      <AuthProviderSetup>
        {children}
      </AuthProviderSetup>
    </AuthProvider>
  );
};

export default AuthProviderWrapper;