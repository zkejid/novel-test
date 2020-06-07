package com.zkejid.test.noveltest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Merger {

  private final Path source1Path;
  private final Path source2Path;
  private final Path outputPath;

  private final Map<String, Long> currentData;

  public Merger(Path source1Path, Path source2Path, Path outputPath) {

    this.source1Path = source1Path;
    this.source2Path = source2Path;
    this.outputPath = outputPath;
    this.currentData = new ConcurrentHashMap<>();
  }

  public void merge() {
    try {
      readFirst();
      readSecondAndWriteOutput();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // TODO какой чарсет у файлов?
  private void readFirst() throws IOException {
    try (RandomAccessFile raFile = new RandomAccessFile(source1Path.toFile(), "r")) {
      while (raFile.getFilePointer() < raFile.length()) {
        final String id = raFile.readLine();
        final long filePointer = raFile.getFilePointer();
        // skip
        raFile.readLine();
        final Long oldVal = currentData.put(id, filePointer);
        assert oldVal == null : "not unique id: " + id;
      }
    }
  }

  private void readSecondAndWriteOutput() throws IOException {
    try (RandomAccessFile raFile1 = new RandomAccessFile(source1Path.toFile(), "r");
        InputStream in = Files.newInputStream(source2Path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        RandomAccessFile outputFile = new RandomAccessFile(outputPath.toFile(), "rw")) {
      String lineId = null;
      String lineValue = null;
      while ((lineId = reader.readLine()) != null && (lineValue = reader.readLine()) != null) {
        if (currentData.containsKey(lineId)) {
          outputFile.writeBytes(lineId);
          outputFile.writeBytes(System.lineSeparator());
          final Long firstFilePointer = currentData.get(lineId);
          raFile1.seek(firstFilePointer);
          final String val1 = raFile1.readLine();
          outputFile.writeBytes(val1);
          outputFile.writeBytes(System.lineSeparator());
          outputFile.writeBytes(lineValue);
          outputFile.writeBytes(System.lineSeparator());
        }
      }
    }
  }
}
