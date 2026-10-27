import { createSlice, createAsyncThunk, isAnyOf, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';
import client from '../api/client';
import { logoutUser } from './authSlice';
import type { MembershipPlan } from '../types';

type PlanState = {
  list: MembershipPlan[];
  loading: boolean;
  saving: boolean;
  error: string | null;
};

const initialState: PlanState = {
  list: [],
  loading: false,
  saving: false,
  error: null,
};

const errorMessage = (e: any) => e?.response?.data?.message || e?.message || 'Something went wrong';

const unwrap = (res: any) => {
  if (res.data?.success === false) throw new Error(res.data?.message);
  return res.data?.data;
};

export const fetchPlans = createAsyncThunk('plan/fetch', async (_, { rejectWithValue }) => {
  try {
    const data = unwrap(await client.get('/rest/plan/list'));
    return (data?.data_array || []) as MembershipPlan[];
  } catch (e: any) {
    return rejectWithValue(errorMessage(e));
  }
});

export const savePlan = createAsyncThunk('plan/save', async (payload: any, { rejectWithValue }) => {
  try {
    return unwrap(await client.post('/rest/plan/save', null, { params: payload })) as MembershipPlan;
  } catch (e: any) {
    return rejectWithValue(errorMessage(e));
  }
});

export const togglePlan = createAsyncThunk('plan/toggle', async (id: number, { rejectWithValue }) => {
  try {
    return unwrap(await client.post('/rest/plan/toggle', null, { params: { id } })) as MembershipPlan;
  } catch (e: any) {
    return rejectWithValue(errorMessage(e));
  }
});

export const deletePlan = createAsyncThunk('plan/delete', async (id: number, { rejectWithValue }) => {
  try {
    unwrap(await client.post('/rest/plan/delete', null, { params: { id } }));
    return id;
  } catch (e: any) {
    return rejectWithValue(errorMessage(e));
  }
});

const planSlice = createSlice({
  name: 'plan',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchPlans.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(fetchPlans.fulfilled, (s, a) => { s.loading = false; s.list = a.payload; })
      .addCase(fetchPlans.rejected, (s, a) => { s.loading = false; s.error = a.payload as string; })
      .addCase(togglePlan.fulfilled, (s, a) => { s.list = s.list.map((p) => (p.id === a.payload.id ? a.payload : p)); })
      .addCase(deletePlan.fulfilled, (s, a) => { s.list = s.list.filter((p) => p.id !== a.payload); })
      .addCase(logoutUser.fulfilled, () => initialState)
      .addMatcher(isPending(savePlan, togglePlan, deletePlan), (s) => { s.saving = true; })
      .addMatcher(isAnyOf(isFulfilled(savePlan, togglePlan, deletePlan), isRejected(savePlan, togglePlan, deletePlan)), (s) => { s.saving = false; });
  },
});

export default planSlice.reducer;
