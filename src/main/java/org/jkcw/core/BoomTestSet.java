package org.jkcw.core;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class BoomTestSet {
    private String name;
    private String sql;
    private String params;

    public BoomTestSet(String name, String sql, String params) {
        this.name = name;
        this.sql = sql;
        this.params = params;
    }


    public List<BoomTest> createTests() throws IOException {
        CSVParser params = CSVFormat.DEFAULT.parse(new StringReader(this.params));
        return params.stream().map(this::toTest).toList();
    }

    private BoomTest toTest(CSVRecord rec) {
        List<String> params = rec.stream().toList();
        String testId = this.getName() + ":" + md5sum(params);
        return new BoomTest(testId, sql, params);
    }

    private String md5sum(List<String> strings) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            for (String str : strings) {
                md.update(str.getBytes());
            }
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    public String getName() {
        return this.name;
    }
}


