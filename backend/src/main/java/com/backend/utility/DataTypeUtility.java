package com.backend.utility;

/**
 * DataTypeUtility - Single utility for type conversions and sanitization.
 * Provides null-safe conversions used across services.
 * Usage: import static com.backend.utility.DataTypeUtility.*;
 */
public class DataTypeUtility {

    private DataTypeUtility() {}

    // -------------------- Long --------------------
    public static Long longValue(Object obj) {
        try {
            if (obj == null) return null;
            if (obj instanceof Number) return ((Number) obj).longValue();
            String str = obj.toString().trim();
            if (str.isEmpty() || str.equalsIgnoreCase("null")) return null;
            return Long.parseLong(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long getForeignKeyValue(Object obj) {
        return longValue(obj);
    }

    public static long longZeroValue(Object obj) {
        Long v = longValue(obj);
        return v == null ? 0L : v;
    }

    // -------------------- Integer --------------------
    public static Integer integerValue(Object obj) {
        try {
            if (obj == null) return null;
            if (obj instanceof Number) return ((Number) obj).intValue();
            String str = obj.toString().trim();
            if (str.isEmpty() || str.equalsIgnoreCase("null")) return null;
            return Integer.parseInt(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer integerNullValue(Object obj) {
        return integerValue(obj);
    }

    // -------------------- String --------------------
    public static String stringValue(Object obj) {
        try {
            if (obj == null) return "";
            String str = obj.toString().trim();
            if (str.equalsIgnoreCase("null")) return "";
            return str;
        } catch (Exception e) {
            return "";
        }
    }

    public static String stringNullValue(Object obj) {
        String v = stringValue(obj);
        return v == null ? "" : v;
    }

    // -------------------- Boolean --------------------
    public static boolean booleanValue(Object obj) {
        try {
            if (obj == null) return false;
            if (obj instanceof Boolean) return (Boolean) obj;
            String str = obj.toString().trim();
            return str.equalsIgnoreCase("true") || str.equals("1") || str.equalsIgnoreCase("yes");
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------- Float / Double --------------------
    public static Float floatValue(Object obj) {
        try {
            if (obj == null) return null;
            if (obj instanceof Number) return ((Number) obj).floatValue();
            return Float.parseFloat(obj.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    public static float floatZeroValue(Object obj) {
        Float v = floatValue(obj);
        return v == null ? 0f : v;
    }

    public static Double doubleValue(Object obj) {
        try {
            if (obj == null) return null;
            if (obj instanceof Number) return ((Number) obj).doubleValue();
            return Double.parseDouble(obj.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    public static double doubleZeroValue(Object obj) {
        Double v = doubleValue(obj);
        return v == null ? 0d : v;
    }
}
