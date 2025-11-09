import React from 'react';
import RegisterForm from '../components/auth/RegisterForm';
import ProtectedRoute from '../components/common/ProtectedRoute';

const RegisterPage = () => {
  return (
    <ProtectedRoute requireAuth={false}>
      <RegisterForm />
    </ProtectedRoute>
  );
};

export default RegisterPage;