import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, Alert } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { BrutalScreen } from '../components/brutal/BrutalScreen';
import { BrutalHeader } from '../components/brutal/BrutalHeader';
import { BrutalBox } from '../components/brutal/BrutalBox';
import { BrutalButton } from '../components/brutal/BrutalButton';
import { Skeleton } from '../components/brutal/Skeleton';
import { BrutalEmpty } from '../components/brutal/BrutalEmpty';
import { BottomBar } from '../components/brutal/BottomBar';
import { ConfirmModal } from '../components/common/ConfirmModal';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { useRole } from '../hooks/useRole';
import { fetchPlans, togglePlan, deletePlan } from '../store/planSlice';
import { COLORS, SPACING, TYPE } from '../theme/tokens';
import { formatMoney } from '../utils/dateUtils';
import { stringValue } from '../utils/typeUtils';
import type { MembershipPlan } from '../types';

const DURATION_LABEL: Record<string, string> = {
  MONTHLY: 'MONTHLY',
  QUARTERLY: 'QUARTERLY',
  HALF_YEARLY: 'HALF-YEARLY',
  YEARLY: 'YEARLY',
  CUSTOM: 'CUSTOM',
};

export const PlansScreen = ({ navigation }: any) => {
  const dispatch = useAppDispatch();
  const { list, loading, error, saving } = useAppSelector((s) => s.plan);
  const { isManager } = useRole();
  const [toDelete, setToDelete] = useState<MembershipPlan | null>(null);

  useFocusEffect(
    useCallback(() => {
      dispatch(fetchPlans());
    }, [dispatch]),
  );

  const onToggle = async (p: MembershipPlan) => {
    const res: any = await dispatch(togglePlan(p.id));
    if (res.type?.endsWith('/rejected')) Alert.alert('Failed', stringValue(res.payload));
  };

  const onDelete = async () => {
    if (!toDelete) return;
    const res: any = await dispatch(deletePlan(toDelete.id));
    setToDelete(null);
    if (res.type?.endsWith('/rejected')) Alert.alert('Failed', stringValue(res.payload));
  };

  return (
    <BrutalScreen refreshing={loading && list.length > 0} onRefresh={() => dispatch(fetchPlans())} footer={<BottomBar active="Plans" navigation={navigation} />}>
      <BrutalHeader
        section="03 / PLANS"
        title="Membership plans"
        subtitle={isManager ? `${list.length} ${list.length === 1 ? 'plan' : 'plans'}` : 'View only — ask the owner to change plans'}
        right={isManager ? <BrutalButton title="+ New" small onPress={() => navigation.navigate('PlanForm')} /> : null}
      />

      {loading && list.length === 0 ? <Skeleton rows={3} height={100} /> : null}
      {!loading && error && list.length === 0 ? (
        <BrutalEmpty title="Couldn't load plans" message={error} actionTitle="Retry" onAction={() => dispatch(fetchPlans())} />
      ) : null}
      {!loading && !error && list.length === 0 ? (
        <BrutalEmpty
          title="No plans yet"
          message="Create monthly, quarterly or yearly plans to start assigning memberships."
          actionTitle={isManager ? 'Create plan' : undefined}
          onAction={() => navigation.navigate('PlanForm')}
        />
      ) : null}

      {list.map((p, i) => (
        <BrutalBox key={p.id} color={p.is_active ? COLORS.surface : COLORS.disabled} style={styles.card} contentStyle={styles.cardContent}>
          <View style={styles.row}>
            <Text style={styles.index}>{String(i + 1).padStart(2, '0')}</Text>
            <Text style={styles.type}>{DURATION_LABEL[p.duration_type] || p.duration_type}</Text>
            {!p.is_active ? <Text style={styles.inactive}>INACTIVE</Text> : null}
          </View>
          <Text style={styles.name}>{p.plan_name}</Text>
          <View style={styles.row}>
            <Text style={styles.price}>{formatMoney(p.price)}</Text>
            <Text style={styles.days}>/ {p.duration_days} DAYS</Text>
          </View>
          {p.description ? <Text style={styles.desc}>{p.description}</Text> : null}
          {isManager ? (
            <View style={styles.actions}>
              <BrutalButton title="Edit" small variant="outline" onPress={() => navigation.navigate('PlanForm', { plan: p })} style={styles.flex} />
              <BrutalButton title={p.is_active ? 'Deactivate' : 'Activate'} small variant={p.is_active ? 'outline' : 'primary'} onPress={() => onToggle(p)} style={styles.flex} />
              <BrutalButton title="Delete" small variant="danger" onPress={() => setToDelete(p)} style={styles.flex} />
            </View>
          ) : null}
        </BrutalBox>
      ))}

      <ConfirmModal
        visible={!!toDelete}
        title={`Delete "${stringValue(toDelete?.plan_name)}"?`}
        message="It can't be assigned anymore. Existing memberships keep their plan name and dates."
        onConfirm={onDelete}
        onCancel={() => setToDelete(null)}
        loading={saving}
      />
    </BrutalScreen>
  );
};

const styles = StyleSheet.create({
  card: { marginBottom: SPACING.md },
  cardContent: { padding: SPACING.md },
  row: { flexDirection: 'row', alignItems: 'center', gap: SPACING.sm },
  index: { ...TYPE.label, backgroundColor: COLORS.ink, color: COLORS.lime, paddingHorizontal: 6 },
  type: { ...TYPE.label },
  inactive: { ...TYPE.label, color: COLORS.danger, marginLeft: 'auto' },
  name: { ...TYPE.heading, fontSize: 20, marginTop: SPACING.sm },
  price: { fontSize: 32, fontWeight: '900', color: COLORS.ink },
  days: { ...TYPE.label, color: COLORS.muted },
  desc: { ...TYPE.small, marginTop: SPACING.xs },
  actions: { flexDirection: 'row', gap: SPACING.xs, marginTop: SPACING.md },
  flex: { flex: 1 },
});
