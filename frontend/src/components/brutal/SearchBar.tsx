import React from 'react';
import { View, TextInput, Text, Pressable, StyleSheet } from 'react-native';
import { COLORS, BORDER, SPACING, MIN_TOUCH } from '../../theme/tokens';

type Props = { value: string; onChangeText: (t: string) => void; placeholder?: string };

export const SearchBar = ({ value, onChangeText, placeholder = 'Search name, phone, member ID' }: Props) => (
  <View style={styles.wrap}>
    <Text style={styles.icon}>{'⌕'}</Text>
    <TextInput
      value={value}
      onChangeText={onChangeText}
      placeholder={placeholder}
      placeholderTextColor={COLORS.muted}
      style={styles.input}
      autoCorrect={false}
      autoCapitalize="none"
      returnKeyType="search"
      accessibilityLabel="Search"
    />
    {value ? (
      <Pressable onPress={() => onChangeText('')} hitSlop={10} accessibilityRole="button" accessibilityLabel="Clear search">
        <Text style={styles.clear}>{'✕'}</Text>
      </Pressable>
    ) : null}
  </View>
);

const styles = StyleSheet.create({
  wrap: {
    flexDirection: 'row',
    alignItems: 'center',
    minHeight: MIN_TOUCH,
    borderWidth: BORDER.width,
    borderColor: COLORS.line,
    borderRadius: BORDER.radius,
    backgroundColor: COLORS.surface,
    paddingHorizontal: SPACING.md,
    marginBottom: SPACING.md,
  },
  icon: { fontSize: 20, fontWeight: '900', marginRight: SPACING.sm, color: COLORS.ink },
  input: { flex: 1, fontSize: 16, fontWeight: '600', color: COLORS.ink, paddingVertical: SPACING.sm },
  clear: { fontSize: 16, fontWeight: '900', color: COLORS.ink, paddingHorizontal: SPACING.xs },
});
