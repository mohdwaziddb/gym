import AsyncStorage from '@react-native-async-storage/async-storage';
import { STORAGE_KEYS } from '../utils/constants';

export const saveGymCode = async (code: string) => AsyncStorage.setItem(STORAGE_KEYS.GYM_CODE, code);
export const getGymCode = async (): Promise<string | null> => AsyncStorage.getItem(STORAGE_KEYS.GYM_CODE);

export const saveToken = async (token: string) => AsyncStorage.setItem(STORAGE_KEYS.TOKEN, token);
export const getToken = async (): Promise<string | null> => AsyncStorage.getItem(STORAGE_KEYS.TOKEN);

export const saveUser = async (user: any) => AsyncStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
export const getUser = async (): Promise<any | null> => {
  const v = await AsyncStorage.getItem(STORAGE_KEYS.USER);
  return v ? JSON.parse(v) : null;
};

export const saveDbName = async (db: string) => AsyncStorage.setItem(STORAGE_KEYS.DB_NAME, db);

export const clearStorage = async () => {
  await Promise.all([
    AsyncStorage.removeItem(STORAGE_KEYS.GYM_CODE),
    AsyncStorage.removeItem(STORAGE_KEYS.TOKEN),
    AsyncStorage.removeItem(STORAGE_KEYS.USER),
    AsyncStorage.removeItem(STORAGE_KEYS.DB_NAME),
  ]);
};
