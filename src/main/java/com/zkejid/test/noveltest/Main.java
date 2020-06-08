package com.zkejid.test.noveltest;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Запуск приложения.
 */
public class Main {

  /**
   * Два параметра: базовый каталог с входными и выходными файлами,
   * размер буфера при работе с файлами. По умолчанию базовый каталог - текущий,
   * а размер буфера 256 байт.
   */
  public static void main(String[] args) {
    Path base = Paths.get(".");
    int bufferSize = 256;
    if (args.length > 0) {
      base = Paths.get(args[0]);
      bufferSize = Integer.parseInt(args[1]);
    }

    final Path source1Path = base.resolve("source1.txt");
    final Path source2Path = base.resolve("source2.txt");
    final Path outputPath = base.resolve("output.txt");

    Merger m = new Merger(source1Path, source2Path, outputPath);
    m.merge(bufferSize);
  }
}
