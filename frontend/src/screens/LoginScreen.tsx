import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, KeyboardAvoidingView, Platform, Pressable } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { BrandBlock } from '../components/brutal/BrandBlock';
import { BrutalInput } from '../components/brutal/BrutalInput';
import { BrutalButton } from '../components/brutal/BrutalButton';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { login } from '../store/authSlice';
import { stringValue } from '../utils/typeUtils';
import { COLORS, BORDER, SPACING, TYPE } from '../theme/tokens';

export const LoginScreen = ({ navigation }: any) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<{ username?: string; password?: string; form?: string }>({});
  const dispatch = useAppDispatch();
  const { loading, gymCode } = useAppSelector((s) => s.auth);
  const insets = useSafeAreaInsets();

  const onLogin = async () => {
    const e: typeof errors = {};
    if (!stringValue(username)) e.username = 'Enter your username';
    if (!stringValue(password)) e.password = 'Enter your password';
    setErrors(e);
    if (Object.keys(e).length > 0) return;

    const code = stringValue(gymCode).toUpperCase();
    const res: any = await dispatch(login({ gymCode: code, username: stringValue(username), password: stringValue(password) }));
    // success pe token Redux me aata hai aur AppNavigator khud Dashboard dikha deta hai
    if (res.type?.endsWith('/rejected')) setErrors({ form: stringValue(res.payload) || 'Login failed' });
  };

  return (
    <View style={[styles.root, { paddingTop: insets.top }]}>
      <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
          <BrandBlock step="STEP 02 / 02" />
          <View style={styles.body}>
            <View style={styles.gymRow}>
              <View style={styles.gymChip}>
                <Text style={styles.gymLabel}>GYM</Text>
                <Text style={styles.gymCode}>{stringValue(gymCode) || '—'}</Text>
              </View>
              <Pressable onPress={() => navigation.goBack()} accessibilityRole="button" accessibilityLabel="Change gym" hitSlop={8}>
                <Text style={styles.change}>CHANGE</Text>
              </Pressable>
            </View>

            <Text style={styles.title}>Sign in</Text>
            <Text style={styles.sub}>Owner, admin and staff accounts.</Text>
            <View style={styles.divider} />

            {errors.form ? (
              <View style={styles.formError}>
                <Text style={styles.formErrorText}>{errors.form}</Text>
              </View>
            ) : null}

            <BrutalInput
              label="Username"
              value={username}
              onChangeText={(t) => { setUsername(t); if (errors.username || errors.form) setErrors({}); }}
              placeholder="Your username"
              autoCapitalize="none"
              error={errors.username}
              returnKeyType="next"
            />
            <BrutalInput
              label="Password"
              value={password}
              onChangeText={(t) => { setPassword(t); if (errors.password || errors.form) setErrors({}); }}
              placeholder="Your password"
              secureTextEntry={!showPassword}
              autoCapitalize="none"
              error={errors.password}
              rightAction={{ label: showPassword ? 'HIDE' : 'SHOW', onPress: () => setShowPassword((v) => !v) }}
              returnKeyType="go"
              onSubmitEditing={onLogin}
            />
            <BrutalButton title="Login →" onPress={onLogin} loading={loading} />
            <Text style={styles.help}>Forgot password? Ask your gym owner to reset it.</Text>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
      <View style={{ height: insets.bottom }} />
    </View>
  );
};

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: COLORS.background },
  flex: { flex: 1 },
  scroll: { flexGrow: 1 },
  body: { padding: SPACING.xl },
  gymRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: SPACING.xl },
  gymChip: { flexDirection: 'row', borderWidth: BORDER.width, borderColor: COLORS.line, backgroundColor: COLORS.surface },
  gymLabel: { ...TYPE.label, backgroundColor: COLORS.ink, color: COLORS.lime, paddingHorizontal: SPACING.sm, paddingVertical: 6 },
  gymCode: { fontSize: 14, fontWeight: '900', letterSpacing: 2, color: COLORS.ink, paddingHorizontal: SPACING.md, paddingVertical: 5 },
  change: { ...TYPE.label, textDecorationLine: 'underline' },
  title: { ...TYPE.title },
  sub: { ...TYPE.small, fontSize: 14, marginTop: SPACING.xs },
  divider: { height: 5, backgroundColor: COLORS.ink, marginVertical: SPACING.lg, width: 64 },
  formError: { borderWidth: BORDER.width, borderColor: COLORS.line, backgroundColor: COLORS.danger, padding: SPACING.md, marginBottom: SPACING.lg },
  formErrorText: { color: COLORS.surface, fontWeight: '900' },
  help: { ...TYPE.small, textAlign: 'center', marginTop: SPACING.lg },
});
