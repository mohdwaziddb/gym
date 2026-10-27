import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, KeyboardAvoidingView, Platform } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { BrandBlock } from '../components/brutal/BrandBlock';
import { BrutalInput } from '../components/brutal/BrutalInput';
import { BrutalButton } from '../components/brutal/BrutalButton';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { validateGym } from '../store/authSlice';
import { stringValue } from '../utils/typeUtils';
import { COLORS, SPACING, TYPE } from '../theme/tokens';

export const GymCodeScreen = ({ navigation }: any) => {
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const dispatch = useAppDispatch();
  const { loading } = useAppSelector((s) => s.auth);
  const insets = useSafeAreaInsets();

  const onNext = async () => {
    const c = stringValue(code).toUpperCase().trim();
    if (!c) return setError('Enter your gym code');
    setError('');
    const res: any = await dispatch(validateGym(c));
    if (res.type?.endsWith('/fulfilled')) navigation.navigate('Login');
    else setError(stringValue(res.payload) || 'Invalid gym code');
  };

  return (
    <View style={[styles.root, { paddingTop: insets.top }]}>
      <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
          <BrandBlock step="STEP 01 / 02" />
          <View style={styles.body}>
            <Text style={styles.title}>Find your gym</Text>
            <Text style={styles.sub}>Enter the gym code shared by your gym owner.</Text>
            <View style={styles.divider} />
            <BrutalInput
              label="Gym code"
              value={code}
              onChangeText={(t) => { setCode(t.toUpperCase()); if (error) setError(''); }}
              placeholder="LAJPAT"
              autoCapitalize="characters"
              maxLength={50}
              big
              error={error || undefined}
              returnKeyType="go"
              onSubmitEditing={onNext}
            />
            <BrutalButton title="Continue →" onPress={onNext} loading={loading} />
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
      <Text style={[styles.footer, { paddingBottom: insets.bottom + SPACING.md }]}>SECURE · MULTI-GYM · BUILT FOR INDIA</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: COLORS.background },
  flex: { flex: 1 },
  scroll: { flexGrow: 1 },
  body: { padding: SPACING.xl },
  title: { ...TYPE.title },
  sub: { ...TYPE.small, fontSize: 14, marginTop: SPACING.xs },
  divider: { height: 5, backgroundColor: COLORS.ink, marginVertical: SPACING.lg, width: 64 },
  footer: { ...TYPE.label, fontSize: 10, color: COLORS.muted, textAlign: 'center', paddingTop: SPACING.md },
});
