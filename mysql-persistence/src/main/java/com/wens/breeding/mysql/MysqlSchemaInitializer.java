package com.wens.breeding.mysql;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

public final class MysqlSchemaInitializer {
    private final DataSource dataSource;

    public MysqlSchemaInitializer(DataSource dataSource) {
        this.dataSource = java.util.Objects.requireNonNull(dataSource, "dataSource");
    }

    public void initialize() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/mysql/schema.sql"));
        populator.execute(dataSource);
    }
}
