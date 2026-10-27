import React from 'react';
import { ActivityIndicator, View, StyleSheet, Text } from 'react-native';
import { APP_COLORS } from '../../utils/constants';

type Props = { visible: boolean; text?: string; overlay?: boolean };

export const Loader = ({ visible, text, overlay = true }: Props) => {
  if (!visible) return null;
  return (
    <View style={[styles.container, overlay && styles.overlay]}>
      <ActivityIndicator size="large" color={APP_COLORS.primary} />
      {text ? <Text style={styles.text}>{text}</Text> : null}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { padding: 20, alignItems: 'center', justifyContent: 'center' },
  overlay: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(255,255,255,0.7)', zIndex: 99 },
  text: { marginTop: 8, color: APP_COLORS.text },
});
