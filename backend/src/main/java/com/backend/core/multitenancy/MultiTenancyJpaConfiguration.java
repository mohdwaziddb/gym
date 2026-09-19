package com.backend.core.multitenancy;

import com.backend.core.DomainInfo;
import com.backend.core.Registry;
import com.backend.core.appconfig.ApplicationConstant;
import com.backend.plateform.tomcat.MysqlDataSourceService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@ComponentScan("com.backend")
@Lazy
@EnableRedisRepositories(basePackages = "com.backend.redis_repo")
public class MultiTenancyJpaConfiguration {


    @Autowired
    private JpaProperties jpaProperties;

    @Value("${microservice.local.database.port:3306}")
    private String localDbPort;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.timeout:6000}")
    private long redisTimeout;


    private HikariDataSource setDynamicDataSource(String jdbcUrl, String username, String password, String driverClassName, String tenant, boolean isread) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);  // JDBC URL for the tenant's database
        config.setUsername(username);  // Database username
        config.setPassword(password);  // Database password'
        config.setDriverClassName(driverClassName); // Driver Class Name

        // Connection pool settings
        config.setMinimumIdle(0);
        config.setMaximumPoolSize(20);  // reduced for local dev
        config.setIdleTimeout(60000);
        config.setConnectionTimeout(10000);  // 10 seconds
        config.setMaxLifetime(900000);
        config.setInitializationFailTimeout(10000); // don't block startup forever
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        // Handle unknown database gracefully in dev: allow app to start even if DB not yet created
        // Hikari will retry; if still fails, exception is thrown but we catch at higher level

        String poolName = tenant + (isread ? "-READ" : "-WRITE");
        config.setPoolName(poolName);
        if (isread) {
            config.setReadOnly(true);
        }
        HikariDataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }


    @Primary
    @Bean(name = "dataSourcesMtApp")
    public Map<String, DataSource> dataSourcesMtApp() {
        Map<String, DataSource> result = new HashMap<>();
        String url = "jdbc:mysql://localhost:" + localDbPort + "/";
        String reader_url = "jdbc:mysql://localhost:" + localDbPort + "/";
        String username = "";
        String password = "";
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        if (Registry.IS_ONLINE) {
            url = Registry.dbmap.get("url");
            reader_url = Registry.dbmap.get("urlreader");
            username = Registry.dbmap.get("username");
            password = Registry.dbmap.get("password");
            driverClassName = "com.mysql.cj.jdbc.Driver";
        } else {
            MysqlDataSourceService.loadDatabaseCredentialsFromLocalHost();
            username = Registry.dbmap.get("username");
            password = Registry.dbmap.get("password");
        }


        HashSet<String> dbset = new HashSet<>();
        if (Registry.IS_ONLINE) {
            for (String tenant_domain : DomainInfo.domainInfoJSON.keySet()) {
                try {
                    JSONObject tenant_json = DomainInfo.domainInfoJSON.get(tenant_domain);
                    if (tenant_json != null && tenant_json.length() > 0) {
                        String tenant = tenant_json.optString("database");
                        if (dbset != null && dbset.size() > 0 && dbset.contains(tenant)) {
                            continue;
                        }
                        dbset.add(tenant);
                        String databaseip = tenant_json.optString("databaseip");
                        if (url.equalsIgnoreCase(databaseip)) {
//                        For writer Instance
                            HikariDataSource dataSourcewrite = setDynamicDataSource(url + tenant, username, password, driverClassName, tenant, false);
                            result.put(tenant, dataSourcewrite);

//                        For reader Instance
                            HikariDataSource dataSourceread = setDynamicDataSource(reader_url + tenant, username, password, driverClassName, tenant, true);
                            result.put(tenant + ApplicationConstant.CONNECTION_READ_STRING, dataSourceread);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else {
            String tenant = Registry.dbmap.get("databasename");
            try {
                HikariDataSource dataSourcewrite = setDynamicDataSource(url + tenant, username, password, driverClassName, tenant, false);
                result.put(tenant, dataSourcewrite);
                HikariDataSource dataSourceread = setDynamicDataSource(reader_url + tenant, username, password, driverClassName, tenant, true);
                result.put(tenant + ApplicationConstant.CONNECTION_READ_STRING, dataSourceread);
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to create DataSource for tenant '" + tenant + "': " + e.getMessage());
                System.err.println("[HINT] Ensure MySQL is running on localhost:" + localDbPort + " and database '" + tenant + "' exists. Run: CREATE DATABASE " + tenant + ";");
                e.printStackTrace();
                // Create placeholder that will fail gracefully on getConnection rather than crashing context
                // But we still throw to make the error visible during startup; comment out throw to allow boot without DB
                // For now, rethrow with clear message
                throw new RuntimeException("Failed to initialize datasource for tenant '" + tenant + "'. Check DB existence and credentials.", e);
            }

        }

        if (result.isEmpty()) {
            System.err.println("[WARN] No tenants resolved - dataSourcesMtApp is empty! Application will fail on DB access. Check DomainInfo or local DB config.");
        } else {
            System.out.println("[INFO] Initialized DataSources for tenants: " + result.keySet());
        }

        return result;
    }

    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider() {
        return new DataSourceBasedMultiTenantConnectionProviderImpl();
    }

    @Bean
    public CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolverImpl();
    }

    @Bean(name = "entityManagerFactoryBean")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
            AbstractDataSourceBasedMultiTenantConnectionProviderImpl multiTenantConnectionProvider,
            CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {

        Map<String, Object> hibernateProps = new LinkedHashMap<>();
        hibernateProps.putAll(this.jpaProperties.getProperties());
        hibernateProps.put("hibernate.multiTenancy", "DATABASE");
        hibernateProps.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        hibernateProps.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        hibernateProps.put("hibernate.hbm2ddl.auto", "none");
        hibernateProps.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        hibernateProps.put("hibernate.enable_lazy_load_no_trans", "true");
        hibernateProps.put("hibernate.naming.implicit-strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl");
        hibernateProps.put("hibernate.naming.physical-strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");

        LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
        result.setPackagesToScan("com.backend");
        result.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        result.setJpaPropertyMap(hibernateProps);
        result.setPersistenceUnitName("schoolEntityManager");

        return result;
    }

    @Bean
    @Primary
    public EntityManagerFactory entityManagerFactory(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return entityManagerFactoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }


    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        try {
            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
            redisStandaloneConfiguration.setHostName(redisHost);
            redisStandaloneConfiguration.setPort(redisPort);
            JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();
            jedisClientConfiguration.connectTimeout(Duration.ofMillis(redisTimeout));
            JedisConnectionFactory factory = new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build());
            // Test connection lazily - don't fail startup if Redis is down
            System.out.println("[INFO] Redis configured at " + redisHost + ":" + redisPort + " (timeout " + redisTimeout + "ms) - will connect lazily");
            return factory;
        } catch (Exception e) {
            System.err.println("[WARN] Failed to configure Redis at " + redisHost + ":" + redisPort + " - Redis will be unavailable: " + e.getMessage());
            e.printStackTrace();
            // Return factory anyway; operations will fail gracefully at runtime
            RedisStandaloneConfiguration fallback = new RedisStandaloneConfiguration(redisHost, redisPort);
            return new JedisConnectionFactory(fallback);
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new JdkSerializationRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        template.afterPropertiesSet();
        return template;
    }
}
