import React from 'react';
import LoginForm from '../components/auth/LoginForm';
import ProtectedRoute from '../components/common/ProtectedRoute';

const LoginPage = () => {
  return (
    <ProtectedRoute requireAuth={false}>
      <LoginForm />
    </ProtectedRoute>
  );
};

export default LoginPage;