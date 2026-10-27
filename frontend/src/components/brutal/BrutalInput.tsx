import React, { useState } from 'react';
import { View, Text, TextInput, Pressable, StyleSheet, KeyboardTypeOptions } from 'react-native';
import { COLORS, BORDER, SPACING, TYPE, MIN_TOUCH } from '../../theme/tokens';

type Props = {
  label: string;
  value: string;
  onChangeText: (t: string) => void;
  placeholder?: string;
  error?: string;
  keyboardType?: KeyboardTypeOptions;
  secureTextEntry?: boolean;
  editable?: boolean;
  multiline?: boolean;
  maxLength?: number;
  autoCapitalize?: 'none' | 'sentences' | 'words' | 'characters';
  hint?: string;
  big?: boolean; // gym code jaisa bada input
  rightAction?: { label: string; onPress: () => void }; // e.g. password SHOW / HIDE
  onSubmitEditing?: () => void;
  returnKeyType?: 'done' | 'next' | 'go';
  autoFocus?: boolean;
};

export const BrutalInput = ({
  label, value, onChangeText, placeholder, error, keyboardType, secureTextEntry,
  editable = true, multiline, maxLength, autoCapitalize, hint, big, rightAction,
  onSubmitEditing, returnKeyType, autoFocus,
}: Props) => {
  const [focused, setFocused] = useState(false);
  return (
    <View style={styles.wrap}>
      <Text style={styles.label}>{label}</Text>
      <View>
      <TextInput
        onSubmitEditing={onSubmitEditing}
        returnKeyType={returnKeyType}
        autoFocus={autoFocus}
        autoCorrect={secureTextEntry || big ? false : undefined}
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={COLORS.muted}
        keyboardType={keyboardType}
        secureTextEntry={secureTextEntry}
        editable={editable}
        multiline={multiline}
        maxLength={maxLength}
        autoCapitalize={autoCapitalize ?? (keyboardType === 'email-address' ? 'none' : 'sentences')}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        accessibilityLabel={label}
        style={[
          styles.input,
          big && styles.big,
          rightAction && styles.withAction,
          multiline && styles.multiline,
          focused && styles.focused,
          !editable && styles.disabled,
          error ? styles.inputError : null,
        ]}
      />
      {rightAction ? (
        <Pressable onPress={rightAction.onPress} style={styles.action} accessibilityRole="button" accessibilityLabel={rightAction.label} hitSlop={6}>
          <Text style={styles.actionText}>{rightAction.label}</Text>
        </Pressable>
      ) : null}
      </View>
      {error ? <Text style={styles.error}>{error}</Text> : hint ? <Text style={styles.hint}>{hint}</Text> : null}
    </View>
  );
};

const styles = StyleSheet.create({
  wrap: { marginBottom: SPACING.lg },
  label: { ...TYPE.label, marginBottom: SPACING.xs },
  input: {
    minHeight: MIN_TOUCH,
    borderWidth: BORDER.width,
    borderColor: COLORS.line,
    borderRadius: BORDER.radius,
    backgroundColor: COLORS.surface,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.sm,
    fontSize: 16,
    fontWeight: '600',
    color: COLORS.ink,
  },
  big: { minHeight: 64, fontSize: 26, fontWeight: '900', letterSpacing: 3 },
  withAction: { paddingRight: 72 },
  action: { position: 'absolute', right: 0, top: 0, bottom: 0, width: 68, alignItems: 'center', justifyContent: 'center', borderLeftWidth: BORDER.width, borderLeftColor: COLORS.line },
  actionText: { fontSize: 12, fontWeight: '900', letterSpacing: 1, color: COLORS.ink },
  multiline: { minHeight: 90, textAlignVertical: 'top' },
  focused: { backgroundColor: '#FBFFE9', borderWidth: BORDER.width + 0.5 },
  disabled: { backgroundColor: COLORS.disabled, color: COLORS.muted },
  inputError: { borderColor: COLORS.danger },
  error: { color: COLORS.danger, fontWeight: '800', fontSize: 12, marginTop: SPACING.xs },
  hint: { ...TYPE.small, marginTop: SPACING.xs },
});
