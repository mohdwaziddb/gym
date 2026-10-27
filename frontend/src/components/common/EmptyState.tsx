import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export const EmptyState = ({ message = 'No data found' }: { message?: string }) => (
  <View style={styles.wrap}>
    <Text style={styles.text}>{message}</Text>
  </View>
);

const styles = StyleSheet.create({
  wrap: { padding: 20, alignItems: 'center' },
  text: { color: '#777' },
});
