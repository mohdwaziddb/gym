/**
 * typeUtils.ts - Ported from backend DataTypeUtility.java (src/main/java/com/backend/utility/DataTypeUtility.java)
 * Single utility for type conversions. Use this everywhere instead of raw parse.
 * AI must read AI_INSTRUCTIONS.md first.
 */

export const stringValue = (obj: any): string => {
  try {
    if (obj === null || obj === undefined) return '';
    const str = String(obj).trim();
    if (str.toLowerCase() === 'null') return '';
    return str;
  } catch {
    return '';
  }
};

export const stringNullValue = (obj: any): string => stringValue(obj);

export const longValue = (obj: any): number | null => {
  try {
    if (obj === null || obj === undefined) return null;
    if (typeof obj === 'number') return Math.trunc(obj);
    const str = String(obj).trim();
    if (!str || str.toLowerCase() === 'null') return null;
    const n = parseInt(str, 10);
    return isNaN(n) ? null : n;
  } catch {
    return null;
  }
};

export const getForeignKeyValue = (obj: any): number | null => longValue(obj);

export const longZeroValue = (obj: any): number => longValue(obj) ?? 0;

export const integerValue = (obj: any): number | null => longValue(obj);

export const integerNullValue = (obj: any): number | null => integerValue(obj);

export const booleanValue = (obj: any): boolean => {
  try {
    if (obj === null || obj === undefined) return false;
    if (typeof obj === 'boolean') return obj;
    const str = String(obj).trim().toLowerCase();
    return str === 'true' || str === '1' || str === 'yes';
  } catch {
    return false;
  }
};

export const floatValue = (obj: any): number | null => {
  try {
    if (obj === null || obj === undefined) return null;
    if (typeof obj === 'number') return obj;
    const n = parseFloat(String(obj).trim());
    return isNaN(n) ? null : n;
  } catch {
    return null;
  }
};

export const floatZeroValue = (obj: any): number => floatValue(obj) ?? 0;

export const doubleValue = (obj: any): number | null => floatValue(obj);
export const doubleZeroValue = (obj: any): number => floatZeroValue(obj);
