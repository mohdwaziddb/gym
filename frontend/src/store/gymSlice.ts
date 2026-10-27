import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import client from '../api/client';
import { stringValue } from '../utils/typeUtils';
import { logoutUser } from './authSlice';

type GymState = {
  list: any[];
  loading: boolean;
  error: string | null;
};

const initialState: GymState = {
  list: [],
  loading: false,
  error: null,
};

export const fetchGyms = createAsyncThunk('gym/fetch', async (_, { rejectWithValue }) => {
  try {
    const res = await client.get('/rest/gym/list');
    // backend returns {success, message, data:{data_array, result_size}}
    if (res.data?.success === false) throw new Error(res.data?.message);
    const data = res.data?.data || res.data;
    const arr = data?.data_array || [];
    return Array.isArray(arr) ? arr : [];
  } catch (e: any) {
    return rejectWithValue(e?.response?.data?.message || e.message);
  }
});

export const saveGym = createAsyncThunk('gym/save', async (payload: any, { rejectWithValue }) => {
  try {
    const res = await client.post('/rest/gym/save', null, { params: payload });
    if (res.data?.success === false) throw new Error(res.data?.message);
    return res.data;
  } catch (e: any) {
    return rejectWithValue(e?.response?.data?.message || e.message);
  }
});

export const deleteGym = createAsyncThunk('gym/delete', async (id: number, { rejectWithValue }) => {
  try {
    const res = await client.post('/rest/gym/delete', null, { params: { id } });
    if (res.data?.success === false) throw new Error(res.data?.message);
    return id;
  } catch (e: any) {
    return rejectWithValue(e?.response?.data?.message || e.message);
  }
});

const gymSlice = createSlice({
  name: 'gym',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchGyms.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(fetchGyms.fulfilled, (s, a) => { s.loading = false; s.list = a.payload; })
      .addCase(fetchGyms.rejected, (s, a) => { s.loading = false; s.error = a.payload as string; })
      .addCase(saveGym.pending, (s) => { s.loading = true; })
      .addCase(saveGym.fulfilled, (s) => { s.loading = false; })
      .addCase(saveGym.rejected, (s, a) => { s.loading = false; s.error = a.payload as string; })
      .addCase(deleteGym.fulfilled, (s, a) => { s.list = s.list.filter((x) => stringValue(x.id) !== stringValue(a.payload)); })
      .addCase(deleteGym.rejected, (s, a) => { s.error = a.payload as string; })
      // logout pe purane gym ki list saaf (agla gym login kare to pichla data na dikhe)
      .addCase(logoutUser.fulfilled, () => initialState);
  },
});

export default gymSlice.reducer;
