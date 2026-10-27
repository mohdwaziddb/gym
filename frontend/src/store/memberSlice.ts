import { createSlice, createAsyncThunk, isAnyOf, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';
import client from '../api/client';
import { logoutUser } from './authSlice';
import { stringValue } from '../utils/typeUtils';
import type { MemberDetail, MemberListItem } from '../types';

const PAGE_SIZE = 20;

type MemberState = {
  list: MemberListItem[];
  total: number;
  page: number;
  hasMore: boolean;
  search: string;
  status: string; // '' = sab
  loading: boolean;
  loadingMore: boolean;
  error: string | null;
  detail: MemberDetail | null;
  detailLoading: boolean;
  saving: boolean;
};

const initialState: MemberState = {
  list: [],
  total: 0,
  page: 0,
  hasMore: false,
  search: '',
  status: '',
  loading: false,
  loadingMore: false,
  error: null,
  detail: null,
  detailLoading: false,
  saving: false,
};

const errorMessage = (e: any) => e?.response?.data?.message || e?.message || 'Something went wrong';

// backend {success:false, message} ko error bana do
const unwrap = (res: any) => {
  if (res.data?.success === false) throw new Error(res.data?.message);
  return res.data?.data;
};

/** reset=true -> pehla page (search / filter badalne pe), warna agla page (load more) */
export const fetchMembers = createAsyncThunk('member/fetch', async ({ reset }: { reset: boolean }, { getState, rejectWithValue }) => {
  try {
    const s = (getState() as any).member as MemberState;
    const page = reset ? 0 : s.page + 1;
    const res = await client.get('/rest/member/list', { params: { search: s.search, status: s.status, page, size: PAGE_SIZE } });
    const data = unwrap(res);
    return { reset, page, list: (data?.data_array || []) as MemberListItem[], total: data?.total || 0, hasMore: !!data?.has_more };
  } catch (e: any) {
    return rejectWithValue(errorMessage(e));
  }
});

export const fetchMemberDetail = createAsyncThunk('member/detail', async (id: number, { rejectWithValue }) => {
  try {
    return unwrap(await client.get('/rest/member/get', { params: { id } })) as MemberDetail;
  } catch (e: any) {
    return rejectWithValue(errorMessage(e));
  }
});

export const saveMember = createAsyncThunk('member/save', async (payload: any, { rejectWithValue }) => {
  try {
    return unwrap(await client.post('/rest/member/save', null, { params: payload }));
  } catch (e: any) {
    return rejectWithValue(errorMessage(e));
  }
});

export const archiveMember = createAsyncThunk('member/archive', async (id: number, { rejectWithValue }) => {
  try {
    unwrap(await client.post('/rest/member/archive', null, { params: { id } }));
    return id;
  } catch (e: any) {
    return rejectWithValue(errorMessage(e));
  }
});

export const assignMembership = createAsyncThunk('member/assignMembership', async (payload: any, { rejectWithValue }) => {
  try {
    return unwrap(await client.post('/rest/membership/assign', null, { params: payload }));
  } catch (e: any) {
    return rejectWithValue(errorMessage(e));
  }
});

// after_membership_id bhejo - double tap pe doosra renew backend reject karega
export const renewMembership = createAsyncThunk('member/renewMembership', async (payload: any, { rejectWithValue }) => {
  try {
    return unwrap(await client.post('/rest/membership/renew', null, { params: payload }));
  } catch (e: any) {
    return rejectWithValue(errorMessage(e));
  }
});

export const cancelMembership = createAsyncThunk('member/cancelMembership', async (id: number, { rejectWithValue }) => {
  try {
    unwrap(await client.post('/rest/membership/cancel', null, { params: { id } }));
    return id;
  } catch (e: any) {
    return rejectWithValue(errorMessage(e));
  }
});

const SAVING_THUNKS = [saveMember, assignMembership, renewMembership, cancelMembership, archiveMember] as const;

const memberSlice = createSlice({
  name: 'member',
  initialState,
  reducers: {
    setMemberSearch: (s, a) => { s.search = stringValue(a.payload); },
    setMemberStatus: (s, a) => { s.status = stringValue(a.payload); },
    clearMemberDetail: (s) => { s.detail = null; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchMembers.pending, (s, a) => {
        if (a.meta.arg.reset) s.loading = true; else s.loadingMore = true;
        s.error = null;
      })
      .addCase(fetchMembers.fulfilled, (s, a) => {
        s.loading = false;
        s.loadingMore = false;
        s.list = a.payload.reset ? a.payload.list : [...s.list, ...a.payload.list];
        s.page = a.payload.page;
        s.total = a.payload.total;
        s.hasMore = a.payload.hasMore;
      })
      .addCase(fetchMembers.rejected, (s, a) => { s.loading = false; s.loadingMore = false; s.error = a.payload as string; })
      .addCase(fetchMemberDetail.pending, (s) => { s.detailLoading = true; s.error = null; })
      .addCase(fetchMemberDetail.fulfilled, (s, a) => { s.detailLoading = false; s.detail = a.payload; })
      .addCase(fetchMemberDetail.rejected, (s, a) => { s.detailLoading = false; s.error = a.payload as string; })
      .addCase(archiveMember.fulfilled, (s, a) => {
        s.list = s.list.filter((m) => m.id !== a.payload);
        s.total = Math.max(s.total - 1, 0);
        s.detail = null;
      })
      // logout pe pichle gym ka data saaf
      .addCase(logoutUser.fulfilled, () => initialState)
      // save / assign / renew / cancel / archive ke waqt button loading (addCase ek action pe ek hi baar lagta hai, isliye matcher)
      .addMatcher(isPending(...SAVING_THUNKS), (s) => { s.saving = true; })
      .addMatcher(isAnyOf(isFulfilled(...SAVING_THUNKS), isRejected(...SAVING_THUNKS)), (s) => { s.saving = false; });
  },
});

export const { setMemberSearch, setMemberStatus, clearMemberDetail } = memberSlice.actions;
export default memberSlice.reducer;
