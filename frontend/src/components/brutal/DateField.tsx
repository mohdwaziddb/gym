import React, { useState } from 'react';
import { View, Text, Pressable, Modal, Platform, StyleSheet } from 'react-native';
import DateTimePicker, { DateTimePickerAndroid, DateTimePickerEvent } from '@react-native-community/datetimepicker';
import { BrutalInput } from './BrutalInput';
import { BrutalButton } from './BrutalButton';
import { COLORS, BORDER, SPACING, TYPE, MIN_TOUCH } from '../../theme/tokens';
import { formatDate, isValidDate, todayString } from '../../utils/dateUtils';

type Props = {
  label: string;
  value: string; // YYYY-MM-DD ya ''
  onChangeText: (t: string) => void;
  error?: string;
  hint?: string;
  minimumDate?: string; // YYYY-MM-DD
  maximumDate?: string; // YYYY-MM-DD
  clearable?: boolean; // optional field (jaise DOB) - khaali kar sakte hain
};

// "2026-10-27" <-> local Date (time 00:00, timezone se din na khiske)
const toDate = (ymd: string): Date => {
  const [y, m, d] = ymd.split('-').map((n) => parseInt(n, 10));
  return new Date(y, m - 1, d);
};
const toYmd = (date: Date): string => {
  const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`);
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

/**
 * DateField - Android pe system calendar dialog, iOS pe calendar sheet.
 * Web pe date picker package kaam nahi karta, wahan YYYY-MM-DD type karne wala field (dash apne aap).
 * Value hamesha YYYY-MM-DD string (backend yahi leta hai).
 */
export const DateField = ({ label, value, onChangeText, error, hint, minimumDate, maximumDate, clearable }: Props) => {
  const [iosOpen, setIosOpen] = useState(false);
  const [iosTemp, setIosTemp] = useState<Date>(new Date());

  const min = minimumDate && isValidDate(minimumDate) ? toDate(minimumDate) : undefined;
  const max = maximumDate && isValidDate(maximumDate) ? toDate(maximumDate) : undefined;
  const current = isValidDate(value) ? toDate(value) : max && toDate(todayString()) > max ? max : new Date();

  if (Platform.OS === 'web') {
    const onType = (t: string) => {
      const digits = t.replace(/[^0-9]/g, '').slice(0, 8);
      let out = digits;
      if (digits.length > 4) out = `${digits.slice(0, 4)}-${digits.slice(4)}`;
      if (digits.length > 6) out = `${digits.slice(0, 4)}-${digits.slice(4, 6)}-${digits.slice(6)}`;
      onChangeText(out);
    };
    return (
      <BrutalInput label={label} value={value} onChangeText={onType} placeholder="YYYY-MM-DD" keyboardType="number-pad" maxLength={10} error={error} hint={hint || 'Format: YYYY-MM-DD'} />
    );
  }

  const open = () => {
    if (Platform.OS === 'android') {
      DateTimePickerAndroid.open({
        value: current,
        mode: 'date',
        minimumDate: min,
        maximumDate: max,
        onChange: (event: DateTimePickerEvent, date?: Date) => {
          if (event.type === 'set' && date) onChangeText(toYmd(date));
        },
      });
    } else {
      setIosTemp(current);
      setIosOpen(true);
    }
  };

  return (
    <View style={styles.wrap}>
      <Text style={styles.label}>{label}</Text>
      <View style={styles.row}>
        <Pressable
          onPress={open}
          accessibilityRole="button"
          accessibilityLabel={`${label}, ${value ? formatDate(value) : 'not set'}`}
          style={({ pressed }) => [styles.field, error ? styles.fieldError : null, pressed && styles.pressed]}
        >
          <Text style={[styles.value, !value && styles.placeholder]}>{value ? formatDate(value) : 'Select date'}</Text>
          <Text style={styles.icon}>{'▦'}</Text>
        </Pressable>
        {clearable && value ? (
          <Pressable onPress={() => onChangeText('')} style={styles.clear} accessibilityRole="button" accessibilityLabel={`Clear ${label}`}>
            <Text style={styles.clearText}>{'✕'}</Text>
          </Pressable>
        ) : null}
      </View>
      {error ? <Text style={styles.error}>{error}</Text> : hint ? <Text style={styles.hint}>{hint}</Text> : null}

      {/* iOS: neeche se calendar sheet, Done pe value set */}
      <Modal visible={iosOpen} transparent animationType="slide" onRequestClose={() => setIosOpen(false)}>
        <Pressable style={styles.overlay} onPress={() => setIosOpen(false)} />
        <View style={styles.sheet}>
          <Text style={styles.sheetTitle}>{label}</Text>
          <DateTimePicker
            value={iosTemp}
            mode="date"
            display="inline"
            minimumDate={min}
            maximumDate={max}
            themeVariant="light"
            accentColor={COLORS.ink}
            onChange={(_e: DateTimePickerEvent, date?: Date) => { if (date) setIosTemp(date); }}
          />
          <View style={styles.sheetActions}>
            <BrutalButton title="Cancel" variant="outline" onPress={() => setIosOpen(false)} style={styles.flex} />
            <BrutalButton title="Done" onPress={() => { onChangeText(toYmd(iosTemp)); setIosOpen(false); }} style={styles.flex} />
          </View>
        </View>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  wrap: { marginBottom: SPACING.lg },
  label: { ...TYPE.label, marginBottom: SPACING.xs },
  row: { flexDirection: 'row', gap: SPACING.sm },
  field: {
    flex: 1,
    minHeight: MIN_TOUCH,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: BORDER.width,
    borderColor: COLORS.line,
    borderRadius: BORDER.radius,
    backgroundColor: COLORS.surface,
    paddingHorizontal: SPACING.md,
  },
  fieldError: { borderColor: COLORS.danger },
  pressed: { backgroundColor: '#FBFFE9' },
  value: { flex: 1, fontSize: 16, fontWeight: '800', color: COLORS.ink },
  placeholder: { color: COLORS.muted, fontWeight: '600' },
  icon: { fontSize: 20, fontWeight: '900', color: COLORS.ink },
  clear: {
    width: MIN_TOUCH,
    minHeight: MIN_TOUCH,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: BORDER.width,
    borderColor: COLORS.line,
    borderRadius: BORDER.radius,
    backgroundColor: COLORS.surface,
  },
  clearText: { fontSize: 16, fontWeight: '900', color: COLORS.ink },
  error: { color: COLORS.danger, fontWeight: '800', fontSize: 12, marginTop: SPACING.xs },
  hint: { ...TYPE.small, marginTop: SPACING.xs },
  overlay: { flex: 1, backgroundColor: COLORS.overlay },
  sheet: { backgroundColor: COLORS.background, borderTopWidth: 3, borderTopColor: COLORS.ink, padding: SPACING.lg, paddingBottom: SPACING.xxl },
  sheetTitle: { ...TYPE.heading, marginBottom: SPACING.sm },
  sheetActions: { flexDirection: 'row', gap: SPACING.sm, marginTop: SPACING.md },
  flex: { flex: 1 },
});
