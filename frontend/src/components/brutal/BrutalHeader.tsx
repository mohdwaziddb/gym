import React from 'react';
import { View, Text, Pressable, StyleSheet } from 'react-native';
import { COLORS, SPACING, TYPE, BORDER, MIN_TOUCH } from '../../theme/tokens';

type Props = {
  section?: string; // e.g. "02 / MEMBERS"
  title: string;
  subtitle?: string;
  onBack?: () => void;
  right?: React.ReactNode;
};

/** Bada uppercase title + numbered section label + moti divider */
export const BrutalHeader = ({ section, title, subtitle, onBack, right }: Props) => (
  <View style={styles.wrap}>
    <View style={styles.topRow}>
      {onBack ? (
        <Pressable onPress={onBack} style={styles.back} accessibilityRole="button" accessibilityLabel="Back" hitSlop={8}>
          <Text style={styles.backText}>{'←'}</Text>
        </Pressable>
      ) : null}
      {section ? <Text style={styles.section}>{section}</Text> : <View />}
      <View style={styles.right}>{right}</View>
    </View>
    <Text style={styles.title}>{title}</Text>
    {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
    <View style={styles.divider} />
  </View>
);

const styles = StyleSheet.create({
  wrap: { marginBottom: SPACING.lg },
  topRow: { flexDirection: 'row', alignItems: 'center', marginBottom: SPACING.sm, minHeight: 28 },
  back: {
    width: MIN_TOUCH - 8,
    height: MIN_TOUCH - 8,
    borderWidth: BORDER.width,
    borderColor: COLORS.line,
    borderRadius: BORDER.radius,
    backgroundColor: COLORS.surface,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: SPACING.md,
  },
  backText: { fontSize: 20, fontWeight: '900', color: COLORS.ink },
  section: { ...TYPE.label, backgroundColor: COLORS.ink, color: COLORS.lime, paddingHorizontal: SPACING.sm, paddingVertical: 2 },
  right: { marginLeft: 'auto' },
  title: { ...TYPE.title },
  subtitle: { ...TYPE.small, marginTop: SPACING.xs },
  divider: { height: 5, backgroundColor: COLORS.ink, marginTop: SPACING.md },
});
