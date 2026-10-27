import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, Alert } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { BrutalScreen } from '../components/brutal/BrutalScreen';
import { BrutalHeader } from '../components/brutal/BrutalHeader';
import { BrutalBox } from '../components/brutal/BrutalBox';
import { BrutalButton } from '../components/brutal/BrutalButton';
import { StatusChip } from '../components/brutal/StatusChip';
import { SectionLabel } from '../components/brutal/SectionLabel';
import { Skeleton } from '../components/brutal/Skeleton';
import { BrutalEmpty } from '../components/brutal/BrutalEmpty';
import { MembershipCard } from '../components/member/MembershipCard';
import { ConfirmModal } from '../components/common/ConfirmModal';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { useRole } from '../hooks/useRole';
import { fetchMemberDetail, archiveMember, cancelMembership, clearMemberDetail } from '../store/memberSlice';
import { COLORS, SPACING, TYPE } from '../theme/tokens';
import { formatDate } from '../utils/dateUtils';
import { stringValue } from '../utils/typeUtils';

const InfoRow = ({ label, value }: { label: string; value: string }) => (
  <View style={styles.infoRow}>
    <Text style={styles.infoLabel}>{label}</Text>
    <Text style={styles.infoValue}>{value || '—'}</Text>
  </View>
);

export const MemberDetailScreen = ({ navigation, route }: any) => {
  const id: number = route?.params?.id;
  const dispatch = useAppDispatch();
  const { detail, detailLoading, error, saving } = useAppSelector((s) => s.member);
  const { isManager } = useRole();
  const [confirm, setConfirm] = useState<{ type: 'archive' } | { type: 'cancel'; membershipId: number } | null>(null);

  useFocusEffect(
    useCallback(() => {
      dispatch(fetchMemberDetail(id));
      return () => { dispatch(clearMemberDetail()); };
    }, [dispatch, id]),
  );

  const member = detail && detail.id === id ? detail : null;

  const onConfirm = async () => {
    if (!confirm) return;
    const res: any = confirm.type === 'archive' ? await dispatch(archiveMember(id)) : await dispatch(cancelMembership(confirm.membershipId));
    setConfirm(null);
    if (res.type?.endsWith('/rejected')) return Alert.alert('Failed', stringValue(res.payload));
    if (confirm.type === 'archive') navigation.goBack();
    else dispatch(fetchMemberDetail(id));
  };

  if (!member) {
    return (
      <BrutalScreen>
        <BrutalHeader section="02 / MEMBER" title="Member" onBack={() => navigation.goBack()} />
        {detailLoading || !error ? <Skeleton rows={3} height={100} /> : (
          <BrutalEmpty title="Couldn't load member" message={error || ''} actionTitle="Retry" onAction={() => dispatch(fetchMemberDetail(id))} />
        )}
      </BrutalScreen>
    );
  }

  const hasPlan = member.memberships.some((m) => m.status === 'ACTIVE');
  const canRenew = hasPlan && member.status !== 'NO_PLAN';
  const history = member.memberships.filter((m) => m.id !== member.current_membership?.id && m.id !== member.upcoming_membership?.id);

  return (
    <BrutalScreen refreshing={detailLoading} onRefresh={() => dispatch(fetchMemberDetail(id))}>
      <BrutalHeader
        section={`02 / ${member.member_code}`}
        title={member.full_name}
        onBack={() => navigation.goBack()}
        right={<BrutalButton title="Edit" small variant="outline" onPress={() => navigation.navigate('MemberForm', { member })} />}
      />

      <View style={styles.statusRow}>
        <StatusChip status={member.status} />
        <Text style={styles.statusText}>
          {member.expiry_date ? `Valid till ${formatDate(member.expiry_date)}` : 'No membership assigned yet'}
        </Text>
      </View>

      <View style={styles.actions}>
        {canRenew ? (
          <BrutalButton
            title="Renew"
            onPress={() => navigation.navigate('AssignMembership', { memberId: member.id, mode: 'renew', member })}
            style={styles.flex}
          />
        ) : null}
        <BrutalButton
          title={canRenew ? 'New plan' : 'Assign plan'}
          variant={canRenew ? 'outline' : 'primary'}
          onPress={() => navigation.navigate('AssignMembership', { memberId: member.id, mode: 'assign', member })}
          style={styles.flex}
        />
      </View>

      <SectionLabel number="01" title="Current plan" />
      {member.current_membership ? (
        <MembershipCard
          membership={member.current_membership}
          highlight
          onCancel={isManager ? () => setConfirm({ type: 'cancel', membershipId: member.current_membership!.id }) : undefined}
        />
      ) : (
        <BrutalEmpty title="No running plan" message={member.status === 'EXPIRED' ? 'Membership has expired. Renew to continue.' : 'Assign a plan to activate this member.'} />
      )}
      {member.upcoming_membership ? (
        <MembershipCard
          title="Up next"
          membership={member.upcoming_membership}
          onCancel={isManager ? () => setConfirm({ type: 'cancel', membershipId: member.upcoming_membership!.id }) : undefined}
        />
      ) : null}

      <SectionLabel number="02" title="Profile" />
      <BrutalBox contentStyle={styles.profile}>
        <InfoRow label="Mobile" value={member.phone} />
        <InfoRow label="Email" value={member.email} />
        <InfoRow label="Gender" value={member.gender} />
        <InfoRow label="Date of birth" value={member.dob ? formatDate(member.dob) : ''} />
        <InfoRow label="Joined" value={formatDate(member.joining_date)} />
        <InfoRow label="Address" value={member.address} />
        <InfoRow label="Emergency" value={[member.emergency_name, member.emergency_phone].filter(Boolean).join(' · ')} />
        {isManager ? <InfoRow label="Private notes" value={stringValue(member.notes)} /> : null}
      </BrutalBox>

      <SectionLabel number="03" title={`History (${history.length})`} />
      {history.length === 0 ? <Text style={styles.muted}>No previous memberships.</Text> : null}
      {history.map((m) => (
        <MembershipCard key={m.id} membership={m} />
      ))}

      {isManager ? (
        <BrutalButton title="Archive member" variant="danger" onPress={() => setConfirm({ type: 'archive' })} style={styles.archive} />
      ) : null}

      <ConfirmModal
        visible={!!confirm}
        title={confirm?.type === 'archive' ? 'Archive this member?' : 'Cancel this membership?'}
        message={confirm?.type === 'archive'
          ? 'The member will be hidden from lists. Membership history is kept.'
          : 'The membership will stop counting as active. This cannot be undone.'}
        onConfirm={onConfirm}
        onCancel={() => setConfirm(null)}
        loading={saving}
      />
    </BrutalScreen>
  );
};

const styles = StyleSheet.create({
  statusRow: { flexDirection: 'row', alignItems: 'center', gap: SPACING.sm, marginBottom: SPACING.md },
  statusText: { ...TYPE.body, fontWeight: '800' },
  actions: { flexDirection: 'row', gap: SPACING.sm },
  flex: { flex: 1 },
  profile: { padding: SPACING.md },
  infoRow: { flexDirection: 'row', paddingVertical: SPACING.sm, borderBottomWidth: 1.5, borderBottomColor: COLORS.disabled },
  infoLabel: { ...TYPE.label, width: 110, color: COLORS.muted },
  infoValue: { ...TYPE.body, flex: 1, fontWeight: '700' },
  muted: { ...TYPE.small },
  archive: { marginTop: SPACING.xl },
});
