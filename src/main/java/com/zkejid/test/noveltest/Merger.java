package com.zkejid.test.noveltest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
  // TODO как обрабатывать пробельные строки?
  // TODO какие могут быть переводы строки?
  private void readFirst() throws IOException {
    ByteBuffer buf = ByteBuffer.allocateDirect(256);
    Charset cs = StandardCharsets.ISO_8859_1;
    try (FileChannel fc = FileChannel.open(source1Path);
        RandomAccessFile raFile = new RandomAccessFile(source1Path.toFile(), "r")) {

      String id = null;

      long totalReadBytes = 0;
      int readBytes;
      StringBuilder sb = new StringBuilder();

      while ((readBytes = fc.read(buf)) != -1) {
        buf.rewind();
        final CharBuffer decoded = cs.decode(buf);
        String currentChunk = decoded.toString();
        buf.clear();

        int indexOf = currentChunk.indexOf("\n");
        if (indexOf != -1) {
          String line = null;
          while (indexOf != -1) {
            sb.append(currentChunk, 0, indexOf);
            // substring + new line character
            totalReadBytes += indexOf + 1;

            currentChunk = currentChunk.substring(indexOf + 1);
            indexOf = currentChunk.indexOf("\n");
            line = sb.toString();
            sb = new StringBuilder();

            if (id == null) {
              id = line;
              final Long oldVal = currentData.put(id, totalReadBytes);
              assert oldVal == null : "not unique id: " + id;
            } else {
              id = null;
            }
          }
        } else {
          sb.append(currentChunk);
          totalReadBytes += readBytes;
        }
      }
    }
  }

  private void readSecondAndWriteOutput() throws IOException {
    try (RandomAccessFile raFile1 = new RandomAccessFile(source1Path.toFile(), "r");
        InputStream in = Files.newInputStream(source2Path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outputPath))) {
      String lineId = null;
      String lineValue = null;
      while ((lineId = reader.readLine()) != null && (lineValue = reader.readLine()) != null) {
        if (currentData.containsKey(lineId)) {
          pw.println(lineId);
          final Long firstFilePointer = currentData.get(lineId);
          raFile1.seek(firstFilePointer);
          final String val1 = raFile1.readLine();
          pw.println(val1);
          pw.println(lineValue);
        }
      }
    }
  }
}
