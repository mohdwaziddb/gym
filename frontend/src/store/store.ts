import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import gymReducer from './gymSlice';
import memberReducer from './memberSlice';
import planReducer from './planSlice';
import dashboardReducer from './dashboardSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    gym: gymReducer,
    member: memberReducer,
    plan: planReducer,
    dashboard: dashboardReducer,
  },
  middleware: (getDefault) => getDefault({ serializableCheck: false }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
