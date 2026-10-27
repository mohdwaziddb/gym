import React, { useState } from 'react';
import { Alert, StyleSheet } from 'react-native';
import { BrutalScreen } from '../components/brutal/BrutalScreen';
import { BrutalHeader } from '../components/brutal/BrutalHeader';
import { BrutalInput } from '../components/brutal/BrutalInput';
import { BrutalButton } from '../components/brutal/BrutalButton';
import { ChipSelect } from '../components/brutal/ChipSelect';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { savePlan } from '../store/planSlice';
import { floatValue, integerValue, stringValue } from '../utils/typeUtils';
import { SPACING } from '../theme/tokens';
import type { MembershipPlan } from '../types';

// Backend MembershipDateCalculator.defaultDurationDays jaisa hi
const DURATION_OPTIONS = [
  { value: 'MONTHLY', label: 'Monthly', caption: '30 days', days: 30 },
  { value: 'QUARTERLY', label: 'Quarterly', caption: '90 days', days: 90 },
  { value: 'HALF_YEARLY', label: 'Half-yearly', caption: '180 days', days: 180 },
  { value: 'YEARLY', label: 'Yearly', caption: '365 days', days: 365 },
  { value: 'CUSTOM', label: 'Custom', caption: 'set days', days: 0 },
];

export const PlanFormScreen = ({ navigation, route }: any) => {
  const plan: MembershipPlan | undefined = route?.params?.plan;
  const dispatch = useAppDispatch();
  const { saving } = useAppSelector((s) => s.plan);

  const [name, setName] = useState(stringValue(plan?.plan_name));
  const [durationType, setDurationType] = useState(stringValue(plan?.duration_type) || 'MONTHLY');
  const [customDays, setCustomDays] = useState(plan?.duration_type === 'CUSTOM' ? stringValue(plan?.duration_days) : '');
  const [price, setPrice] = useState(stringValue(plan?.price));
  const [description, setDescription] = useState(stringValue(plan?.description));
  const [errors, setErrors] = useState<{ name?: string; days?: string; price?: string }>({});

  const onSave = async () => {
    const e: typeof errors = {};
    if (!stringValue(name)) e.name = 'Plan name is required';
    const days = integerValue(customDays);
    if (durationType === 'CUSTOM' && (days === null || days <= 0 || days > 3650)) e.days = 'Enter days between 1 and 3650';
    const p = floatValue(price);
    if (p === null || p < 0) e.price = 'Enter a valid price';
    setErrors(e);
    if (Object.keys(e).length > 0) return;

    const payload: any = {
      plan_name: stringValue(name),
      duration_type: durationType,
      price: stringValue(price),
      description: stringValue(description),
    };
    if (durationType === 'CUSTOM') payload.duration_days = days;
    if (plan?.id) payload.id = plan.id;

    const res: any = await dispatch(savePlan(payload));
    if (res.type?.endsWith('/rejected')) {
      const msg = stringValue(res.payload) || 'Save failed';
      if (msg.toLowerCase().includes('exists')) setErrors({ name: msg });
      else Alert.alert('Failed', msg);
      return;
    }
    navigation.goBack();
  };

  return (
    <BrutalScreen>
      <BrutalHeader
        section={plan ? 'EDIT / PLAN' : '03 / NEW PLAN'}
        title={plan ? 'Edit plan' : 'New plan'}
        subtitle="Changes apply to new assignments only"
        onBack={() => navigation.goBack()}
      />
      <BrutalInput label="Plan name *" value={name} onChangeText={setName} placeholder="Monthly Basic" error={errors.name} autoCapitalize="words" />
      <ChipSelect label="Duration *" options={DURATION_OPTIONS} value={durationType} onChange={setDurationType} />
      {durationType === 'CUSTOM' ? (
        <BrutalInput label="Duration in days *" value={customDays} onChangeText={setCustomDays} placeholder="45" keyboardType="number-pad" maxLength={4} error={errors.days} />
      ) : null}
      <BrutalInput label="Price (₹) *" value={price} onChangeText={setPrice} placeholder="1500" keyboardType="decimal-pad" error={errors.price} />
      <BrutalInput label="Description" value={description} onChangeText={setDescription} placeholder="Gym floor + cardio access" multiline maxLength={500} />
      <BrutalButton title={plan ? 'Save plan' : 'Create plan'} onPress={onSave} loading={saving} style={styles.submit} />
    </BrutalScreen>
  );
};

const styles = StyleSheet.create({
  submit: { marginTop: SPACING.md },
});
