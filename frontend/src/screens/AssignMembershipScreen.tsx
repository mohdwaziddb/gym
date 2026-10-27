import React, { useEffect, useMemo, useState } from 'react';
import { View, Text, StyleSheet, Alert } from 'react-native';
import { BrutalScreen } from '../components/brutal/BrutalScreen';
import { BrutalHeader } from '../components/brutal/BrutalHeader';
import { BrutalBox } from '../components/brutal/BrutalBox';
import { BrutalButton } from '../components/brutal/BrutalButton';
import { BrutalInput } from '../components/brutal/BrutalInput';
import { ChipSelect } from '../components/brutal/ChipSelect';
import { DateField } from '../components/brutal/DateField';
import { SectionLabel } from '../components/brutal/SectionLabel';
import { Skeleton } from '../components/brutal/Skeleton';
import { BrutalEmpty } from '../components/brutal/BrutalEmpty';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { useRole } from '../hooks/useRole';
import { fetchPlans } from '../store/planSlice';
import { assignMembership, renewMembership } from '../store/memberSlice';
import { COLORS, SPACING, TYPE } from '../theme/tokens';
import { addDays, formatDate, formatMoney, isValidDate, todayString } from '../utils/dateUtils';
import { floatValue, stringValue } from '../utils/typeUtils';
import type { MemberDetail } from '../types';

/**
 * mode 'assign' -> start date user chune (default aaj)
 * mode 'renew'  -> start date backend tay karta hai (chalu plan ke agle din / aaj), yaha sirf preview
 */
export const AssignMembershipScreen = ({ navigation, route }: any) => {
  const member: MemberDetail = route?.params?.member;
  const mode: 'assign' | 'renew' = route?.params?.mode === 'renew' ? 'renew' : 'assign';
  const dispatch = useAppDispatch();
  const { list: plans, loading } = useAppSelector((s) => s.plan);
  const { saving } = useAppSelector((s) => s.member);
  const { isManager } = useRole();

  const lastPlanId = member?.memberships?.find((m) => m.id === member.latest_membership_id)?.plan_id;
  const [planId, setPlanId] = useState<string>(lastPlanId ? String(lastPlanId) : '');
  const [startDate, setStartDate] = useState(mode === 'renew' ? stringValue(member?.renewal_start_date) : todayString());
  const [discount, setDiscount] = useState('');
  const [error, setError] = useState<{ plan?: string; start?: string; discount?: string }>({});

  useEffect(() => { dispatch(fetchPlans()); }, [dispatch]);

  const activePlans = plans.filter((p) => p.is_active);
  const plan = activePlans.find((p) => String(p.id) === planId);

  // Live preview: end date + final amount (backend bhi yahi formula use karta hai)
  const preview = useMemo(() => {
    if (!plan || !isValidDate(startDate)) return null;
    const price = floatValue(plan.price) ?? 0;
    const disc = floatValue(discount) ?? 0;
    return { end: addDays(startDate, plan.duration_days - 1), price, disc, final: Math.max(price - disc, 0) };
  }, [plan, startDate, discount]);

  const onSubmit = async () => {
    const e: typeof error = {};
    if (!plan) e.plan = 'Choose a plan';
    if (mode === 'assign' && !isValidDate(startDate)) e.start = 'Enter date as YYYY-MM-DD';
    const disc = stringValue(discount) ? floatValue(discount) : 0;
    if (disc === null || disc < 0) e.discount = 'Enter a valid amount';
    else if (plan && disc > (floatValue(plan.price) ?? 0)) e.discount = 'Discount cannot be more than plan price';
    setError(e);
    if (Object.keys(e).length > 0) return;

    const payload: any = { member_id: member.id, plan_id: plan!.id, discount: stringValue(discount) || '0' };
    let res: any;
    if (mode === 'renew') {
      payload.after_membership_id = member.latest_membership_id;
      res = await dispatch(renewMembership(payload));
    } else {
      payload.start_date = startDate;
      res = await dispatch(assignMembership(payload));
    }
    if (res.type?.endsWith('/rejected')) return Alert.alert('Not saved', stringValue(res.payload));
    navigation.goBack();
  };

  return (
    <BrutalScreen>
      <BrutalHeader
        section={`${mode === 'renew' ? 'RENEW' : 'ASSIGN'} / ${stringValue(member?.member_code)}`}
        title={mode === 'renew' ? 'Renew plan' : 'Assign plan'}
        subtitle={stringValue(member?.full_name)}
        onBack={() => navigation.goBack()}
      />

      <SectionLabel number="01" title="Choose plan" />
      {loading && plans.length === 0 ? <Skeleton rows={2} height={48} /> : null}
      {!loading && activePlans.length === 0 ? (
        <BrutalEmpty
          title="No active plans"
          message={isManager ? 'Create a membership plan first.' : 'Ask the owner to create a membership plan.'}
          actionTitle={isManager ? 'Create plan' : undefined}
          onAction={() => navigation.navigate('PlanForm')}
        />
      ) : null}
      <ChipSelect
        options={activePlans.map((p) => ({ value: String(p.id), label: p.plan_name, caption: `${p.duration_days} days · ${formatMoney(p.price)}` }))}
        value={planId}
        onChange={setPlanId}
        error={error.plan}
      />

      <SectionLabel number="02" title="Dates & discount" />
      {mode === 'renew' ? (
        <BrutalInput label="Starts on" value={formatDate(startDate)} onChangeText={() => {}} editable={false} hint="Renewal starts the day after the current plan ends (or today if expired)." />
      ) : (
        <DateField label="Start date *" value={startDate} onChangeText={setStartDate} error={error.start} />
      )}
      <BrutalInput label="Discount (₹)" value={discount} onChangeText={setDiscount} placeholder="0" keyboardType="decimal-pad" error={error.discount} />

      <SectionLabel number="03" title="Summary" />
      <BrutalBox color={COLORS.ink} contentStyle={styles.summary}>
        {preview ? (
          <>
            <Text style={styles.sumLabel}>VALID</Text>
            <Text style={styles.sumDates}>{formatDate(startDate)} → {formatDate(preview.end)}</Text>
            <View style={styles.sumRow}>
              <Text style={styles.sumLabel}>PLAN</Text>
              <Text style={styles.sumValue}>{formatMoney(preview.price)}</Text>
            </View>
            <View style={styles.sumRow}>
              <Text style={styles.sumLabel}>DISCOUNT</Text>
              <Text style={styles.sumValue}>− {formatMoney(preview.disc)}</Text>
            </View>
            <View style={[styles.sumRow, styles.sumTotal]}>
              <Text style={styles.sumLabel}>AMOUNT DUE</Text>
              <Text style={styles.sumBig}>{formatMoney(preview.final)}</Text>
            </View>
            <Text style={styles.sumNote}>Marked UNPAID until payment is recorded.</Text>
          </>
        ) : (
          <Text style={styles.sumNote}>Choose a plan and start date to see the summary.</Text>
        )}
      </BrutalBox>

      <BrutalButton
        title={mode === 'renew' ? 'Confirm renewal' : 'Confirm & assign'}
        onPress={onSubmit}
        loading={saving}
        disabled={activePlans.length === 0}
        style={styles.submit}
      />
    </BrutalScreen>
  );
};

const styles = StyleSheet.create({
  summary: { padding: SPACING.lg },
  sumLabel: { ...TYPE.label, color: COLORS.muted },
  sumDates: { fontSize: 18, fontWeight: '900', color: COLORS.lime, marginBottom: SPACING.md },
  sumRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: SPACING.xs },
  sumValue: { fontSize: 16, fontWeight: '800', color: COLORS.surface },
  sumTotal: { borderTopWidth: 2, borderTopColor: COLORS.surface, marginTop: SPACING.sm, paddingTop: SPACING.sm },
  sumBig: { fontSize: 32, fontWeight: '900', color: COLORS.lime },
  sumNote: { ...TYPE.small, color: COLORS.disabled, marginTop: SPACING.sm },
  submit: { marginTop: SPACING.lg },
});
