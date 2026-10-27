/**
 * dateUtils.ts - Date helpers (backend YYYY-MM-DD bhejta / leta hai).
 * Calculation UTC me taaki timezone se din aage-peeche na ho.
 */
import { stringValue, floatValue } from './typeUtils';

/** "2026-10-27" format + asli calendar date */
export const isValidDate = (value: string): boolean => {
  const v = stringValue(value);
  if (!/^\d{4}-\d{2}-\d{2}$/.test(v)) return false;
  const d = new Date(`${v}T00:00:00Z`);
  return !isNaN(d.getTime()) && d.toISOString().slice(0, 10) === v;
};

/** Aaj ki local date YYYY-MM-DD */
export const todayString = (): string => {
  const d = new Date();
  const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

/** date + days -> YYYY-MM-DD */
export const addDays = (date: string, days: number): string => {
  const d = new Date(`${date}T00:00:00Z`);
  d.setUTCDate(d.getUTCDate() + days);
  return d.toISOString().slice(0, 10);
};

/** "2026-10-27" -> "27 OCT 2026" */
export const formatDate = (value: string): string => {
  const v = stringValue(value).slice(0, 10);
  if (!isValidDate(v)) return v || '—';
  const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'];
  const [y, m, d] = v.split('-');
  return `${d} ${months[parseInt(m, 10) - 1]} ${y}`;
};

/** 1300 -> "₹1,300" (Indian grouping) */
export const formatMoney = (value: any): string => {
  const n = floatValue(value) ?? 0;
  return `₹${n.toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;
};
