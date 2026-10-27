import React from 'react';
import { View, Pressable, StyleSheet, ViewStyle, StyleProp } from 'react-native';
import { COLORS, BORDER, SHADOW } from '../../theme/tokens';

type Props = {
  children: React.ReactNode;
  onPress?: () => void;
  disabled?: boolean;
  color?: string;
  style?: StyleProp<ViewStyle>;
  contentStyle?: StyleProp<ViewStyle>;
  shadow?: boolean;
  accessibilityLabel?: string;
};

/**
 * BrutalBox - Moti black border + bina blur ka hard shadow (peeche ek black box, offset pe).
 * Android elevation hard shadow nahi bana sakta, isliye shadow ek alag View hai - dono platform same dikhte hain.
 * onPress ho to dabane pe box shadow ki taraf khisakta hai (press feedback).
 */
export const BrutalBox = ({ children, onPress, disabled, color = COLORS.surface, style, contentStyle, shadow = true, accessibilityLabel }: Props) => {
  const renderBox = (pressed: boolean) => {
    const shift = pressed ? SHADOW.offset - SHADOW.pressedOffset : 0;
    return (
      <View style={[styles.wrap, shadow && { marginRight: SHADOW.offset, marginBottom: SHADOW.offset }, style]}>
        {shadow ? <View style={[styles.shadow, { top: SHADOW.offset, left: SHADOW.offset }]} /> : null}
        <View
          style={[
            styles.box,
            { backgroundColor: disabled ? COLORS.disabled : color, transform: [{ translateX: shift }, { translateY: shift }] },
            contentStyle,
          ]}
        >
          {children}
        </View>
      </View>
    );
  };

  if (!onPress) return renderBox(false);
  return (
    <Pressable onPress={onPress} disabled={disabled} accessibilityRole="button" accessibilityLabel={accessibilityLabel}>
      {({ pressed }) => renderBox(pressed)}
    </Pressable>
  );
};

const styles = StyleSheet.create({
  wrap: { position: 'relative' },
  shadow: {
    position: 'absolute',
    width: '100%',
    height: '100%',
    backgroundColor: COLORS.ink,
    borderRadius: BORDER.radius,
  },
  box: {
    borderWidth: BORDER.width,
    borderColor: COLORS.line,
    borderRadius: BORDER.radius,
  },
});
