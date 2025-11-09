import axios from 'axios';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Store reference to auth context refresh function
let refreshTokenFunction = null;

// Function to set the refresh token function from AuthContext
export const setRefreshTokenFunction = (refreshFn) => {
  refreshTokenFunction = refreshFn;
};

// Response interceptor to handle common errors and token refresh
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    if (error.response) {
      // Handle different HTTP status codes
      switch (error.response.status) {
        case 401:
          // Unauthorized - try to refresh token if not already tried
          if (!originalRequest._retry && refreshTokenFunction) {
            originalRequest._retry = true;
            
            try {
              const newToken = await refreshTokenFunction();
              originalRequest.headers.Authorization = `Bearer ${newToken}`;
              return api(originalRequest);
            } catch (refreshError) {
              // Refresh failed, redirect to login
              localStorage.removeItem('token');
              localStorage.removeItem('refreshToken');
              window.location.href = '/login';
              return Promise.reject(refreshError);
            }
          } else {
            // No refresh function or already tried, redirect to login
            localStorage.removeItem('token');
            localStorage.removeItem('refreshToken');
            window.location.href = '/login';
          }
          break;
        case 403:
          // Forbidden
          console.error('Access forbidden');
          break;
        case 404:
          // Not found
          console.error('Resource not found');
          break;
        case 500:
          // Server error
          console.error('Internal server error');
          break;
        default:
          console.error('API Error:', error.response.data);
      }
    } else if (error.request) {
      // Network error
      console.error('Network error:', error.message);
    } else {
      // Other error
      console.error('Error:', error.message);
    }
    
    return Promise.reject(error);
  }
);

// API methods
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  refreshToken: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  logout: () => api.post('/auth/logout')
};

export const snippetAPI = {
  getSnippets: (params) => api.get('/snippets', { params }),
  getSnippet: (id) => api.get(`/snippets/${id}`),
  createSnippet: (snippetData) => api.post('/snippets', snippetData),
  updateSnippet: (id, snippetData) => api.put(`/snippets/${id}`, snippetData),
  deleteSnippet: (id) => api.delete(`/snippets/${id}`),
  searchSnippets: (query, params) => api.get('/snippets/search', { params: { q: query, ...params } })
};

export const userAPI = {
  getProfile: () => api.get('/users/profile'),
  updateProfile: (profileData) => api.put('/users/profile', profileData),
  getUserSnippets: (username, params) => api.get(`/users/${username}/snippets`, { params })
};

// Convenience functions that extract data from responses
export const login = async (credentials) => {
  const response = await authAPI.login(credentials);
  return response.data;
};

export const register = async (userData) => {
  const response = await authAPI.register(userData);
  return response.data;
};

export const getSnippets = async (params) => {
  const response = await snippetAPI.getSnippets(params);
  return response.data;
};

export const getSnippet = async (id) => {
  const response = await snippetAPI.getSnippet(id);
  return response.data;
};

export const createSnippet = async (snippetData) => {
  const response = await snippetAPI.createSnippet(snippetData);
  return response.data;
};

export const updateSnippet = async (id, snippetData) => {
  const response = await snippetAPI.updateSnippet(id, snippetData);
  return response.data;
};

export const deleteSnippet = async (id) => {
  const response = await snippetAPI.deleteSnippet(id);
  return response.data;
};

export const searchSnippets = async (query, params) => {
  const response = await snippetAPI.searchSnippets(query, params);
  return response.data;
};

export const getProfile = async () => {
  const response = await userAPI.getProfile();
  return response.data;
};

export const updateProfile = async (profileData) => {
  const response = await userAPI.updateProfile(profileData);
  return response.data;
};

export const getUserSnippets = async (username, params) => {
  const response = await userAPI.getUserSnippets(username, params);
  return response.data;
};

export default api;