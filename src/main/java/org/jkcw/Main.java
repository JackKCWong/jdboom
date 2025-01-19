package org.jkcw;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jkcw.core.BoomExecutor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Properties props = System.getProperties();
        Integer concurrency = (Integer) props.getOrDefault("concurrency", 10);
        String datasourceConfig = (String) props.getOrDefault("datasource", "hikari.properties");

        HikariConfig config = new HikariConfig(datasourceConfig);
        HikariDataSource ds = new HikariDataSource(config);

        String testsetsRoot = args[0];
        new BoomExecutor(concurrency, ds).executeTestSets(Path.of(testsetsRoot));
    }
}