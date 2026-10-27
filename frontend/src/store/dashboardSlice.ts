import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import client from '../api/client';
import { logoutUser } from './authSlice';
import type { DashboardSummary } from '../types';

type DashboardState = {
  summary: DashboardSummary | null;
  loading: boolean;
  error: string | null;
};

const initialState: DashboardState = {
  summary: null,
  loading: false,
  error: null,
};

export const fetchDashboard = createAsyncThunk('dashboard/fetch', async (_, { rejectWithValue }) => {
  try {
    const res = await client.get('/rest/dashboard/summary');
    if (res.data?.success === false) throw new Error(res.data?.message);
    return res.data?.data as DashboardSummary;
  } catch (e: any) {
    return rejectWithValue(e?.response?.data?.message || e?.message || 'Something went wrong');
  }
});

const dashboardSlice = createSlice({
  name: 'dashboard',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchDashboard.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(fetchDashboard.fulfilled, (s, a) => { s.loading = false; s.summary = a.payload; })
      .addCase(fetchDashboard.rejected, (s, a) => { s.loading = false; s.error = a.payload as string; })
      .addCase(logoutUser.fulfilled, () => initialState);
  },
});

export default dashboardSlice.reducer;
