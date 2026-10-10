import React, { createContext, useContext, useState, useEffect } from 'react';
import { STORAGE_KEYS, ROLES } from '../utils/constants';
import { authApi } from '../api/authApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const savedUser = localStorage.getItem(STORAGE_KEYS.USER);
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState(() => {
    return localStorage.getItem(STORAGE_KEYS.TOKEN) || null;
  });

  const [loading, setLoading] = useState(false);

  // Synchronize state when auth expired event is broadcast by Axios
  useEffect(() => {
    const handleAuthExpired = () => {
      setUser(null);
      setToken(null);
    };

    window.addEventListener('hirewise-auth-expired', handleAuthExpired);
    return () => window.removeEventListener('hirewise-auth-expired', handleAuthExpired);
  }, []);

  const login = async (credentials) => {
    setLoading(true);
    try {
      const data = await authApi.login(credentials);
      // data format: { accessToken, tokenType, expiresIn, user: { id, fullName, email, role } }
      const accessToken = data.accessToken;
      const userProfile = data.user;

      localStorage.setItem(STORAGE_KEYS.TOKEN, accessToken);
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(userProfile));

      setToken(accessToken);
      setUser(userProfile);

      return userProfile;
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    setLoading(true);
    try {
      return await authApi.register(userData);
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem(STORAGE_KEYS.TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
    setToken(null);
    setUser(null);
  };

  const value = {
    user,
    token,
    isAuthenticated: Boolean(token && user),
    isSeeker: user?.role === ROLES.JOB_SEEKER,
    isRecruiter: user?.role === ROLES.RECRUITER,
    loading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
