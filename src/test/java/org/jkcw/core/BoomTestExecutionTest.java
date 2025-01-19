package org.jkcw.core;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BoomTestExecutionTest {
    private JdbcDataSource dataSource;

    @Before
    public void setUp() throws SQLException {
        // Create an in-memory database connection
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"); // In-memory database
        // Initialize your database schema and data
        try (Statement stmt = dataSource.getConnection().createStatement()) {
            stmt.execute("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(255));");
            stmt.execute("INSERT INTO users (id, name) VALUES (1, 'Alice');");
            stmt.execute("INSERT INTO users (id, name) VALUES (2, 'Bob');");
        }
    }

    @Test
    public void canConvertParamTypesAccordingly() throws SQLException {
        BoomTest bt1 = new BoomTest("mock", "select * from users where id = ? and name = ?", List.of("1", "Alice"));
        BoomTest.Metrics metrics = bt1.execute(dataSource);
        assertThat(metrics.recordCount()).isEqualTo(1);
        assertThat(metrics.latencies()).hasSize(5);

        System.out.println(metrics);
    }
}
