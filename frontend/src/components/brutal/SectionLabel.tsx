import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { COLORS, SPACING, TYPE } from '../../theme/tokens';

/** "01" + "CURRENT PLAN" section label, moti line ke saath */
export const SectionLabel = ({ number, title, right }: { number?: string; title: string; right?: React.ReactNode }) => (
  <View style={styles.wrap}>
    {number ? <Text style={styles.number}>{number}</Text> : null}
    <Text style={styles.title}>{title}</Text>
    <View style={styles.line} />
    {right}
  </View>
);

const styles = StyleSheet.create({
  wrap: { flexDirection: 'row', alignItems: 'center', marginTop: SPACING.lg, marginBottom: SPACING.md, gap: SPACING.sm },
  number: { ...TYPE.label, backgroundColor: COLORS.lime, borderWidth: 2, borderColor: COLORS.line, paddingHorizontal: 6 },
  title: { ...TYPE.label },
  line: { flex: 1, height: 3, backgroundColor: COLORS.ink },
});
