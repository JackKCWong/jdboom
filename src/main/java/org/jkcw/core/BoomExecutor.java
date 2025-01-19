package org.jkcw.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

public class BoomExecutor {
    private static final Logger logger = LoggerFactory.getLogger(BoomExecutor.class);
    private static final Logger metricsLogger = LoggerFactory.getLogger("metrics");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private final DataSource ds;
    private final ExecutorService executorService;

    public BoomExecutor(int concurrency, DataSource ds) {
        this.ds = ds;
        this.executorService = new ForkJoinPool(concurrency);
    }

    public List<BoomTest.Metrics> executeTestSets(Path rootDir) throws IOException, ExecutionException, InterruptedException {
        BoomTestSetLoader loader = new BoomTestSetLoader();
        logger.info("loading test sets: rootDir={}", rootDir.toAbsolutePath());
        List<BoomTestSet> sets = loader.load(rootDir);

        Future<List<BoomTest.Metrics>> task = executorService.submit(() -> sets.parallelStream()
                .flatMap(set -> {
                    // interesting finding: parallelStream does not guarantee to start from 1st element
                    logger.info("executing test set: name={}", set.getName());
                    List<BoomTest.Metrics> metrics = new BoomTestSetExecutor(set, ds).execute();
                    metrics.forEach(m -> {
                        try {
                            metricsLogger.info("{}", objectMapper.writeValueAsString(m));
                        } catch (JsonProcessingException e) {
                            metricsLogger.warn("failed to log metrics", e);
                        }
                    });
                    logger.info("finished test set: name={}", set.getName());
                    return metrics.stream();
                })
                .toList());

        return task.get();
    }
}
