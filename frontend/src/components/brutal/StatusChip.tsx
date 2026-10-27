import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { COLORS, BORDER, STATUS_COLORS } from '../../theme/tokens';
import { stringValue } from '../../utils/typeUtils';

/** ACTIVE / EXPIRING / EXPIRED / UPCOMING / NO_PLAN / UNPAID chip */
export const StatusChip = ({ status }: { status: string }) => {
  const key = stringValue(status).toUpperCase();
  const c = STATUS_COLORS[key] || { bg: COLORS.surface, text: COLORS.ink, label: key };
  return (
    <View style={[styles.chip, { backgroundColor: c.bg }]}>
      <Text style={[styles.text, { color: c.text }]}>{c.label}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  chip: {
    alignSelf: 'flex-start',
    borderWidth: 2,
    borderColor: COLORS.line,
    borderRadius: BORDER.radius,
    paddingHorizontal: 8,
    paddingVertical: 3,
  },
  text: { fontSize: 11, fontWeight: '900', letterSpacing: 1 },
});
