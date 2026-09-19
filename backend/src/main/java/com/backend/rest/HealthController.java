package com.backend.rest;

import com.backend.core.multitenancy.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired(required = false)
    @Qualifier("dataSourcesMtApp")
    private Map<String, DataSource> dataSourcesMtApp;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", "UP");
        map.put("service", "backend");
        map.put("tenant", TenantContextHolder.getTenant());
        if (dataSourcesMtApp != null) {
            map.put("tenants", dataSourcesMtApp.keySet());
            // test DB connection for first tenant
            try {
                DataSource ds = dataSourcesMtApp.values().iterator().next();
                try (Connection c = ds.getConnection()) {
                    map.put("database", c.isValid(2) ? "connected" : "invalid");
                }
            } catch (Exception e) {
                map.put("database", "error: " + e.getMessage());
            }
        }
        return ResponseEntity.ok(map);
    }

    @GetMapping("/")
    public ResponseEntity<?> root() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("message", "Gym Management API is running (DATABASE-per-tenant)");
        map.put("health", "/health");
        map.put("gym_list", "/rest/gym/list");
        map.put("gym_save", "/rest/gym/save?gym_name=FitZone&city=Delhi");
        map.put("gym_saveJson", "/rest/gym/saveJson (POST JSON)");
        map.put("gym_get", "/rest/gym/get?id=1");
        map.put("gym_delete", "/rest/gym/delete?id=1");
        return ResponseEntity.ok(map);
    }
}
