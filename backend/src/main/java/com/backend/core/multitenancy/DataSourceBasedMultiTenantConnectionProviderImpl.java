package com.backend.core.multitenancy;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Component
public class DataSourceBasedMultiTenantConnectionProviderImpl extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private static final long serialVersionUID = 1L;

    @Autowired
    @Qualifier("dataSourcesMtApp")
    private Map<String, DataSource> dataSourcesMtApp;

    @Override
    protected DataSource selectAnyDataSource() {
        if (this.dataSourcesMtApp == null || this.dataSourcesMtApp.isEmpty()) {
            throw new RuntimeException("No DataSources configured - check microservice.local.database.* properties and ensure DB 'hsr' exists");
        }
        return this.dataSourcesMtApp.values().iterator().next();
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        if (this.dataSourcesMtApp == null || this.dataSourcesMtApp.isEmpty()) {
            throw new RuntimeException("DataSources not initialized for tenant: " + tenantIdentifier);
        }
        DataSource ds = this.dataSourcesMtApp.get(tenantIdentifier);
        if (ds == null) {
            // Fallback: strip _read/_write suffix and retry
            String base = tenantIdentifier;
            if (base != null) {
                base = base.replace("_read", "").replace("_write", "");
                ds = this.dataSourcesMtApp.get(base);
            }
            if (ds == null) {
                // return any as last fallback and log
                System.err.println("[WARN] Tenant '" + tenantIdentifier + "' not found in DataSources, falling back to any. Available: " + this.dataSourcesMtApp.keySet());
                ds = selectAnyDataSource();
            }
        }
        return ds;
    }


    public DataSourceBasedMultiTenantConnectionProviderImpl() {
        super();
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return super.getAnyConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        super.releaseAnyConnection(connection);
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        return super.getConnection(tenantIdentifier);
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        super.releaseConnection(tenantIdentifier, connection);
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return super.supportsAggressiveRelease();
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
        return super.isUnwrappableAs(unwrapType);
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return super.unwrap(unwrapType);
    }
}
