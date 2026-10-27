import React, { useState } from 'react';
import { Alert, StyleSheet } from 'react-native';
import { BrutalScreen } from '../components/brutal/BrutalScreen';
import { BrutalHeader } from '../components/brutal/BrutalHeader';
import { BrutalInput } from '../components/brutal/BrutalInput';
import { BrutalButton } from '../components/brutal/BrutalButton';
import { SectionLabel } from '../components/brutal/SectionLabel';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { saveGym } from '../store/gymSlice';
import { stringValue, floatValue } from '../utils/typeUtils';
import { SPACING } from '../theme/tokens';

export const GymFormScreen = ({ navigation, route }: any) => {
  const gym = route?.params?.gym;
  const [name, setName] = useState(stringValue(gym?.gym_name));
  const [code, setCode] = useState(stringValue(gym?.gym_code));
  const [owner, setOwner] = useState(stringValue(gym?.owner_name));
  const [contactNo, setContactNo] = useState(stringValue(gym?.contact_no));
  const [email, setEmail] = useState(stringValue(gym?.email));
  const [address, setAddress] = useState(stringValue(gym?.address));
  const [city, setCity] = useState(stringValue(gym?.city));
  const [monthlyFees, setMonthlyFees] = useState(stringValue(gym?.monthly_fees));
  const [errors, setErrors] = useState<{ name?: string; fees?: string }>({});
  const dispatch = useAppDispatch();
  const { loading } = useAppSelector((s) => s.gym);

  const onSave = async () => {
    const e: typeof errors = {};
    if (!stringValue(name)) e.name = 'Gym name is required';
    if (stringValue(monthlyFees) && floatValue(monthlyFees) === null) e.fees = 'Monthly fees must be a number';
    setErrors(e);
    if (Object.keys(e).length > 0) return;

    // Saari fields bhejo taaki edit me koi purani value khaali na ho
    const payload: any = {
      gym_name: stringValue(name),
      gym_code: stringValue(code).toUpperCase(),
      owner_name: stringValue(owner),
      contact_no: stringValue(contactNo),
      email: stringValue(email),
      address: stringValue(address),
      city: stringValue(city),
      monthly_fees: stringValue(monthlyFees),
    };
    if (gym?.id) payload.id = gym.id;
    const res: any = await dispatch(saveGym(payload));
    if (res.type?.endsWith('/fulfilled')) navigation.goBack();
    else Alert.alert('Failed', stringValue(res.payload) || 'Save failed');
  };

  return (
    <BrutalScreen>
      <BrutalHeader
        section={gym ? `EDIT / ${stringValue(gym.gym_code)}` : '04 / NEW GYM'}
        title={gym ? 'Edit gym' : 'Add gym'}
        subtitle="Fields marked * are required"
        onBack={() => navigation.goBack()}
      />
      <SectionLabel number="01" title="Gym details" />
      <BrutalInput label="Gym name *" value={name} onChangeText={setName} placeholder="FitZone Lajpat" error={errors.name} autoCapitalize="words" />
      {/* gym_code login ki pehchaan hai, edit me change nahi hota */}
      <BrutalInput
        label="Gym code"
        value={code}
        onChangeText={(t) => setCode(t.toUpperCase())}
        placeholder="LAJPAT"
        editable={!gym}
        autoCapitalize="characters"
        hint={gym ? 'Gym code cannot be changed.' : 'Leave blank to auto-generate.'}
      />
      <BrutalInput label="Monthly fees (₹)" value={monthlyFees} onChangeText={setMonthlyFees} placeholder="1500" keyboardType="decimal-pad" error={errors.fees} />

      <SectionLabel number="02" title="Contact" />
      <BrutalInput label="Owner" value={owner} onChangeText={setOwner} placeholder="Amit Sharma" autoCapitalize="words" />
      <BrutalInput label="Contact no" value={contactNo} onChangeText={setContactNo} placeholder="9876543210" keyboardType="phone-pad" maxLength={15} />
      <BrutalInput label="Email" value={email} onChangeText={setEmail} placeholder="gym@example.com" keyboardType="email-address" />
      <BrutalInput label="Address" value={address} onChangeText={setAddress} placeholder="Shop no, street, area" multiline />
      <BrutalInput label="City" value={city} onChangeText={setCity} placeholder="Delhi" autoCapitalize="words" />

      <BrutalButton title={gym ? 'Save changes' : 'Save gym'} onPress={onSave} loading={loading} style={styles.submit} />
    </BrutalScreen>
  );
};

const styles = StyleSheet.create({
  submit: { marginTop: SPACING.md },
});
