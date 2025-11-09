import { render, screen } from '@testing-library/react';
import App from './App';

test('renders welcome heading', () => {
  render(<App />);
  const welcomeElement = screen.getByText(/welcome to snipper/i);
  expect(welcomeElement).toBeInTheDocument();
});

test('renders navigation logo', () => {
  render(<App />);
  const logoElement = screen.getByRole('link', { name: /snipper/i });
  expect(logoElement).toBeInTheDocument();
});

test('renders auth buttons for non-authenticated users', () => {
  render(<App />);
  const signInButton = screen.getByRole('link', { name: /sign in/i });
  const signUpButton = screen.getByRole('link', { name: /sign up/i });
  expect(signInButton).toBeInTheDocument();
  expect(signUpButton).toBeInTheDocument();
});