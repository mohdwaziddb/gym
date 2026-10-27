import React from 'react';
import { TextInput, View, Text, StyleSheet, KeyboardTypeOptions } from 'react-native';
import { APP_COLORS } from '../../utils/constants';

type Props = {
  label?: string;
  value: string;
  onChangeText: (t: string) => void;
  placeholder?: string;
  secureTextEntry?: boolean;
  error?: string;
  keyboardType?: KeyboardTypeOptions;
  editable?: boolean;
  multiline?: boolean;
};

export const AppInput = ({ label, value, onChangeText, placeholder, secureTextEntry, error, keyboardType, editable = true, multiline }: Props) => (
  <View style={styles.wrap}>
    {label ? <Text style={styles.label}>{label}</Text> : null}
    <TextInput
      value={value}
      onChangeText={onChangeText}
      placeholder={placeholder}
      secureTextEntry={secureTextEntry}
      keyboardType={keyboardType}
      editable={editable}
      multiline={multiline}
      autoCapitalize={keyboardType === 'email-address' ? 'none' : undefined}
      style={[styles.input, !editable ? styles.inputDisabled : null, error ? styles.inputError : null]}
      placeholderTextColor={APP_COLORS.grey}
    />
    {error ? <Text style={styles.error}>{error}</Text> : null}
  </View>
);

const styles = StyleSheet.create({
  wrap: { marginVertical: 8 },
  label: { marginBottom: 4, fontWeight: '500', color: APP_COLORS.text },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 8, padding: 12, backgroundColor: '#fff' },
  inputDisabled: { backgroundColor: '#eee', color: APP_COLORS.grey },
  inputError: { borderColor: APP_COLORS.danger },
  error: { color: APP_COLORS.danger, marginTop: 4, fontSize: 12 },
});
