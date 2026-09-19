package com.backend.core.multitenancy;

import com.backend.core.DomainInfo;
import com.backend.core.Registry;
import com.backend.core.appconfig.ApplicationConstant;
import com.backend.core.gymcommon.GymRegistryService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.sql.DataSource;
import java.util.Enumeration;
import java.util.Map;

@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired(required = false)
    private GymRegistryService gymRegistryService;

    @Autowired(required = false)
    @Qualifier("dataSourcesMtApp")
    private Map<String, DataSource> dataSourcesMtApp;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        String requestUrl = request.getRequestURL().toString();
        String domain = requestUrl.split("/")[2];

        String databasename = null;

        // 1. Gym Code based tenant (Mobile flow: X-Gym-Code header)
        String gymCode = request.getHeader("X-Gym-Code");
        if (gymCode == null || gymCode.trim().isEmpty()) gymCode = request.getHeader("x-gym-code");
        if (gymCode == null || gymCode.trim().isEmpty()) gymCode = request.getHeader("gym_code");
        if (gymCode == null || gymCode.trim().isEmpty()) gymCode = request.getParameter("gym_code");
        if (gymCode == null || gymCode.trim().isEmpty()) gymCode = request.getParameter("gymCode");

        if (gymCode != null && !gymCode.trim().isEmpty() && gymRegistryService != null) {
            String dbFromGym = gymRegistryService.resolveDbName(gymCode.trim());
            if (dbFromGym != null && !dbFromGym.isEmpty()) {
                databasename = dbFromGym;
                // db_host per gym (local: localhost:3306, live: livehost:3306)
                String dbHost = gymRegistryService.resolveDbHost(gymCode.trim());
                // lazy create datasource if new client DB (e.g., dbgym_noida) not yet in map
                ensureTenantDataSource(databasename, dbHost);
            } else {
                // invalid gym code - for validate endpoint allow to pass, others will get dbgym fallback but service will return failedMessage
                // Keep databasename as is for now, controller will handle invalid gym code
            }
        }

        if (databasename == null) {
            if (Registry.IS_ONLINE) {
                databasename = DomainInfo.getDatabaseName(domain);
            } else {
                databasename = Registry.dbmap.get("databasename");
            }
        }
        // fallback to dbgym if still null
        if (databasename == null || databasename.isEmpty()) databasename = "dbgym";

        Enumeration<String> headers = request.getHeaders(ApplicationConstant.CONNECTION_TYPE_PARAM_NAME);
        String connectionType = null;
        if (headers.hasMoreElements()) {
            connectionType = headers.nextElement();
        }

//            Set Database Name in Request
        request.setAttribute(ApplicationConstant.REQUEST_DATABASE_NAME, databasename);
        request.setAttribute(ApplicationConstant.DATABASE_NAME, databasename);

        if (connectionType != null && !connectionType.isEmpty()) {
            if (connectionType.equalsIgnoreCase(ApplicationConstant.CONNECTION_READ_WRITE_STRING)) {
                connectionType = ApplicationConstant.CONNECTION_WRITE_STRING;
            } else if (connectionType.equalsIgnoreCase(ApplicationConstant.CONNECTION_WRITE_STRING)) {
                connectionType = ApplicationConstant.CONNECTION_WRITE_STRING;
            } else if (connectionType.equalsIgnoreCase(ApplicationConstant.CONNECTION_READ_STRING)) {
                connectionType = ApplicationConstant.CONNECTION_READ_STRING;
            }
        } else {
            String method_name = request.getMethod();
            if (method_name != null && method_name.equalsIgnoreCase("get")) {
                connectionType = ApplicationConstant.CONNECTION_READ_STRING;
            } else {
                connectionType = ApplicationConstant.CONNECTION_WRITE_STRING;
            }
        }

        if (connectionType != null && connectionType.equalsIgnoreCase(ApplicationConstant.CONNECTION_WRITE_STRING)) {
            TenantContextHolder.setTenantId(databasename);
        } else {
            TenantContextHolder.setTenantId(databasename + ApplicationConstant.CONNECTION_READ_STRING);
        }

        MDC.clear();
        MDC.put("url", domain);
        return true;
    }

    private synchronized void ensureTenantDataSource(String tenant, String dbHost) {
        ensureTenantDataSourceWithHost(tenant, dbHost);
    }

    private synchronized void ensureTenantDataSource(String tenant) {
        // Default using Registry host (for backward / DomainInfo flow)
        String host = Registry.dbmap.getOrDefault("url", "localhost:3306");
        ensureTenantDataSourceWithHost(tenant, host);
    }

    private synchronized void ensureTenantDataSourceWithHost(String tenant, String dbHost) {
        if (dataSourcesMtApp == null) return;
        if (dataSourcesMtApp.containsKey(tenant) && dataSourcesMtApp.containsKey(tenant + ApplicationConstant.CONNECTION_READ_STRING)) return;
        try {
            // dbHost may be "localhost:3306" or "jdbc:mysql://host:3306/" or "192.168.1.10:3306"
            String hostPort = dbHost;
            if (hostPort == null || hostPort.trim().isEmpty()) hostPort = Registry.dbmap.getOrDefault("url", "localhost:3306");
            hostPort = hostPort.trim().replace("jdbc:mysql://", "").replace("/", "");
            String jdbcBase = "jdbc:mysql://" + hostPort + "/";
            String username = Registry.dbmap.getOrDefault("username", "root");
            String password = Registry.dbmap.getOrDefault("password", "hrhk");
            String driver = Registry.dbmap.getOrDefault("driverClassName", "com.mysql.cj.jdbc.Driver");

            HikariConfig cfgW = new HikariConfig();
            cfgW.setJdbcUrl(jdbcBase + tenant);
            cfgW.setUsername(username);
            cfgW.setPassword(password);
            cfgW.setDriverClassName(driver);
            cfgW.setMinimumIdle(0);
            cfgW.setMaximumPoolSize(20);
            cfgW.setPoolName(tenant + "-WRITE");
            cfgW.setConnectionTimeout(10000);
            cfgW.setIdleTimeout(60000);

            HikariConfig cfgR = new HikariConfig();
            cfgR.setJdbcUrl(jdbcBase + tenant);
            cfgR.setUsername(username);
            cfgR.setPassword(password);
            cfgR.setDriverClassName(driver);
            cfgR.setMinimumIdle(0);
            cfgR.setMaximumPoolSize(20);
            cfgR.setPoolName(tenant + "-READ");
            cfgR.setReadOnly(true);
            cfgR.setConnectionTimeout(10000);

            // Only put if not exists to avoid race
            if (!dataSourcesMtApp.containsKey(tenant)) {
                dataSourcesMtApp.put(tenant, new HikariDataSource(cfgW));
            }
            if (!dataSourcesMtApp.containsKey(tenant + ApplicationConstant.CONNECTION_READ_STRING)) {
                dataSourcesMtApp.put(tenant + ApplicationConstant.CONNECTION_READ_STRING, new HikariDataSource(cfgR));
            }
            System.out.println("[INFO] Lazy initialized tenant DataSources for: " + tenant);
        } catch (Exception e) {
            System.err.println("[WARN] Failed to lazy init DataSource for tenant " + tenant + ": " + e.getMessage());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContextHolder.clear();
        MDC.clear();
    }
}
