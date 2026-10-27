import React from 'react';
import { TouchableOpacity, Text, StyleSheet, ActivityIndicator, ViewStyle } from 'react-native';
import { APP_COLORS } from '../../utils/constants';

type Props = {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'danger' | 'outline';
  loading?: boolean;
  disabled?: boolean;
  style?: ViewStyle;
};

export const AppButton = ({ title, onPress, variant = 'primary', loading, disabled, style }: Props) => {
  const isDisabled = disabled || loading;
  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={isDisabled}
      style={[
        styles.base,
        variant === 'primary' && styles.primary,
        variant === 'danger' && styles.danger,
        variant === 'outline' && styles.outline,
        isDisabled && styles.disabled,
        style,
      ]}
    >
      {loading ? <ActivityIndicator color={variant === 'outline' ? APP_COLORS.primary : '#fff'} /> : <Text style={[styles.text, variant === 'outline' && styles.textOutline]}>{title}</Text>}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  base: { padding: 14, borderRadius: 8, alignItems: 'center', marginVertical: 6 },
  primary: { backgroundColor: APP_COLORS.primary },
  danger: { backgroundColor: APP_COLORS.danger },
  outline: { backgroundColor: 'transparent', borderWidth: 1, borderColor: APP_COLORS.primary },
  disabled: { opacity: 0.5 },
  text: { color: '#fff', fontWeight: '600' },
  textOutline: { color: APP_COLORS.primary },
});
