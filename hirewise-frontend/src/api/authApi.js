import axiosClient from './axiosClient';

export const authApi = {
  /**
   * Authenticate user with email and password
   * Returns: { accessToken, tokenType, expiresIn, user: { id, fullName, email, role } }
   */
  login: async (credentials) => {
    const response = await axiosClient.post('/auth/login', credentials);
    return response.data;
  },

  /**
   * Register a new job seeker user
   * Returns: UserResponse { id, fullName, email, role, createdAt }
   */
  register: async (userData) => {
    const response = await axiosClient.post('/users', userData);
    return response.data;
  },

  /**
   * Check backend server health status
   */
  checkHealth: async () => {
    const response = await axiosClient.get('/health');
    return response.data;
  }
};
