package com.backend.utility;

import java.util.LinkedHashMap;
import java.util.Map;

public class SanitizeData {

    public static Map<String, Object> sanitizeMapObj(Map<String, Object> param) {
        if (param == null) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                sanitized.put(entry.getKey(), sanitizeString((String) value));
            } else {
                sanitized.put(entry.getKey(), value);
            }
        }
        return sanitized;
    }

    public static String sanitizeString(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        result = result.replaceAll("(?i)<script.*?>.*?</script.*?>", "");
        result = result.replace("<", "&lt;").replace(">", "&gt;");
        return result;
    }
}
