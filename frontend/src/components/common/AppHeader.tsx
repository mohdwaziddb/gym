import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { APP_COLORS } from '../../utils/constants';

export const AppHeader = ({ title, subtitle }: { title: string; subtitle?: string }) => (
  <View style={styles.wrap}>
    <Text style={styles.title}>{title}</Text>
    {subtitle ? <Text style={styles.sub}>{subtitle}</Text> : null}
  </View>
);

const styles = StyleSheet.create({
  wrap: { padding: 16, backgroundColor: APP_COLORS.primary },
  title: { color: '#fff', fontSize: 20, fontWeight: '700' },
  sub: { color: '#e8f5e9', marginTop: 4 },
});
