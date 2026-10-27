import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import client from '../api/client';
import { saveGymCode, saveToken, saveUser, saveDbName, clearStorage, getGymCode, getToken, getUser } from '../storage';
import { stringValue } from '../utils/typeUtils';

type AuthState = {
  gymCode: string | null;
  token: string | null;
  user: any | null;
  loading: boolean;
  error: string | null;
  validGym: boolean;
  restoring: boolean;
};

const initialState: AuthState = {
  gymCode: null,
  token: null,
  user: null,
  loading: false,
  error: null,
  validGym: false,
  restoring: true,
};

// App khulte hi storage se purana login wapas lao (auto-login)
export const restoreSession = createAsyncThunk('auth/restoreSession', async () => {
  const [token, gymCode, user] = await Promise.all([getToken(), getGymCode(), getUser()]);
  return { token: stringValue(token) || null, gymCode: stringValue(gymCode) || null, user };
});

export const validateGym = createAsyncThunk('auth/validateGym', async (gymCode: string, { rejectWithValue }) => {
  try {
    const res = await client.get('/rest/auth/validateGym', { params: { gym_code: gymCode } });
    // backend returns {success:true, data:{valid:true, db_name...}}
    if (res.data?.success === false) throw new Error(res.data?.message || 'Invalid gym code');
    const data = res.data?.data || res.data;
    if (data?.valid === false) throw new Error('Invalid gym code');
    await saveGymCode(stringValue(gymCode).toUpperCase());
    if (data?.db_name) await saveDbName(stringValue(data.db_name));
    return data;
  } catch (e: any) {
    return rejectWithValue(e?.response?.data?.message || e.message || 'Invalid gym code');
  }
});

export const login = createAsyncThunk('auth/login', async ({ gymCode, username, password }: any, { rejectWithValue }) => {
  try {
    const res = await client.post('/rest/auth/login', null, { params: { gym_code: gymCode, username, password } });
    if (res.data?.success === false) throw new Error(res.data?.message);
    const data = res.data?.data || res.data;
    await saveToken(stringValue(data.token));
    await saveUser(data);
    await saveGymCode(stringValue(gymCode).toUpperCase());
    return data;
  } catch (e: any) {
    return rejectWithValue(e?.response?.data?.message || e.message || 'Login failed');
  }
});

// Storage saaf karke Redux se bhi logout (token null hote hi navigator Gym Code screen dikhata hai)
export const logoutUser = createAsyncThunk('auth/logoutUser', async () => {
  await clearStorage();
});

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setGymCode: (state, action) => {
      state.gymCode = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(restoreSession.fulfilled, (s, a) => {
        s.restoring = false;
        s.token = a.payload.token;
        s.gymCode = a.payload.gymCode;
        s.user = a.payload.user;
      })
      .addCase(restoreSession.rejected, (s) => { s.restoring = false; })
      .addCase(validateGym.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(validateGym.fulfilled, (s, a) => { s.loading = false; s.validGym = true; s.gymCode = a.payload?.gym_code || s.gymCode; })
      .addCase(validateGym.rejected, (s, a) => { s.loading = false; s.error = a.payload as string; s.validGym = false; })
      .addCase(login.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(login.fulfilled, (s, a) => { s.loading = false; s.token = a.payload?.token; s.user = a.payload; })
      .addCase(login.rejected, (s, a) => { s.loading = false; s.error = a.payload as string; })
      .addCase(logoutUser.fulfilled, (s) => {
        s.token = null;
        s.user = null;
        s.gymCode = null;
        s.validGym = false;
        s.error = null;
      });
  },
});

export const { setGymCode } = authSlice.actions;
export default authSlice.reducer;
