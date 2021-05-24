package com.codelodon.backendscaffold.server.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration("databaseConfig")
@EnableJpaRepositories(
        basePackages = {"com.codelodon.backendscaffold.server.module.*.dao"}
)
public class DatabaseConfig {

    @Autowired
    private Environment env;

    @Bean(name = "dataSource")
    @ConfigurationProperties("spring.datasource.main.hikari")
    public DataSource mainDataSource() {
        return new HikariDataSource();
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

        factory.setDataSource(mainDataSource());
        factory.setPackagesToScan("com.codelodon.backendscaffold.server.module.*.model");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.show_sql", env.getProperty("spring.jpa.show-sql"));
        jpaProperties.put("hibernate.format_sql", env.getProperty("spring.jpa.format-sql"));
        jpaProperties.put("hibernate.use_sql_comments", env.getProperty("spring.jpa.use-sql-comments"));
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        factory.setJpaProperties(jpaProperties);

        return factory;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager mainTransactionManager() {
        EntityManagerFactory factory = mainEntityManagerFactory().getObject();
        return new JpaTransactionManager(factory);
    }
}
