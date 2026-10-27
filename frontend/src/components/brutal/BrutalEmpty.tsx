import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { BrutalButton } from './BrutalButton';
import { COLORS, BORDER, SPACING, TYPE } from '../../theme/tokens';

type Props = { title: string; message?: string; actionTitle?: string; onAction?: () => void };

/** Custom empty / error state - dashed border box + optional action */
export const BrutalEmpty = ({ title, message, actionTitle, onAction }: Props) => (
  <View style={styles.wrap}>
    <Text style={styles.mark}>{'//'}</Text>
    <Text style={styles.title}>{title}</Text>
    {message ? <Text style={styles.message}>{message}</Text> : null}
    {actionTitle && onAction ? <BrutalButton title={actionTitle} onPress={onAction} small style={styles.action} /> : null}
  </View>
);

const styles = StyleSheet.create({
  wrap: {
    borderWidth: BORDER.width,
    borderColor: COLORS.line,
    borderStyle: 'dashed',
    borderRadius: BORDER.radius,
    padding: SPACING.xl,
    alignItems: 'center',
    marginVertical: SPACING.md,
  },
  mark: { fontSize: 32, fontWeight: '900', color: COLORS.orange },
  title: { ...TYPE.heading, textAlign: 'center', marginTop: SPACING.xs },
  message: { ...TYPE.small, textAlign: 'center', marginTop: SPACING.xs },
  action: { marginTop: SPACING.lg },
});
