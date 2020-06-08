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

/**
 * Класс логики приложения. Выполняет чтение файла в память, сравнение прочитанного со значениями
 * из второго файла и вывод пересечения записей в файл вывода.
 */
public class Merger {

  public static final int MEGABYTE = 1024 * 1024;
  private final Path source1Path;
  private final Path source2Path;
  private final Path outputPath;
  private final Instrumentation instrumentation;

  private final Map<Integer, Set<FileEntry>> currentData;

  public Merger(Path source1Path, Path source2Path, Path outputPath) {

    this.source1Path = source1Path;
    this.source2Path = source2Path;
    this.outputPath = outputPath;
    this.currentData = new ConcurrentHashMap<>();
    this.instrumentation = new Instrumentation();
  }

  /**
   * Основной метод алгоритма. Аргумент - размер буфера при работе с файлами.
   */
  public void merge(int bufferSize) {

    try {
      System.out.println("Read input file 1");
      readFirst(bufferSize);
      System.out.println("Reading finished");
      System.out.println("Match with file 2 and fill output file");
      readSecondFileAndWriteOutput(bufferSize);
      System.out.println("Matching done, output file created");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // TODO какой чарсет у файлов?
  // TODO как обрабатывать пробельные строки?
  // TODO какие могут быть переводы строки?
  private void readFirst(int bufferSize) throws IOException {
    instrumentation.startMetric();
    ByteBuffer buf = ByteBuffer.allocateDirect(bufferSize);
    Charset cs = StandardCharsets.ISO_8859_1;
    try (FileChannel fc = FileChannel.open(source1Path)) {
      // first line is id
      Boolean isId = true;

      long previousLinePoint = 0;
      long totalReadBytesCount = 0;
      int readBytesCount;
      StringBuilder headOfCurrentLine = new StringBuilder();

      while ((readBytesCount = fc.read(buf)) != -1) {
        String currentChunk = convertBytesToString(buf, cs);
        int indexOf = currentChunk.indexOf("\n");
        // if chunk of data contains line end (one or more)
        if (indexOf != -1) {
          String line;
          while (indexOf != -1) {
            // append tail to head
            headOfCurrentLine.append(currentChunk, 0, indexOf);
            // count substring + new line character
            totalReadBytesCount += indexOf + 1;
            // remove processed tail from chunk
            currentChunk = currentChunk.substring(indexOf + 1);
            indexOf = currentChunk.indexOf("\n");
            line = headOfCurrentLine.toString();
            headOfCurrentLine = new StringBuilder();

            if (isId) {
              isId = false;
              final int hashCode = line.hashCode();
              currentData.computeIfAbsent(
                  hashCode,
                  (k) -> new HashSet<>()
              ).add(new FileEntry(previousLinePoint, totalReadBytesCount));
            } else {
              isId = true;
            }
            previousLinePoint = totalReadBytesCount;
          }
          headOfCurrentLine.append(currentChunk);
          totalReadBytesCount += currentChunk.length();
        } else {
          headOfCurrentLine.append(currentChunk);
          totalReadBytesCount += readBytesCount;
        }
        instrumentation.readFile(totalReadBytesCount);
      }
    }
  }

  private String convertBytesToString(ByteBuffer buf, Charset cs) {
    buf.rewind();
    final CharBuffer decoded = cs.decode(buf);
    String currentChunk = decoded.toString();
    buf.clear();
    return currentChunk;
  }

  private void readSecondFileAndWriteOutput(int bufferSize) throws IOException {

    ByteBuffer buf = ByteBuffer.allocateDirect(bufferSize);
    Charset cs = StandardCharsets.ISO_8859_1;
    try (FileChannel fc = FileChannel.open(source1Path);
        InputStream in = Files.newInputStream(source2Path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outputPath))) {

      String lineId;
      String lineValue;
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
    long valueFilePointer = -1;
    // handle clash of hashes - search given id
    for (FileEntry fileEntry : fileEntries) {
      final String currentId = readOneLine(fc, fileEntry.getIdPoint(), buf, cs);
      if (lineId.equals(currentId)) {
        valueFilePointer = fileEntry.getValuePoint();
        break;
      }
    }
    if (valueFilePointer == -1) {
      throw new RuntimeException("Id not found: " + lineId);
    }
    return readOneLine(fc, valueFilePointer, buf, cs);
  }

  private String readOneLine(FileChannel fc, Long point, ByteBuffer buf,
      Charset cs) throws IOException {

    StringBuilder headOfLine = new StringBuilder();
    fc.position(point);
    while (fc.read(buf) != -1) {
      String currentChunk = convertBytesToString(buf, cs);
      int indexOf = currentChunk.indexOf("\n");
      if (indexOf != -1) {
        // append tail to head
        headOfLine.append(currentChunk, 0, indexOf);
        return headOfLine.toString();
      } else {
        headOfLine.append(currentChunk);
      }
    }
    return headOfLine.toString();
  }

  private static class FileEntry {

    private final long idPoint;
    private final long valuePoint;

    public FileEntry(long idPoint, long valuePoint) {

      this.idPoint = idPoint;
      this.valuePoint = valuePoint;
    }

    public long getIdPoint() {
      return idPoint;
    }

    public long getValuePoint() {
      return valuePoint;
    }
  }

  /**
   * Отображение прогресса при обработке файлов прямого доступа.
   */
  private static class Instrumentation {

    private long start;

    public void startMetric() {
      start = System.nanoTime();
    }
    public void readFile(long currentPosition) {
      if (currentPosition % (100 * MEGABYTE) == 0) {
        final long end = System.nanoTime();
        final double duration = end - start;
        final double speed = ((double) (100 * MEGABYTE)) / (duration / 1_000_000_000);
        System.out.println("100 Mb read. Speed (Mb/s): " + (speed / MEGABYTE));
        start = end;
      }
    }
  }
}
