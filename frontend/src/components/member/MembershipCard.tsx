import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { BrutalBox } from '../brutal/BrutalBox';
import { StatusChip } from '../brutal/StatusChip';
import { BrutalButton } from '../brutal/BrutalButton';
import { COLORS, SPACING, TYPE } from '../../theme/tokens';
import { formatDate, formatMoney } from '../../utils/dateUtils';
import type { Membership } from '../../types';

type Props = {
  membership: Membership;
  highlight?: boolean;
  title?: string;
  onCancel?: () => void;
};

/** Ek membership ka block: plan, dates, amount, UNPAID / CANCELLED tag */
export const MembershipCard = ({ membership: m, highlight, title, onCancel }: Props) => {
  const cancelled = m.status === 'CANCELLED';
  return (
    <BrutalBox color={highlight ? COLORS.lime : COLORS.surface} shadow={highlight} style={styles.box} contentStyle={styles.content}>
      {title ? <Text style={styles.title}>{title}</Text> : null}
      <View style={styles.row}>
        <Text style={[styles.plan, cancelled && styles.strike]} numberOfLines={1}>{m.plan_name}</Text>
        <StatusChip status={cancelled ? 'CANCELLED' : m.payment_status} />
      </View>
      <Text style={styles.dates}>
        {formatDate(m.start_date)} → {formatDate(m.end_date)}
      </Text>
      <View style={styles.row}>
        <Text style={styles.amount}>{formatMoney(m.final_amount)}</Text>
        {Number(m.discount) > 0 ? <Text style={styles.meta}>({formatMoney(m.price)} − {formatMoney(m.discount)} off)</Text> : null}
        {highlight && !cancelled ? <Text style={styles.days}>{m.days_left} DAYS LEFT</Text> : null}
      </View>
      {onCancel && !cancelled ? <BrutalButton title="Cancel membership" variant="outline" small onPress={onCancel} style={styles.cancel} /> : null}
    </BrutalBox>
  );
};

const styles = StyleSheet.create({
  box: { marginBottom: SPACING.md },
  content: { padding: SPACING.md },
  title: { ...TYPE.label, marginBottom: SPACING.xs },
  row: { flexDirection: 'row', alignItems: 'center', gap: SPACING.sm, flexWrap: 'wrap' },
  plan: { ...TYPE.heading, flex: 1 },
  strike: { textDecorationLine: 'line-through', color: COLORS.muted },
  dates: { fontSize: 14, fontWeight: '800', color: COLORS.ink, marginVertical: SPACING.xs },
  amount: { fontSize: 20, fontWeight: '900', color: COLORS.ink },
  meta: { ...TYPE.small },
  days: { ...TYPE.label, marginLeft: 'auto' },
  cancel: { marginTop: SPACING.md },
});
