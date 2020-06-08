package com.zkejid.test.noveltest;

/**
 * Расчёт примерной усреднённой производительности.
 */
public class Metrics {

  public static final int RETRY_COUNT = 10;

  public static void main(String[] args) {
    Cleaner.main(new String[0]);
    Generator.main(new String[] {"10000"});
    // Warm up
    doStuff();

    double sum = 0;
    for (int i = 0; i < RETRY_COUNT; i ++) {
      final long current = doStuff();
      sum += current;
      System.out.println("current score (ns): " + current);
    }

    Cleaner.main(args);
    System.out.println("Average processing time: "
        + (sum / (RETRY_COUNT * 1_000_000_000L)) + " seconds");
  }

  private static long doStuff() {
    Cleaner.main(new String[] {"true"});
    final long start = System.nanoTime();
    Main.main(new String[0]);
    final long end = System.nanoTime();
    return end - start;
  }
}
