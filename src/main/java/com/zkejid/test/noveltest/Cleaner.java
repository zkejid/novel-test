package com.zkejid.test.noveltest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Cleaner {

  public static void main(String[] args) {

    boolean onlyOutput = false;
    if (args.length > 0) {
      onlyOutput = Boolean.parseBoolean(args[0]);
    }

    final Path source1Path = Paths.get("source1.txt");
    final Path source2Path = Paths.get("source2.txt");
    final Path outputPath = Paths.get("output.txt");

    try {
      if (!onlyOutput) {
        Files.deleteIfExists(source1Path);
        Files.deleteIfExists(source2Path);
      }
      Files.deleteIfExists(outputPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
