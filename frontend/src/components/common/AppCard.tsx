import React from 'react';
import { View, StyleSheet, ViewStyle } from 'react-native';

export const AppCard = ({ children, style }: { children: React.ReactNode; style?: ViewStyle }) => (
  <View style={[styles.card, style]}>{children}</View>
);

const styles = StyleSheet.create({
  card: { backgroundColor: '#fff', borderRadius: 10, padding: 14, marginVertical: 6, elevation: 2, shadowColor: '#000', shadowOpacity: 0.1, shadowRadius: 4 },
});
