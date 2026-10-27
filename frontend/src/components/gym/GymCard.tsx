import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { BrutalBox } from '../brutal/BrutalBox';
import { BrutalButton } from '../brutal/BrutalButton';
import { COLORS, SPACING, TYPE } from '../../theme/tokens';
import { formatMoney } from '../../utils/dateUtils';

type Props = { gym: any; onEdit: () => void; onDelete: () => void; deleting?: boolean };

export const GymCard = ({ gym, onEdit, onDelete, deleting }: Props) => (
  <BrutalBox style={styles.box} contentStyle={styles.content}>
    <View style={styles.row}>
      <Text style={styles.code}>{gym.gym_code}</Text>
      {gym.city ? <Text style={styles.city}>{String(gym.city).toUpperCase()}</Text> : null}
    </View>
    <Text style={styles.name}>{gym.gym_name}</Text>
    {gym.owner_name || gym.contact_no ? (
      <Text style={styles.meta}>{[gym.owner_name, gym.contact_no].filter(Boolean).join(' · ')}</Text>
    ) : null}
    {gym.email ? <Text style={styles.meta}>{gym.email}</Text> : null}
    {gym.address ? <Text style={styles.meta}>{gym.address}</Text> : null}
    {gym.monthly_fees ? <Text style={styles.fees}>{formatMoney(gym.monthly_fees)} <Text style={styles.per}>/ MONTH</Text></Text> : null}
    <View style={styles.actions}>
      <BrutalButton title="Edit" small variant="outline" onPress={onEdit} style={styles.flex} />
      <BrutalButton title="Delete" small variant="danger" onPress={onDelete} loading={deleting} style={styles.flex} />
    </View>
  </BrutalBox>
);

const styles = StyleSheet.create({
  box: { marginBottom: SPACING.md },
  content: { padding: SPACING.md },
  row: { flexDirection: 'row', alignItems: 'center', gap: SPACING.sm },
  code: { ...TYPE.label, backgroundColor: COLORS.ink, color: COLORS.lime, paddingHorizontal: 6, paddingVertical: 2 },
  city: { ...TYPE.label, color: COLORS.muted },
  name: { ...TYPE.heading, fontSize: 20, marginTop: SPACING.sm },
  meta: { ...TYPE.small, marginTop: 2 },
  fees: { fontSize: 24, fontWeight: '900', color: COLORS.ink, marginTop: SPACING.sm },
  per: { ...TYPE.label, color: COLORS.muted },
  actions: { flexDirection: 'row', gap: SPACING.sm, marginTop: SPACING.md },
  flex: { flex: 1 },
});
