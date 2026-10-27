import React from 'react';
import { Text, StyleSheet, View } from 'react-native';
import { BrutalBox } from './BrutalBox';
import { COLORS, SPACING, TYPE } from '../../theme/tokens';

type Props = {
  label: string;
  value: string | number;
  caption?: string;
  color?: string;
  onPress?: () => void;
  big?: boolean;
};

/** Dashboard ka bada number tile. onPress se us number ke records khulte hain. */
export const StatTile = ({ label, value, caption, color = COLORS.surface, onPress, big }: Props) => (
  <BrutalBox onPress={onPress} color={color} contentStyle={styles.content} accessibilityLabel={`${label} ${value}`}>
    <Text style={styles.label}>{label}</Text>
    <Text style={[styles.value, big && styles.valueBig]} numberOfLines={1} adjustsFontSizeToFit>
      {value}
    </Text>
    <View style={styles.footer}>
      {caption ? <Text style={styles.caption}>{caption}</Text> : null}
      {onPress ? <Text style={styles.arrow}>{'→'}</Text> : null}
    </View>
  </BrutalBox>
);

const styles = StyleSheet.create({
  content: { padding: SPACING.md, minHeight: 110 },
  label: { ...TYPE.label },
  value: { ...TYPE.display, marginTop: SPACING.xs },
  valueBig: { fontSize: 56 },
  footer: { flexDirection: 'row', alignItems: 'center', marginTop: 'auto' },
  caption: { ...TYPE.small, color: COLORS.ink, flex: 1 },
  arrow: { fontSize: 18, fontWeight: '900', color: COLORS.ink, marginLeft: 'auto' },
});
