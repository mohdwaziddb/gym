import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { GymCodeScreen } from '../screens/GymCodeScreen';
import { LoginScreen } from '../screens/LoginScreen';
import { HomeScreen } from '../screens/HomeScreen';
import { GymFormScreen } from '../screens/GymFormScreen';
import { DashboardScreen } from '../screens/DashboardScreen';
import { MembersScreen } from '../screens/MembersScreen';
import { MemberFormScreen } from '../screens/MemberFormScreen';
import { MemberDetailScreen } from '../screens/MemberDetailScreen';
import { AssignMembershipScreen } from '../screens/AssignMembershipScreen';
import { PlansScreen } from '../screens/PlansScreen';
import { PlanFormScreen } from '../screens/PlanFormScreen';
import { Loader } from '../components/common/Loader';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { restoreSession, logoutUser } from '../store/authSlice';
import { setUnauthorizedHandler } from '../api/client';

const Stack = createNativeStackNavigator();

/**
 * Token hai -> Home / GymForm, token nahi -> GymCode / Login.
 * Login / logout / session expire pe Redux token badalte hi screen apne aap badal jaati hai.
 */
export const AppNavigator = () => {
  const dispatch = useAppDispatch();
  const { token, restoring } = useAppSelector((s) => s.auth);

  useEffect(() => {
    setUnauthorizedHandler(() => dispatch(logoutUser()));
    dispatch(restoreSession());
  }, []);

  if (restoring) return <Loader visible text="Loading..." />;

  return (
    <NavigationContainer>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        {token ? (
          <>
            {/* Phase 1: login ke baad pehla screen Dashboard; BottomBar se Members / Plans / Gyms */}
            <Stack.Screen name="Dashboard" component={DashboardScreen} />
            <Stack.Screen name="Members" component={MembersScreen} />
            <Stack.Screen name="MemberForm" component={MemberFormScreen} />
            <Stack.Screen name="MemberDetail" component={MemberDetailScreen} />
            <Stack.Screen name="AssignMembership" component={AssignMembershipScreen} />
            <Stack.Screen name="Plans" component={PlansScreen} />
            <Stack.Screen name="PlanForm" component={PlanFormScreen} />
            <Stack.Screen name="Home" component={HomeScreen} />
            <Stack.Screen name="GymForm" component={GymFormScreen} />
          </>
        ) : (
          <>
            <Stack.Screen name="GymCode" component={GymCodeScreen} />
            <Stack.Screen name="Login" component={LoginScreen} />
          </>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
};
