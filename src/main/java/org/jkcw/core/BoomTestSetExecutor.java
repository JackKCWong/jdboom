package org.jkcw.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class BoomTestSetExecutor {
    private static final Logger logger = LoggerFactory.getLogger(BoomTestSetExecutor.class);

    private BoomTestSet testset;
    private DataSource ds;

    public BoomTestSetExecutor(BoomTestSet testset, DataSource ds) {
        this.testset = testset;
        this.ds = ds;
    }

    public List<BoomTest.Metrics> execute() {
        try {
            List<BoomTest> tests = testset.createTests();
            return tests.stream().map(boomTest -> boomTest.execute(ds)).toList();
        } catch (IOException e) {
            logger.warn("failed to create tests from test set: name={}", testset.getName(), e);
        }

        return Collections.emptyList();
    }
}
