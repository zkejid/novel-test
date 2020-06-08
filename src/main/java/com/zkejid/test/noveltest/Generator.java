package com.zkejid.test.noveltest;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Генерирует входные файлы для тестов. Файлы имеют частичное пересечение по идентификаторам.
 */
public class Generator {

  /**
   * Один параметр: количество пар идентификатор - значение. По умолчанию 10 пар. Значение
   * одно для обоих файлов.
   */
  public static void main(String[] args) {
    int limit = 10;
    if (args.length > 0) {
      limit = Integer.parseInt(args[0]);
    }

    final Generator generator = new Generator();
    try {
      generator.generateFile("source1.txt", limit, 0);
      generator.generateFile("source2.txt", limit, 1);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public void generateFile(String fileName, long limit, int seed) throws FileNotFoundException {
    final Supplier<String> idSupplier = new IdGenerator(seed);
    final Supplier<String> valueSupplier = new ValueGenerator();

    try (PrintWriter pw = new PrintWriter(fileName)) {
      int count = 0;
      while (count < limit) {
        pw.println(idSupplier.get());
        pw.println(valueSupplier.get());
        count++;
      }
    }
  }

  public static class IdGenerator implements Supplier<String> {

    private int count;
    private Random random;

    public IdGenerator(int seed) {
      random = new Random(seed);
      count = 0;
    }

    @Override
    public String get() {
      final double nextDouble = random.nextDouble();
      if (nextDouble < 0.4) {
        count += 2;
      } else {
        count += 1;
      }
      return "@id"+count;
    }
  }

  public static class ValueGenerator implements Supplier<String> {

    @Override
    public String get() {
      return
          UUID.randomUUID().toString()
              + UUID.randomUUID().toString()
              + UUID.randomUUID().toString()
              + UUID.randomUUID().toString()
              + UUID.randomUUID().toString()
              + UUID.randomUUID().toString()
              + UUID.randomUUID().toString()
              + UUID.randomUUID().toString()
              + UUID.randomUUID().toString()
              + UUID.randomUUID().toString();
    }
  }
}
