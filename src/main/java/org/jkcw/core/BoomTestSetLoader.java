package org.jkcw.core;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BoomTestSetLoader {
    public List<BoomTestSet> load(Path rootDir) throws IOException {
        ArrayList<BoomTestSet> testSets = new ArrayList<>();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(rootDir)) {
            for (Path entry : paths) {
                String sql = Files.readString(entry.resolve("test.sql"));
                String params = Files.readString(entry.resolve("params.csv"));

                BoomTestSet ts = new BoomTestSet(entry.getFileName().toString(), sql, params);
                testSets.add(ts);
            }
        }

        return testSets.stream().sorted((ts1, ts2) -> ts1.getName().compareTo(ts2.getName())).toList();
    }
}
