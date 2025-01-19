package org.jkcw.core;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoomTest {
    public final String id;
    public final String sql;
    public final List<String> params;

    public BoomTest(String testId, String sql, List<String> params) {
        this.id = testId;
        this.sql = String.format("/* id: %s */\n%s", testId, sql);
        this.params = Collections.unmodifiableList(params);
    }

    public Metrics execute(DataSource ds) {
        List<Metrics.Latency> latencies = new ArrayList<>();
        long startNanoTime = System.nanoTime();
        LocalDateTime startTm = LocalDateTime.now();
        try {
            Connection conn = ds.getConnection();
            markLatency(latencies, "timeToConnected", startNanoTime);

            PreparedStatement pstmt = conn.prepareStatement(sql);

            ParameterMetaData metaData = pstmt.getParameterMetaData();
            for (int i = 0; i < metaData.getParameterCount(); i++) {
                String paramValue = params.get(i);
                if (paramValue != null) {
                    try {
                        // Check the SQL type of the parameter
                        int sqlType = metaData.getParameterType(i + 1);
                        switch (sqlType) {
                            case java.sql.Types.INTEGER:
                                pstmt.setInt(i + 1, Integer.parseInt(paramValue));
                                break;
                            case java.sql.Types.DOUBLE:
                                pstmt.setDouble(i + 1, Double.parseDouble(paramValue));
                                break;
                            case java.sql.Types.FLOAT:
                                pstmt.setFloat(i + 1, Float.parseFloat(paramValue));
                                break;
                            case java.sql.Types.VARCHAR:
                            case java.sql.Types.CHAR:
                                pstmt.setString(i + 1, paramValue);
                                break;
                            // Add more cases as needed for different SQL types
                            default:
                                pstmt.setString(i + 1, paramValue);
                                break;
                        }
                    } catch (NumberFormatException e) {
                        // Handle the case where parsing fails
                        System.err.println("Invalid number format for parameter: " + paramValue);
                        pstmt.setString(i + 1, paramValue); // Fallback to setting as string
                    }
                } else {
                    pstmt.setNull(i + 1, metaData.getParameterType(i + 1)); // Set NULL if the value is null
                }
            }
            markLatency(latencies, "timeToSqlPrepared", startNanoTime);

            long recordCount = 0;

            try (ResultSet rs = pstmt.executeQuery()) {
                markLatency(latencies, "timeToSqlExecuted", startNanoTime);
                while (rs.next()) {
                    ++recordCount;
                    if (recordCount == 1) {
                        markLatency(latencies, "timeToFirstRowRead", startNanoTime);
                    }
                }
                markLatency(latencies, "timeToLastRowRead", startNanoTime);
            }

            return new Metrics(id, startTm, recordCount, latencies, null);
        } catch (SQLException e) {
            return new Metrics(id, startTm, -1, new ArrayList<>(), e);
        }
    }

    private static boolean markLatency(List<Metrics.Latency> latencies, String timeToConnected, long startTm) {
        return latencies.add(new Metrics.Latency(timeToConnected, System.nanoTime() - startTm));
    }

    public record Metrics(String id, LocalDateTime startTm, long recordCount, List<Latency> latencies, Exception error) {
        public record Latency(String stage, long elapsedNanoTime) {
        }
    }
}
