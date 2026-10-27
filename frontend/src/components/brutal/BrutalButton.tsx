import React from 'react';
import { Text, StyleSheet, ActivityIndicator, StyleProp, ViewStyle } from 'react-native';
import { BrutalBox } from './BrutalBox';
import { COLORS, MIN_TOUCH, SPACING, TYPE } from '../../theme/tokens';

type Variant = 'primary' | 'dark' | 'outline' | 'danger' | 'accent';

type Props = {
  title: string;
  onPress: () => void;
  variant?: Variant;
  loading?: boolean;
  disabled?: boolean;
  style?: StyleProp<ViewStyle>;
  small?: boolean;
};

const VARIANT_COLORS: Record<Variant, { bg: string; text: string }> = {
  primary: { bg: COLORS.lime, text: COLORS.ink },
  dark: { bg: COLORS.ink, text: COLORS.lime },
  outline: { bg: COLORS.surface, text: COLORS.ink },
  danger: { bg: COLORS.danger, text: COLORS.surface },
  accent: { bg: COLORS.orange, text: COLORS.ink },
};

export const BrutalButton = ({ title, onPress, variant = 'primary', loading, disabled, style, small }: Props) => {
  const c = VARIANT_COLORS[variant];
  const isDisabled = disabled || loading;
  return (
    <BrutalBox
      onPress={onPress}
      disabled={isDisabled}
      color={c.bg}
      style={style}
      contentStyle={[styles.content, small && styles.small]}
      accessibilityLabel={title}
    >
      {loading ? (
        <ActivityIndicator color={c.text} />
      ) : (
        <Text style={[styles.text, small && styles.textSmall, { color: isDisabled ? COLORS.muted : c.text }]} numberOfLines={1}>
          {title.toUpperCase()}
        </Text>
      )}
    </BrutalBox>
  );
};

const styles = StyleSheet.create({
  content: { minHeight: MIN_TOUCH + 4, alignItems: 'center', justifyContent: 'center', paddingHorizontal: SPACING.lg },
  small: { minHeight: 40, paddingHorizontal: SPACING.md },
  text: { ...TYPE.heading, fontSize: 16, letterSpacing: 1 },
  textSmall: { fontSize: 13 },
});
