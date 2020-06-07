package com.zkejid.test.noveltest;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

  public static void main(String[] args) {

    final Path source1Path = Paths.get("source1.txt");
    final Path source2Path = Paths.get("source2.txt");
    final Path outputPath = Paths.get("output.txt");

    Merger m = new Merger(source1Path, source2Path, outputPath);
    m.merge();
  }
}
