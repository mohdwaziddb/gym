import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { COLORS, SPACING } from '../../theme/tokens';

// Product naam ek jagah - branding badalni ho to sirf yaha
export const BRAND_NAME = 'IRONBASE';
export const BRAND_TAGLINE = 'GYM OPERATING SYSTEM';

/** Login flow ke upar wala bada brand block (black slab + lime) */
export const BrandBlock = ({ step }: { step: string }) => (
  <View style={styles.wrap}>
    <View style={styles.topRow}>
      <View style={styles.logo}>
        <Text style={styles.logoText}>IB</Text>
      </View>
      <Text style={styles.step}>{step}</Text>
    </View>
    <Text style={styles.name}>{BRAND_NAME}</Text>
    <View style={styles.tagRow}>
      <View style={styles.bar} />
      <Text style={styles.tag}>{BRAND_TAGLINE}</Text>
    </View>
  </View>
);

const styles = StyleSheet.create({
  wrap: { backgroundColor: COLORS.ink, padding: SPACING.xl, paddingTop: SPACING.lg },
  topRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: SPACING.xl },
  logo: { width: 44, height: 44, backgroundColor: COLORS.lime, alignItems: 'center', justifyContent: 'center', transform: [{ rotate: '-6deg' }] },
  logoText: { fontSize: 20, fontWeight: '900', color: COLORS.ink },
  step: { fontSize: 12, fontWeight: '900', letterSpacing: 1.5, color: COLORS.lime },
  name: { fontSize: 52, fontWeight: '900', letterSpacing: -2, color: COLORS.surface },
  tagRow: { flexDirection: 'row', alignItems: 'center', gap: SPACING.sm, marginTop: SPACING.xs },
  bar: { width: 28, height: 6, backgroundColor: COLORS.orange },
  tag: { fontSize: 12, fontWeight: '800', letterSpacing: 2, color: COLORS.disabled },
});
