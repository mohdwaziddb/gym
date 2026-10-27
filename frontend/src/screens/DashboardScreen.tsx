import React, { useCallback } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { BrutalScreen } from '../components/brutal/BrutalScreen';
import { BrutalHeader } from '../components/brutal/BrutalHeader';
import { StatTile } from '../components/brutal/StatTile';
import { SectionLabel } from '../components/brutal/SectionLabel';
import { BrutalButton } from '../components/brutal/BrutalButton';
import { BrutalEmpty } from '../components/brutal/BrutalEmpty';
import { BottomBar } from '../components/brutal/BottomBar';
import { Skeleton } from '../components/brutal/Skeleton';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { useRole } from '../hooks/useRole';
import { fetchDashboard } from '../store/dashboardSlice';
import { setMemberSearch, setMemberStatus } from '../store/memberSlice';
import { logoutUser } from '../store/authSlice';
import { COLORS, SPACING, TYPE } from '../theme/tokens';
import { formatDate, formatMoney } from '../utils/dateUtils';

export const DashboardScreen = ({ navigation }: any) => {
  const dispatch = useAppDispatch();
  const { summary, loading, error } = useAppSelector((s) => s.dashboard);
  const { gymCode, username, role } = useRole();

  useFocusEffect(
    useCallback(() => {
      dispatch(fetchDashboard());
    }, [dispatch]),
  );

  // Tile pe tap -> usi status ke members (list ka filter wahi SQL use karta hai, count match karega)
  const openMembers = (status: string) => {
    dispatch(setMemberSearch(''));
    dispatch(setMemberStatus(status));
    navigation.navigate('Members');
  };

  return (
    <BrutalScreen
      refreshing={loading && !!summary}
      onRefresh={() => dispatch(fetchDashboard())}
      footer={<BottomBar active="Dashboard" navigation={navigation} />}
    >
      <BrutalHeader
        section="01 / DASHBOARD"
        title={gymCode || 'IRONBASE'}
        subtitle={`${username} · ${role}${summary ? ` · as of ${formatDate(summary.as_of)}` : ''}`}
        right={<BrutalButton title="Logout" small variant="outline" onPress={() => dispatch(logoutUser())} />}
      />

      {!summary && loading ? <Skeleton rows={3} height={110} /> : null}
      {!summary && !loading && error ? (
        <BrutalEmpty title="Couldn't load dashboard" message={error} actionTitle="Retry" onAction={() => dispatch(fetchDashboard())} />
      ) : null}

      {summary ? (
        <>
          <StatTile
            big
            label="Active members"
            value={summary.active}
            caption={`+${summary.expiring} expiring soon · ${summary.total_members} total`}
            color={COLORS.lime}
            onPress={() => openMembers('ACTIVE')}
          />

          <SectionLabel number="01" title="Membership status" />
          <View style={styles.grid}>
            <View style={styles.cell}>
              <StatTile label="Expiring" value={summary.expiring} caption={`in ${summary.expiring_within_days} days`} color={COLORS.orange} onPress={() => openMembers('EXPIRING')} />
            </View>
            <View style={styles.cell}>
              <StatTile label="Expired" value={summary.expired} caption="need renewal" onPress={() => openMembers('EXPIRED')} />
            </View>
            <View style={styles.cell}>
              <StatTile label="No plan" value={summary.no_plan} caption="assign a plan" onPress={() => openMembers('NO_PLAN')} />
            </View>
            <View style={styles.cell}>
              <StatTile label="Upcoming" value={summary.upcoming} caption="starts later" onPress={() => openMembers('UPCOMING')} />
            </View>
          </View>

          <SectionLabel number="02" title="This month" />
          <View style={styles.grid}>
            <View style={styles.cell}>
              <StatTile label="New members" value={summary.new_members_this_month} caption="joined" />
            </View>
            <View style={styles.cell}>
              <StatTile label="Plans assigned" value={summary.memberships_assigned_this_month} caption="new + renewals" />
            </View>
          </View>
          <StatTile
            label="Membership value assigned"
            value={formatMoney(summary.membership_value_assigned)}
            caption="Value of plans assigned, not cash collected"
          />
          <Text style={styles.note}>Fee collection & payments arrive in Phase 2. Until then, memberships are marked UNPAID.</Text>

          <SectionLabel number="03" title="Quick actions" />
          <View style={styles.actions}>
            <BrutalButton title="+ Add member" onPress={() => navigation.navigate('MemberForm')} />
            <BrutalButton title="Find member" variant="outline" onPress={() => openMembers('')} />
          </View>
        </>
      ) : null}
    </BrutalScreen>
  );
};

const styles = StyleSheet.create({
  grid: { flexDirection: 'row', flexWrap: 'wrap', marginHorizontal: -SPACING.xs },
  cell: { width: '50%', paddingHorizontal: SPACING.xs, marginBottom: SPACING.sm },
  note: { ...TYPE.small, marginTop: SPACING.sm },
  actions: { gap: SPACING.sm },
});
