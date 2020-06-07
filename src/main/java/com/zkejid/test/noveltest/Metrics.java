package com.zkejid.test.noveltest;

public class Metrics {

  public static void main(String[] args) {
    Cleaner.main(new String[0]);
    Generator.main(new String[] {"10000"});
    // Warm up
    doStuff();

    long sum = 0;
    for (int i = 0; i < 10; i ++) {
      final long current = doStuff();
      sum += current;
      System.out.println("corrent score " + current);
    }

    Cleaner.main(args);
    System.out.println("Average time: " + (sum / 10) + " nanos or "
        + (sum / 10_000) + " millis or "
        + (sum / 10_000_000) + " millis or "
        + (sum / 10_000_000_000L) + " seconds");
  }

  private static long doStuff() {
    Cleaner.main(new String[] {"true"});
    final long start = System.nanoTime();
    Main.main(new String[0]);
    final long end = System.nanoTime();
    return end - start;
  }
}
