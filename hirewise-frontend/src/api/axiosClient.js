import axios from 'axios';
import { STORAGE_KEYS } from '../utils/constants';

const axiosClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach JWT Bearer Token if available
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Clean error handling and session expiration detection
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response ? error.response.status : null;
    const requestUrl = error.config?.url || '';

    // If 401 Unauthorized occurs on a protected endpoint (excluding login attempt itself)
    if (status === 401 && !requestUrl.includes('/auth/login')) {
      localStorage.removeItem(STORAGE_KEYS.TOKEN);
      localStorage.removeItem(STORAGE_KEYS.USER);
      window.dispatchEvent(new Event('hirewise-auth-expired'));
    }

    // Extract user-friendly error message
    let message = 'An unexpected error occurred. Please try again.';
    if (error.response?.data) {
      const data = error.response.data;
      if (typeof data.message === 'string') {
        message = data.message;
      } else if (typeof data.error === 'string') {
        message = data.error;
      } else if (data.details && typeof data.details === 'object') {
        message = Object.values(data.details).join(', ');
      }
    } else if (error.message) {
      message = error.message;
    }

    const enhancedError = new Error(message);
    enhancedError.status = status;
    enhancedError.originalError = error;
    enhancedError.response = error.response;

    return Promise.reject(enhancedError);
  }
);

export default axiosClient;
