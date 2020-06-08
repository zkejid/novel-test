package com.zkejid.test.noveltest;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

public class Generator {

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
    final ValueSupplier valueSupplier = new ValueSupplier(seed);

    try (PrintWriter pw = new PrintWriter(fileName)) {
      int count = 0;
      while (count < limit) {
        // id
        pw.println(valueSupplier.get());
        // value
        pw.println(valueSupplier.get());
        count++;
      }
    }
  }

  public static class ValueSupplier implements Supplier<String> {

    private boolean id;
    private IdGenerator idGenerator;
    private ValueGenerator valueGenerator;

    public ValueSupplier(int seed) {
      idGenerator = new IdGenerator(seed);
      id = true;
      valueGenerator = new ValueGenerator();
    }

    @Override
    public String get() {
      if (id) {
        id = false;
        return idGenerator.getId();
      } else {
        id = true;
        return valueGenerator.getValue();
      }
    }
  }

  public static class IdGenerator {

    private int count;
    private Random random;

    public IdGenerator(int seed) {
      random = new Random(seed);
      count = 0;
    }

    public String getId() {
      final double nextDouble = random.nextDouble();
      if (nextDouble < 0.4) {
        count += 2;
      } else {
        count += 1;
      }
      return "@id"+count;
    }
  }

  public static class ValueGenerator {

    public String getValue() {
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
