package org.jkcw.core;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class BoomExecutorTest {
    private JdbcDataSource dataSource;

    @Before
    public void setUp() throws SQLException {
        // Create an in-memory database connection
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"); // In-memory database
        // Initialize your database schema and data
        try (Statement stmt = dataSource.getConnection().createStatement()) {
            stmt.execute("CREATE TABLE dummy_table (id INT PRIMARY KEY, name VARCHAR(255));");
            stmt.execute("INSERT INTO dummy_table(id, name) VALUES (1, 'Alice');");
            stmt.execute("INSERT INTO dummy_table(id, name) VALUES (2, 'Bob');");
            stmt.execute("CREATE TABLE mock_table (id INT PRIMARY KEY, name VARCHAR(255));");
            stmt.execute("INSERT INTO mock_table(id, name) VALUES (1, 'Hello');");
            stmt.execute("INSERT INTO mock_table(id, name) VALUES (2, 'World');");
        }
    }

    @Test
    public void executeTestSets() throws IOException, ExecutionException, InterruptedException {
        BoomExecutor executor = new BoomExecutor(1, dataSource);
        List<BoomTest.Metrics> metrics = executor.executeTestSets(Path.of("src/test/resources/tests"));
        assertThat(metrics).hasSize(4);
        assertThat(metrics.get(0).id()).startsWith("1_dummy_test");
        assertThat(metrics.get(1).id()).startsWith("1_dummy_test");
        assertThat(metrics.get(2).id()).startsWith("2_mock_test");
        assertThat(metrics.get(3).id()).startsWith("2_mock_test");
    }
}