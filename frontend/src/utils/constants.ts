/**
 * constants.ts - App wide constants (relative, no hardcode D:\)
 * Local (IS_ONLINE=false): Backend 7007, Frontend 7008
 * Live  (IS_ONLINE=true):  https://api.fitmanage.com
 * Mobile auto-detects LAN IP for physical device via Expo hostUri.
 */
import { Platform } from 'react-native';
import Constants from 'expo-constants';

const getLanHost = (): string | null => {
  try {
    // Expo Go hostUri like "192.168.0.107:7008"
    const hostUri =
      (Constants.expoConfig as any)?.hostUri ||
      (Constants.manifest as any)?.hostUri ||
      (Constants.manifest2 as any)?.extra?.expoGo?.debuggerHost;
    if (hostUri && typeof hostUri === 'string') {
      const host = hostUri.split(':')[0];
      // host may be like "192.168.0.107"
      if (host && host !== '127.0.0.1' && host !== 'localhost') return host;
    }
  } catch {}
  return null;
};

export const API_CONFIG = {
  // Live URL - change when backend deployed
  LIVE_URL: 'https://api.fitmanage.com',
  // Local backend port
  LOCAL_PORT: 7007,
  get BASE_URL() {
    if (!__DEV__) return API_CONFIG.LIVE_URL;
    // Physical device via Expo Go -> use LAN IP from hostUri (e.g., 192.168.0.107:7008 -> 192.168.0.107:7007)
    const lanHost = getLanHost();
    if (lanHost) return `http://${lanHost}:${API_CONFIG.LOCAL_PORT}`;
    // Fallback: emulator / simulator
    if (Platform.OS === 'android') return `http://10.0.2.2:${API_CONFIG.LOCAL_PORT}`;
    return `http://localhost:${API_CONFIG.LOCAL_PORT}`;
  },
  TIMEOUT: 10000,
};

export const STORAGE_KEYS = {
  GYM_CODE: 'gym_code',
  TOKEN: 'token',
  USER: 'user',
  DB_NAME: 'db_name',
};

export const APP_COLORS = {
  primary: '#006633',
  secondary: '#009933',
  danger: '#d32f2f',
  background: '#f5f5f5',
  text: '#000000',
  white: '#ffffff',
  grey: '#9e9e9e',
};
