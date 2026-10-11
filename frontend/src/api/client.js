import axios from 'axios';

const TOKEN_KEY = 'expenseflow_token';

function apiBaseUrl() {
  const configured = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api';
  return configured.replace(/\/+$/, '');
}

export const api = axios.create({
  baseURL: apiBaseUrl(),
});

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function storeToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

api.interceptors.request.use((config) => {
  const base = apiBaseUrl();
  if (typeof config.url === 'string' && !/^https?:\/\//i.test(config.url)) {
    const path = config.url.startsWith('/') ? config.url : `/${config.url}`;
    if (base.endsWith('/api') && (path === '/api' || path.startsWith('/api/'))) {
      config.url = path.slice(4) || '/';
    }
  }
  const token = getStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const url = error.config?.url || '';
    if (error.response?.status === 401 && !url.includes('/auth/login') && !url.includes('/api/auth/login')) {
      storeToken(null);
      window.dispatchEvent(new Event('auth:logout'));
    }
    return Promise.reject(error);
  },
);
