package com.backend.core.gymcommon;

import com.backend.core.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * GymRegistryService - Lookup gym_code in gymcommon.gym_registry and return db_name.
 */
@Service
public class GymRegistryService {

    @Autowired
    @Qualifier("gymcommonJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * Returns db_name for given gym_code if ACTIVE, else null.
     */
    public String resolveDbName(String gymCode) {
        if (gymCode == null || gymCode.trim().isEmpty()) return null;
        try {
            String sql = "SELECT db_name FROM gymcommon.gym_registry WHERE gym_code=? AND status='ACTIVE' LIMIT 1";
            // try upper and original
            try {
                return jdbcTemplate.queryForObject(sql, String.class, gymCode.trim().toUpperCase());
            } catch (Exception e) {
                return jdbcTemplate.queryForObject(sql, String.class, gymCode.trim());
            }
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> getGymInfo(String gymCode) {
        try {
            String sql = "SELECT gym_code, gym_name, db_name, db_host, status FROM gymcommon.gym_registry WHERE gym_code=? LIMIT 1";
            try {
                return jdbcTemplate.queryForMap(sql, gymCode.trim().toUpperCase());
            } catch (Exception ex) {
                return jdbcTemplate.queryForMap(sql, gymCode.trim());
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns db_host for given gym_code.
     * If IS_ONLINE=false → hamesha local (localhost:3306), chahe DB me live host bhi ho.
     * If IS_ONLINE=true  → live host from gymcommon.gym_registry.db_host (e.g. db1.edu.net:3306).
     */
    public String resolveDbHost(String gymCode) {
        if (!Registry.IS_ONLINE) {
            return "localhost:3306";
        }
        Map<String, Object> info = getGymInfo(gymCode);
        if (info != null && info.get("db_host") != null) {
            String host = info.get("db_host").toString().trim();
            if (!host.isEmpty()) {
                // db_host may be "localhost:3306" or "jdbc:mysql://db1.edu.net:3306/"
                return host.replace("jdbc:mysql://", "").replace("/", "");
            }
        }
        return "localhost:3306";
    }

    public boolean isValidGymCode(String gymCode) {
        return resolveDbName(gymCode) != null;
    }
}
