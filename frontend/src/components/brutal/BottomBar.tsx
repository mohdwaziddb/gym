import React from 'react';
import { View, Text, Pressable, StyleSheet } from 'react-native';
import { COLORS, MIN_TOUCH, SPACING } from '../../theme/tokens';

export type TabKey = 'Dashboard' | 'Members' | 'Plans' | 'Home';

const TABS: { key: TabKey; label: string; mark: string }[] = [
  { key: 'Dashboard', label: 'DASH', mark: '01' },
  { key: 'Members', label: 'MEMBERS', mark: '02' },
  { key: 'Plans', label: 'PLANS', mark: '03' },
  { key: 'Home', label: 'GYMS', mark: '04' },
];

/**
 * BottomBar - Main screens ke neeche tabs (bina bottom-tabs library ke).
 * Tab pe tap -> stack reset (tab jaisa, back history nahi badhti). 'Home' = Gyms screen.
 */
export const BottomBar = ({ active, navigation }: { active: TabKey; navigation: any }) => {
  const go = (key: TabKey) => {
    if (key === active) return;
    navigation.reset({ index: 0, routes: [{ name: key }] });
  };
  return (
  <View style={styles.bar}>
    {TABS.map((t) => {
      const selected = t.key === active;
      return (
        <Pressable
          key={t.key}
          onPress={() => go(t.key)}
          style={[styles.tab, selected && styles.tabSelected]}
          accessibilityRole="tab"
          accessibilityState={{ selected }}
          accessibilityLabel={t.label}
        >
          <Text style={[styles.mark, selected && styles.textSelected]}>{t.mark}</Text>
          <Text style={[styles.label, selected && styles.textSelected]}>{t.label}</Text>
        </Pressable>
      );
    })}
  </View>
  );
};

const styles = StyleSheet.create({
  bar: { flexDirection: 'row', backgroundColor: COLORS.ink, borderTopWidth: 3, borderTopColor: COLORS.ink, padding: SPACING.xs, gap: SPACING.xs },
  tab: { flex: 1, minHeight: MIN_TOUCH + 6, alignItems: 'center', justifyContent: 'center', borderRadius: 2 },
  tabSelected: { backgroundColor: COLORS.lime },
  mark: { fontSize: 10, fontWeight: '900', color: COLORS.muted },
  label: { fontSize: 12, fontWeight: '900', letterSpacing: 1, color: COLORS.surface },
  textSelected: { color: COLORS.ink },
});
