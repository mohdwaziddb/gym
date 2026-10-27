import React, { useState } from 'react';
import { Alert, StyleSheet, View } from 'react-native';
import { BrutalScreen } from '../components/brutal/BrutalScreen';
import { BrutalHeader } from '../components/brutal/BrutalHeader';
import { BrutalInput } from '../components/brutal/BrutalInput';
import { BrutalButton } from '../components/brutal/BrutalButton';
import { ChipSelect } from '../components/brutal/ChipSelect';
import { DateField } from '../components/brutal/DateField';
import { SectionLabel } from '../components/brutal/SectionLabel';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { useRole } from '../hooks/useRole';
import { saveMember } from '../store/memberSlice';
import { stringValue } from '../utils/typeUtils';
import { addDays, isValidDate, todayString } from '../utils/dateUtils';
import { SPACING } from '../theme/tokens';
import type { MemberDetail } from '../types';

const GENDER_OPTIONS = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' },
];

const PHONE_REGEX = /^[6-9][0-9]{9}$/;
const EMAIL_REGEX = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

type Errors = Partial<Record<'full_name' | 'phone' | 'email' | 'dob' | 'joining_date' | 'emergency_phone', string>>;

export const MemberFormScreen = ({ navigation, route }: any) => {
  const member: MemberDetail | undefined = route?.params?.member;
  const dispatch = useAppDispatch();
  const { saving } = useAppSelector((s) => s.member);
  const { isManager } = useRole();

  const [fullName, setFullName] = useState(stringValue(member?.full_name));
  const [phone, setPhone] = useState(stringValue(member?.phone));
  const [email, setEmail] = useState(stringValue(member?.email));
  const [gender, setGender] = useState(stringValue(member?.gender));
  const [dob, setDob] = useState(stringValue(member?.dob));
  const [joiningDate, setJoiningDate] = useState(stringValue(member?.joining_date) || todayString());
  const [address, setAddress] = useState(stringValue(member?.address));
  const [emergencyName, setEmergencyName] = useState(stringValue(member?.emergency_name));
  const [emergencyPhone, setEmergencyPhone] = useState(stringValue(member?.emergency_phone));
  const [notes, setNotes] = useState(stringValue(member?.notes));
  const [errors, setErrors] = useState<Errors>({});

  const validate = (): Errors => {
    const e: Errors = {};
    if (!stringValue(fullName)) e.full_name = 'Name is required';
    if (!PHONE_REGEX.test(stringValue(phone))) e.phone = 'Enter a valid 10 digit mobile number';
    if (stringValue(email) && !EMAIL_REGEX.test(stringValue(email))) e.email = 'Enter a valid email';
    if (stringValue(dob) && (!isValidDate(dob) || dob >= todayString())) e.dob = 'Enter a past date as YYYY-MM-DD';
    if (!isValidDate(joiningDate)) e.joining_date = 'Enter date as YYYY-MM-DD';
    if (stringValue(emergencyPhone) && !PHONE_REGEX.test(stringValue(emergencyPhone))) e.emergency_phone = 'Enter a valid 10 digit number';
    return e;
  };

  const onSave = async () => {
    const e = validate();
    setErrors(e);
    if (Object.keys(e).length > 0) return;

    const payload: any = {
      full_name: stringValue(fullName),
      phone: stringValue(phone),
      email: stringValue(email),
      gender: stringValue(gender),
      dob: stringValue(dob),
      joining_date: stringValue(joiningDate),
      address: stringValue(address),
      emergency_name: stringValue(emergencyName),
      emergency_phone: stringValue(emergencyPhone),
    };
    if (isManager) payload.notes = stringValue(notes);
    if (member?.id) payload.id = member.id;

    const res: any = await dispatch(saveMember(payload));
    if (res.type?.endsWith('/rejected')) {
      // Duplicate phone jaisa backend error phone field pe dikhao
      const msg = stringValue(res.payload) || 'Save failed';
      if (msg.toLowerCase().includes('phone')) setErrors({ phone: msg });
      else Alert.alert('Failed', msg);
      return;
    }
    if (member?.id) navigation.goBack();
    else navigation.replace('MemberDetail', { id: res.payload?.id });
  };

  return (
    <BrutalScreen>
      <BrutalHeader
        section={member ? `EDIT / ${member.member_code}` : '02 / NEW MEMBER'}
        title={member ? 'Edit member' : 'Add member'}
        subtitle="Fields marked * are required"
        onBack={() => navigation.goBack()}
      />

      <SectionLabel number="01" title="Basic details" />
      <BrutalInput label="Full name *" value={fullName} onChangeText={setFullName} placeholder="Rahul Verma" error={errors.full_name} autoCapitalize="words" />
      <BrutalInput label="Mobile *" value={phone} onChangeText={setPhone} placeholder="9876543210" keyboardType="phone-pad" maxLength={10} error={errors.phone} />
      <BrutalInput label="Email" value={email} onChangeText={setEmail} placeholder="rahul@example.com" keyboardType="email-address" error={errors.email} />
      <ChipSelect label="Gender" options={GENDER_OPTIONS} value={gender} onChange={(v) => setGender(v === gender ? '' : v)} />
      <DateField label="Date of birth" value={dob} onChangeText={setDob} error={errors.dob} maximumDate={addDays(todayString(), -1)} clearable />
      <DateField label="Joining date *" value={joiningDate} onChangeText={setJoiningDate} error={errors.joining_date} maximumDate={todayString()} />
      <BrutalInput label="Address" value={address} onChangeText={setAddress} placeholder="House no, street, area" multiline />

      <SectionLabel number="02" title="Emergency contact" />
      <BrutalInput label="Contact name" value={emergencyName} onChangeText={setEmergencyName} placeholder="Sunita Verma" autoCapitalize="words" />
      <BrutalInput label="Contact mobile" value={emergencyPhone} onChangeText={setEmergencyPhone} placeholder="9876543210" keyboardType="phone-pad" maxLength={10} error={errors.emergency_phone} />

      {isManager ? (
        <>
          <SectionLabel number="03" title="Private notes" />
          <BrutalInput
            label="Medical / injury notes"
            value={notes}
            onChangeText={setNotes}
            placeholder="e.g. Knee injury - avoid heavy squats"
            multiline
            hint="Visible only to owner and admin."
          />
        </>
      ) : null}

      <View style={styles.actions}>
        <BrutalButton title={member ? 'Save changes' : 'Register member'} onPress={onSave} loading={saving} />
      </View>
    </BrutalScreen>
  );
};

const styles = StyleSheet.create({
  actions: { marginTop: SPACING.md },
});
