import { useAppSelector } from './hooks';
import { stringValue } from '../utils/typeUtils';

/**
 * Login wale user ka role. UI me sirf button chhupane ke liye -
 * asli permission check backend (AuthContext.isManager) karta hai.
 */
export const useRole = () => {
  const user = useAppSelector((s) => s.auth.user);
  const role = stringValue(user?.role).toUpperCase();
  return {
    role,
    username: stringValue(user?.username),
    gymCode: stringValue(user?.gym_code),
    isManager: role === 'OWNER' || role === 'ADMIN',
  };
};
