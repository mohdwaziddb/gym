import React, { useEffect, useRef } from 'react';
import { Animated, View, StyleSheet } from 'react-native';
import { COLORS, BORDER, SPACING } from '../../theme/tokens';

/** List load hote waqt placeholder cards (halka pulse) */
export const Skeleton = ({ rows = 4, height = 72 }: { rows?: number; height?: number }) => {
  const opacity = useRef(new Animated.Value(0.4)).current;

  useEffect(() => {
    const loop = Animated.loop(
      Animated.sequence([
        Animated.timing(opacity, { toValue: 1, duration: 600, useNativeDriver: true }),
        Animated.timing(opacity, { toValue: 0.4, duration: 600, useNativeDriver: true }),
      ]),
    );
    loop.start();
    return () => loop.stop();
  }, [opacity]);

  return (
    <View accessibilityLabel="Loading">
      {Array.from({ length: rows }).map((_, i) => (
        <Animated.View key={i} style={[styles.row, { height, opacity }]}>
          <View style={styles.lineLong} />
          <View style={styles.lineShort} />
        </Animated.View>
      ))}
    </View>
  );
};

const styles = StyleSheet.create({
  row: {
    borderWidth: BORDER.width,
    borderColor: COLORS.disabled,
    borderRadius: BORDER.radius,
    backgroundColor: COLORS.surface,
    padding: SPACING.md,
    marginBottom: SPACING.md,
    justifyContent: 'center',
  },
  lineLong: { height: 14, width: '60%', backgroundColor: COLORS.disabled, marginBottom: SPACING.sm },
  lineShort: { height: 10, width: '35%', backgroundColor: COLORS.disabled },
});
