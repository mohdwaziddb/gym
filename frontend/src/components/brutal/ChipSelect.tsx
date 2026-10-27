import React from 'react';
import { View, Text, Pressable, StyleSheet, ScrollView } from 'react-native';
import { COLORS, BORDER, SPACING, TYPE } from '../../theme/tokens';

export type ChipOption = { value: string; label: string; caption?: string };

type Props = {
  label?: string;
  options: ChipOption[];
  value: string;
  onChange: (value: string) => void;
  horizontal?: boolean;
  error?: string;
};

/** Single-select chips (status filter, gender, plan type, plan choose) */
export const ChipSelect = ({ label, options, value, onChange, horizontal, error }: Props) => {
  const chips = options.map((o) => {
    const selected = o.value === value;
    return (
      <Pressable
        key={o.value}
        onPress={() => onChange(o.value)}
        accessibilityRole="button"
        accessibilityState={{ selected }}
        style={({ pressed }) => [styles.chip, selected && styles.selected, pressed && styles.pressed]}
      >
        <Text style={[styles.text, selected && styles.textSelected]}>{o.label}</Text>
        {o.caption ? <Text style={[styles.caption, selected && styles.textSelected]}>{o.caption}</Text> : null}
      </Pressable>
    );
  });

  return (
    <View style={styles.wrap}>
      {label ? <Text style={styles.label}>{label}</Text> : null}
      {horizontal ? (
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.row}>
          {chips}
        </ScrollView>
      ) : (
        <View style={[styles.row, styles.wrapRow]}>{chips}</View>
      )}
      {error ? <Text style={styles.error}>{error}</Text> : null}
    </View>
  );
};

const styles = StyleSheet.create({
  wrap: { marginBottom: SPACING.lg },
  label: { ...TYPE.label, marginBottom: SPACING.xs },
  row: { flexDirection: 'row', gap: SPACING.sm },
  wrapRow: { flexWrap: 'wrap' },
  chip: {
    minHeight: 40,
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: COLORS.line,
    borderRadius: BORDER.radius,
    backgroundColor: COLORS.surface,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.xs,
  },
  selected: { backgroundColor: COLORS.ink },
  pressed: { transform: [{ translateY: 1 }] },
  text: { fontSize: 13, fontWeight: '900', letterSpacing: 0.5, color: COLORS.ink, textTransform: 'uppercase' },
  caption: { fontSize: 11, fontWeight: '600', color: COLORS.muted },
  textSelected: { color: COLORS.lime },
  error: { color: COLORS.danger, fontWeight: '800', fontSize: 12, marginTop: SPACING.xs },
});
