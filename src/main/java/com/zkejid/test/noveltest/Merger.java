package com.zkejid.test.noveltest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Merger {

  private final Path source1Path;
  private final Path source2Path;
  private final Path outputPath;

  private final Map<Integer, Set<FileEntry>> currentData;

  public Merger(Path source1Path, Path source2Path, Path outputPath) {

    this.source1Path = source1Path;
    this.source2Path = source2Path;
    this.outputPath = outputPath;
    this.currentData = new ConcurrentHashMap<>();
  }

  public void merge(int bufferSize) {

    try {
      readFirst(bufferSize);
      readSecondAndWriteOutput(bufferSize);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // TODO какой чарсет у файлов?
  // TODO как обрабатывать пробельные строки?
  // TODO какие могут быть переводы строки?
  private void readFirst(int bufferSize) throws IOException {

    ByteBuffer buf = ByteBuffer.allocateDirect(bufferSize);
    Charset cs = StandardCharsets.ISO_8859_1;
    try (FileChannel fc = FileChannel.open(source1Path)) {

      String id = null;

      long previousLinePoint = 0;
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
              final int hashCode = id.hashCode();
              currentData.computeIfAbsent(
                  hashCode,
                  (k) -> new HashSet<>()
              ).add(new FileEntry(previousLinePoint, totalReadBytes, hashCode));
            } else {
              id = null;
            }
            previousLinePoint = totalReadBytes;
          }
          sb.append(currentChunk);
          totalReadBytes += currentChunk.length();
        } else {
          sb.append(currentChunk);
          totalReadBytes += readBytes;
        }
      }
    }
  }

  private void readSecondAndWriteOutput(int bufferSize) throws IOException {

    ByteBuffer buf = ByteBuffer.allocateDirect(bufferSize);
    Charset cs = StandardCharsets.ISO_8859_1;
    try (FileChannel fc = FileChannel.open(source1Path);
        InputStream in = Files.newInputStream(source2Path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outputPath))) {
      String lineId = null;
      String lineValue = null;
      while ((lineId = reader.readLine()) != null && (lineValue = reader.readLine()) != null) {
        final int hashCode = lineId.hashCode();
        if (currentData.containsKey(hashCode)) {
          pw.println(lineId);
          final Set<FileEntry> fileEntries = currentData.get(hashCode);
          final String val1 = getValue(buf, cs, fc, fileEntries, lineId);
          pw.println(val1);
          pw.println(lineValue);
        }
      }
    }
  }

  private String getValue(ByteBuffer buf, Charset cs, FileChannel fc,
      Set<FileEntry> fileEntries, String lineId) throws IOException {
    long firstFilePointer = -1;
    for (FileEntry fileEntry : fileEntries) {
      final String currentId = readOneLine(fc, fileEntry.getIdPoint(), buf, cs);
      if (lineId.equals(currentId)) {
        firstFilePointer = fileEntry.getValuePoint();
        break;
      }
    }
    if (firstFilePointer == -1) {
      throw new RuntimeException("Id not found: " + lineId);
    }
    return readOneLine(fc, firstFilePointer, buf, cs);
  }

  private String readOneLine(
      FileChannel fc,
      Long point,
      ByteBuffer buf,
      Charset cs) throws IOException {

    String id = null;

    StringBuilder sb = new StringBuilder();

    fc.position(point);
    while (fc.read(buf) != -1) {
      buf.rewind();
      final CharBuffer decoded = cs.decode(buf);
      String currentChunk = decoded.toString();
      buf.clear();

      int indexOf = currentChunk.indexOf("\n");
      if (indexOf != -1) {
        String line = null;
        sb.append(currentChunk, 0, indexOf);
        return sb.toString();
      } else {
        sb.append(currentChunk);
      }
    }
    return sb.toString();
  }

  private static class FileEntry {

    private final long idPoint;
    private final long valuePoint;
    private final int idHash;

    public FileEntry(long idPoint, long valuePoint, int idHash) {

      this.idPoint = idPoint;
      this.valuePoint = valuePoint;
      this.idHash = idHash;
    }

    public long getIdPoint() {
      return idPoint;
    }

    public long getValuePoint() {
      return valuePoint;
    }

    public int getIdHash() {
      return idHash;
    }
  }
}
