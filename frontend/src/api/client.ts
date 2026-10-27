import axios from 'axios';
import { API_CONFIG } from '../utils/constants';
import { getGymCode, getToken } from '../storage';

const client = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
  headers: { 'Content-Type': 'application/json' },
});

// Token expire / invalid (401) pe kya karna hai - AppNavigator set karta hai (logout -> Gym Code screen)
let onUnauthorized: (() => void) | null = null;
export const setUnauthorizedHandler = (handler: () => void) => {
  onUnauthorized = handler;
};

// Add X-Gym-Code and Authorization on every request (common, like backend RequestInterceptor)
client.interceptors.request.use(async (config) => {
  const gymCode = await getGymCode();
  const token = await getToken();
  if (gymCode) config.headers['X-Gym-Code'] = gymCode;
  if (token) config.headers['Authorization'] = `Bearer ${token}`;
  return config;
});

// Backend AuthInterceptor 401 deta hai jab token galat / expire ho
client.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error?.response?.status === 401 && onUnauthorized) onUnauthorized();
    return Promise.reject(error);
  },
);

export default client;
