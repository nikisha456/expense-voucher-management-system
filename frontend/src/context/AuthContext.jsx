import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { api, getStoredToken, storeToken } from '../api/client';
import { decodeJwt } from '../utils/format';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const onLogout = () => {
      setUser(null);
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign('/login');
      }
    };
    window.addEventListener('auth:logout', onLogout);
    return () => window.removeEventListener('auth:logout', onLogout);
  }, []);

  useEffect(() => {
    const token = getStoredToken();
    if (!token) {
      setLoading(false);
      return;
    }
    const payload = decodeJwt(token);
    if (payload?.exp && payload.exp * 1000 <= Date.now()) {
      storeToken(null);
      setLoading(false);
      return;
    }
    api.get('/api/auth/me')
      .then((response) => setUser(response.data.data))
      .catch(() => {
        storeToken(null);
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const token = getStoredToken();
    const payload = token ? decodeJwt(token) : null;
    if (!payload?.exp) return undefined;
    const delay = payload.exp * 1000 - Date.now();
    if (delay <= 0) return undefined;
    const timer = setTimeout(() => {
      storeToken(null);
      setUser(null);
      window.location.assign('/login');
    }, delay);
    return () => clearTimeout(timer);
  }, [user]);

  const value = useMemo(() => ({
    user,
    loading,
    async login(email, password) {
      const response = await api.post('/api/auth/login', { email, password });
      storeToken(response.data.data.accessToken);
      setUser(response.data.data.user);
      return response.data.data.user;
    },
    async logout() {
      try {
        await api.post('/api/auth/logout');
      } catch {
        // Stateless logout still clears the local session.
      }
      storeToken(null);
      setUser(null);
    },
    refreshUser(next) {
      setUser(next);
    },
  }), [user, loading]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
