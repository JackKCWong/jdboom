package org.jkcw.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BoomTestSetLoaderTest {

    @org.junit.Test
    public void loadTestSetsFromDir() throws IOException {
        BoomTestSetLoader loader = new BoomTestSetLoader();
        List<BoomTestSet> testsets = loader.load(Path.of("src/test/resources/tests").toAbsolutePath());

        assertThat(testsets).hasSize(2);

        BoomTestSet ts1 = testsets.get(0);
        assertThat(ts1.getName()).isEqualTo("1_dummy_test");

        List<BoomTest> tests1 = ts1.createTests();
        assertThat(tests1).hasSize(2);

        assertThat(tests1.get(0).id).isNotEqualTo(tests1.get(1).id);

        BoomTestSet ts2 = testsets.get(1);
        assertThat(ts2.getName()).isEqualTo("2_mock_test");

        List<BoomTest> tests2 = ts2.createTests();
        assertThat(tests2).hasSize(2);
        assertThat(tests2.get(0).id).isNotEqualTo(tests2.get(1).id);
    }
}