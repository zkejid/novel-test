package com.zkejid.test.noveltest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

  @TempDir
  Path tmpDirectory;

  @Test
  void merge_passInOneBufferRead() throws Exception {
    final Path source1Path = Files.createFile(tmpDirectory.resolve("source1.txt"));
    final Path source2Path = Files.createFile(tmpDirectory.resolve("source2.txt"));

    write(
        source1Path,
        "@one",
        "foo1",
        "@six",
        "baz1",
        "@four",
        "bar1"
    );
    write(
        source2Path,
        "@four",
        "foo1",
        "@two",
        "baz1",
        "@six",
        "bar1"
    );

    Main.main(new String[] {tmpDirectory.toFile().getAbsolutePath(), "256"});

    final byte[] bytes = Files.readAllBytes(tmpDirectory.resolve("output.txt"));
    String value = new String(bytes);
    String expected =
        "@four\n"
            + "bar1\n"
            + "foo1\n"
            + "@six\n"
            + "baz1\n"
            + "bar1\n";

    Assertions.assertEquals(expected, value);
  }

  @Test
  void merge_passInSeveralBufferReads() throws Exception {
    final Path source1Path = Files.createFile(tmpDirectory.resolve("source1.txt"));
    final Path source2Path = Files.createFile(tmpDirectory.resolve("source2.txt"));

    write(
        source1Path,
        "@one",
        "foo1",
        "@six",
        "baz1",
        "@four",
        "bar1"
    );
    write(
        source2Path,
        "@four",
        "foo1",
        "@two",
        "baz1",
        "@six",
        "bar1"
    );

    Main.main(new String[] {tmpDirectory.toFile().getAbsolutePath(), "4"});

    final byte[] bytes = Files.readAllBytes(tmpDirectory.resolve("output.txt"));
    String value = new String(bytes);
    String expected =
        "@four\n"
            + "bar1\n"
            + "foo1\n"
            + "@six\n"
            + "baz1\n"
            + "bar1\n";

    Assertions.assertEquals(expected, value);
  }

  private void write(Path source1Path, String ... lines) throws IOException {
    StringBuilder sb = new StringBuilder();
    for (String line : lines) {
      sb.append(line).append(System.lineSeparator());
    }
    Files.write(source1Path, sb.toString().getBytes());
  }
}