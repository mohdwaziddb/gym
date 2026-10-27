import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { BrutalBox } from '../brutal/BrutalBox';
import { StatusChip } from '../brutal/StatusChip';
import { COLORS, SPACING, TYPE } from '../../theme/tokens';
import { formatDate } from '../../utils/dateUtils';
import type { MemberListItem } from '../../types';

/** Members list ka ek card */
export const MemberRow = ({ member, onPress }: { member: MemberListItem; onPress: () => void }) => (
  <BrutalBox onPress={onPress} style={styles.box} contentStyle={styles.content} accessibilityLabel={`${member.full_name} ${member.status}`}>
    <View style={styles.codeCol}>
      <Text style={styles.code}>{member.member_code}</Text>
    </View>
    <View style={styles.main}>
      <Text style={styles.name} numberOfLines={1}>{member.full_name}</Text>
      <Text style={styles.meta}>{member.phone}</Text>
      <Text style={styles.meta}>{member.expiry_date ? `Valid till ${formatDate(member.expiry_date)}` : 'No membership yet'}</Text>
    </View>
    <StatusChip status={member.status} />
  </BrutalBox>
);

const styles = StyleSheet.create({
  box: { marginBottom: SPACING.md },
  content: { flexDirection: 'row', alignItems: 'center', padding: SPACING.md, gap: SPACING.md },
  codeCol: { backgroundColor: COLORS.ink, paddingHorizontal: 6, paddingVertical: 4, alignSelf: 'flex-start' },
  code: { fontSize: 11, fontWeight: '900', color: COLORS.lime, letterSpacing: 0.5 },
  main: { flex: 1 },
  name: { ...TYPE.heading, fontSize: 16 },
  meta: { ...TYPE.small, marginTop: 2 },
});
