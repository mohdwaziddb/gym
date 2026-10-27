package com.backend.plateform.tomcat;

import com.backend.core.Registry;
import com.backend.utility.DataTypeUtility;

import java.util.HashMap;

/**
 * MysqlDataSourceService - Loads local DB credentials into Registry.dbmap.
 * <p>
 * <b>IMPORTANT: Local DB credentials yaha se uthenge, application.properties se nahi.</b><br>
 * - application.properties me microservice.local.database.* sirf reference / documentation ke liye hai.<br>
 * - Actual runtime me {@link #loadDatabaseCredentialsFromLocalHost()} ko
 *   {@link com.backend.core.multitenancy.MultiTenancyJpaConfiguration#dataSourcesMtApp()} call karta hai
 *   aur Registry.dbmap me url/username/password/databasename set karta hai.<br>
 * - Database ka naam yaha {@code dbgym} hardcoded hai (single DB mode).<br>
 * - Naya client aayega to yaha db ka naam change nahi karna, balki
 *   {@code gymcommon.gym_registry} me uska db_name alag banega
 *   (e.g. dbgym_noida) aur {@link com.backend.core.multitenancy.RequestInterceptor#ensureTenantDataSource} lazy create karega.<br>
 * - Isliye local DB change karna ho to sirf is class me badlo, application.properties automatic nahi uthega.
 * <p>
 * For DATABASE-per-tenant, each tenant has its own MySQL schema (e.g. dbgym, dbgym_noida)
 * This class only loads default credentials; actual Hikari pools are created in MultiTenancyJpaConfiguration.
 */
public class MysqlDataSourceService {

    private MysqlDataSourceService() {}

    /**
     * Loads default local MySQL credentials.
     * Env variables DB_URL / DB_USERNAME / DB_PASSWORD set hon to wahi use honge (server pe password code me na rahe),
     * warna local fallback (localhost:3306 / root / hrhk).
     */
    public static HashMap<String, String> loadDatabaseCredentialsFromLocalHost() {
        Registry.dbmap.put("url", envOrDefault("DB_URL", "localhost:3306"));
        Registry.dbmap.put("username", envOrDefault("DB_USERNAME", "root"));
        Registry.dbmap.put("password", envOrDefault("DB_PASSWORD", "hrhk"));
        Registry.dbmap.put("driverClassName", "com.mysql.cj.jdbc.Driver");
        Registry.dbmap.put("databasename", "dbgym");
        return Registry.dbmap;
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = DataTypeUtility.stringValue(System.getenv(name));
        return value.isEmpty() ? defaultValue : value;
    }
}
