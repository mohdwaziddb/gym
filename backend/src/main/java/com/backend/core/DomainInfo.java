package com.backend.core;

import org.json.JSONObject;

import java.util.LinkedHashMap;

/**
 * DomainInfo - Stores tenant domain -> database mapping.
 * Used only when IS_ONLINE=true (multi-tenant via domain header).
 * Local mode (IS_ONLINE=false) uses single entry from Registry.dbmap directly.
 */
public class DomainInfo {

    public static final LinkedHashMap<String, JSONObject> domainInfoJSON = new LinkedHashMap<>();

    static {
        // Single DB dbgym for local dev as per "ek hai database rakho dbgym"
        // One domain = one database. For multiple gym branches, use separate DBs:
        // e.g. put("lajpat.fitmanage.com", "dbgym_lajpat", "jdbc:mysql://localhost:3306/");
        //      put("noida.fitmanage.com", "dbgym_noida", "jdbc:mysql://localhost:3306/");
        put("local.fitmanage.com", "dbgym", "jdbc:mysql://localhost:3306/");
    }

    public static void put(String domain, String database, String databaseIp) {
        try {
            JSONObject json = new JSONObject();
            json.put("database", database);
            json.put("databaseip", databaseIp);
            domainInfoJSON.put(domain, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Legacy / TSM compatibility - returns JSONObject and also registers map entry
    public static JSONObject getJSONOBJ(String database, String timezone, String version, String gym_code, String gym_group_id, String databaseip, String gym_detail) {
        try {
            JSONObject json = new JSONObject();
            json.put("database", database);
            json.put("timezone", timezone);
            json.put("version", version);
            json.put("gym_code", gym_code);
            json.put("gym_group_id", gym_group_id);
            json.put("databaseip", databaseip);
            json.put("gym_detail", gym_detail);
            // also register by gym_code for backward compat
            domainInfoJSON.put(gym_code, json);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static String getDatabaseName(String domain) {
        try {
            JSONObject obj = domainInfoJSON.get(domain);
            if (obj != null && obj.length() > 0) {
                return obj.optString("database");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
