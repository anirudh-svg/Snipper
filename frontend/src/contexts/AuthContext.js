import React, { createContext, useContext, useReducer, useEffect } from 'react';
import { authAPI } from '../services/api';

// Initial state
const initialState = {
  user: null,
  isAuthenticated: false,
  loading: true,
  error: null,
  token: null,
  refreshToken: null
};

// Action types
export const AuthActionTypes = {
  SET_LOADING: 'SET_LOADING',
  SET_ERROR: 'SET_ERROR',
  LOGIN_SUCCESS: 'LOGIN_SUCCESS',
  LOGOUT: 'LOGOUT',
  CLEAR_ERROR: 'CLEAR_ERROR',
  TOKEN_REFRESH_SUCCESS: 'TOKEN_REFRESH_SUCCESS'
};

// Reducer function
const authReducer = (state, action) => {
  switch (action.type) {
    case AuthActionTypes.SET_LOADING:
      return {
        ...state,
        loading: action.payload
      };
    case AuthActionTypes.SET_ERROR:
      return {
        ...state,
        error: action.payload,
        loading: false
      };
    case AuthActionTypes.LOGIN_SUCCESS:
      return {
        ...state,
        user: action.payload.user,
        token: action.payload.token,
        refreshToken: action.payload.refreshToken,
        isAuthenticated: true,
        loading: false,
        error: null
      };
    case AuthActionTypes.TOKEN_REFRESH_SUCCESS:
      return {
        ...state,
        token: action.payload.token,
        refreshToken: action.payload.refreshToken,
        loading: false,
        error: null
      };
    case AuthActionTypes.LOGOUT:
      return {
        ...state,
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        loading: false,
        error: null
      };
    case AuthActionTypes.CLEAR_ERROR:
      return {
        ...state,
        error: null
      };
    default:
      return state;
  }
};

// Create context
const AuthContext = createContext();

// Helper function to decode JWT token
const decodeToken = (token) => {
  try {
    if (!token || typeof token !== 'string') {
      return null;
    }
    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }
    const payload = JSON.parse(atob(parts[1]));
    return payload;
  } catch (error) {
    console.error('Token decode error:', error);
    return null;
  }
};

// Helper function to check if token is expired
const isTokenExpired = (token) => {
  const decoded = decodeToken(token);
  if (!decoded) return true;
  
  const currentTime = Date.now() / 1000;
  return decoded.exp < currentTime;
};

// Context provider component
export const AuthProvider = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // Initialize authentication state on app load
  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('token');
      const refreshToken = localStorage.getItem('refreshToken');

      if (token && refreshToken) {
        if (isTokenExpired(token)) {
          // Try to refresh the token
          try {
            await refreshAuthToken();
          } catch (error) {
            // Refresh failed, clear tokens
            clearTokens();
            dispatch({ type: AuthActionTypes.SET_LOADING, payload: false });
          }
        } else {
          // Token is still valid
          const decoded = decodeToken(token);
          if (decoded) {
            dispatch({
              type: AuthActionTypes.LOGIN_SUCCESS,
              payload: {
                user: {
                  username: decoded.sub  // JWT only contains username in 'sub' field
                },
                token,
                refreshToken
              }
            });
          }
          dispatch({ type: AuthActionTypes.SET_LOADING, payload: false });
        }
      } else {
        dispatch({ type: AuthActionTypes.SET_LOADING, payload: false });
      }
    };

    initializeAuth();
  }, []);

  // Helper function to store tokens
  const storeTokens = (token, refreshToken) => {
    localStorage.setItem('token', token);
    localStorage.setItem('refreshToken', refreshToken);
  };

  // Helper function to clear tokens
  const clearTokens = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
  };

  // Login function
  const login = async (credentials) => {
    try {
      dispatch({ type: AuthActionTypes.SET_LOADING, payload: true });
      dispatch({ type: AuthActionTypes.CLEAR_ERROR });

      const response = await authAPI.login(credentials);
      const { token, refreshToken, user } = response.data;

      storeTokens(token, refreshToken);

      dispatch({
        type: AuthActionTypes.LOGIN_SUCCESS,
        payload: { user, token, refreshToken }
      });

      return { success: true };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Login failed';
      dispatch({ type: AuthActionTypes.SET_ERROR, payload: errorMessage });
      return { success: false, error: errorMessage };
    }
  };

  // Register function
  const register = async (userData) => {
    try {
      dispatch({ type: AuthActionTypes.SET_LOADING, payload: true });
      dispatch({ type: AuthActionTypes.CLEAR_ERROR });

      const response = await authAPI.register(userData);
      const { token, refreshToken, user } = response.data;

      storeTokens(token, refreshToken);

      dispatch({
        type: AuthActionTypes.LOGIN_SUCCESS,
        payload: { user, token, refreshToken }
      });

      return { success: true };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Registration failed';
      dispatch({ type: AuthActionTypes.SET_ERROR, payload: errorMessage });
      return { success: false, error: errorMessage };
    }
  };

  // Refresh token function
  const refreshAuthToken = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await authAPI.refreshToken(refreshToken);
      const { token: newToken, refreshToken: newRefreshToken } = response.data;

      storeTokens(newToken, newRefreshToken);

      dispatch({
        type: AuthActionTypes.TOKEN_REFRESH_SUCCESS,
        payload: { token: newToken, refreshToken: newRefreshToken }
      });

      return newToken;
    } catch (error) {
      clearTokens();
      dispatch({ type: AuthActionTypes.LOGOUT });
      throw error;
    }
  };

  // Logout function
  const logout = async () => {
    try {
      await authAPI.logout();
    } catch (error) {
      // Even if logout API fails, we should clear local state
      console.error('Logout API error:', error);
    } finally {
      clearTokens();
      dispatch({ type: AuthActionTypes.LOGOUT });
    }
  };

  // Clear error function
  const clearError = () => {
    dispatch({ type: AuthActionTypes.CLEAR_ERROR });
  };

  const value = {
    ...state,
    login,
    register,
    logout,
    refreshAuthToken,
    clearError
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

// Custom hook to use the auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;