/**
 * tokens.ts - IRONBASE Neo-Brutalist design tokens.
 * Naye screens me rang / border / spacing hamesha yahi se lo, hardcode mat karo.
 */

export const COLORS = {
  background: '#F1EEE6', // warm concrete
  surface: '#FFFFFF',
  ink: '#0A0A0A',
  lime: '#C8FF2E',
  orange: '#FF5A1F',
  danger: '#E5332A',
  muted: '#6B6B6B',
  line: '#0A0A0A',
  disabled: '#D9D5CC',
  overlay: 'rgba(10,10,10,0.55)',
};

// Member / membership status ke chip colors
export const STATUS_COLORS: Record<string, { bg: string; text: string; label: string }> = {
  ACTIVE: { bg: COLORS.lime, text: COLORS.ink, label: 'ACTIVE' },
  EXPIRING: { bg: COLORS.orange, text: COLORS.ink, label: 'EXPIRING' },
  EXPIRED: { bg: COLORS.danger, text: COLORS.surface, label: 'EXPIRED' },
  UPCOMING: { bg: COLORS.surface, text: COLORS.ink, label: 'UPCOMING' },
  NO_PLAN: { bg: COLORS.disabled, text: COLORS.ink, label: 'NO PLAN' },
  UNPAID: { bg: COLORS.surface, text: COLORS.danger, label: 'UNPAID' },
  CANCELLED: { bg: COLORS.disabled, text: COLORS.muted, label: 'CANCELLED' },
};

export const BORDER = {
  width: 2.5,
  radius: 4,
};

export const SHADOW = {
  offset: 4,
  pressedOffset: 1,
};

export const SPACING = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  xxl: 32,
};

// System font hi - weight aur uppercase se brutal look (koi font package nahi)
export const TYPE = {
  display: { fontSize: 40, fontWeight: '900' as const, letterSpacing: -1, color: COLORS.ink },
  title: { fontSize: 28, fontWeight: '900' as const, letterSpacing: -0.5, textTransform: 'uppercase' as const, color: COLORS.ink },
  heading: { fontSize: 18, fontWeight: '900' as const, textTransform: 'uppercase' as const, color: COLORS.ink },
  label: { fontSize: 12, fontWeight: '800' as const, letterSpacing: 1, textTransform: 'uppercase' as const, color: COLORS.ink },
  body: { fontSize: 15, fontWeight: '500' as const, color: COLORS.ink },
  small: { fontSize: 12, fontWeight: '600' as const, color: COLORS.muted },
};

// Touch target kam se kam itna
export const MIN_TOUCH = 48;
