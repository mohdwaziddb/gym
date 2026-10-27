import React from 'react';
import { View, ScrollView, KeyboardAvoidingView, Platform, StyleSheet, RefreshControl } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { COLORS, SPACING } from '../../theme/tokens';

type Props = {
  children: React.ReactNode;
  scroll?: boolean;
  footer?: React.ReactNode;
  refreshing?: boolean;
  onRefresh?: () => void;
};

/**
 * BrutalScreen - Har naye screen ka base: safe area, concrete background, keyboard avoid,
 * optional scroll + pull-to-refresh, aur neeche fixed footer (BottomBar).
 */
export const BrutalScreen = ({ children, scroll = true, footer, refreshing, onRefresh }: Props) => {
  const insets = useSafeAreaInsets();
  const body = scroll ? (
    <ScrollView
      contentContainerStyle={styles.scrollContent}
      keyboardShouldPersistTaps="handled"
      refreshControl={onRefresh ? <RefreshControl refreshing={!!refreshing} onRefresh={onRefresh} tintColor={COLORS.ink} /> : undefined}
    >
      {children}
    </ScrollView>
  ) : (
    <View style={styles.flexContent}>{children}</View>
  );

  return (
    <View style={[styles.root, { paddingTop: insets.top }]}>
      <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        {body}
      </KeyboardAvoidingView>
      {footer ? <View style={{ paddingBottom: insets.bottom, backgroundColor: COLORS.ink }}>{footer}</View> : <View style={{ height: insets.bottom }} />}
    </View>
  );
};

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: COLORS.background },
  flex: { flex: 1 },
  scrollContent: { padding: SPACING.lg, paddingBottom: SPACING.xxl },
  flexContent: { flex: 1, paddingHorizontal: SPACING.lg, paddingTop: SPACING.lg },
});
