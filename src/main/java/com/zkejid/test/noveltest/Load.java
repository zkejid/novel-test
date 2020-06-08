package com.zkejid.test.noveltest;

public class Load {

  public static void main(String[] args) {
    //Cleaner.main(new String[0]);
    // ~ 10 Gb file
    //Generator.main(new String[] {"20000000"});
    final long start = System.nanoTime();
    Main.main(new String[0]);
    final long end = System.nanoTime();
    long sum = end - start;
    System.out.println("Average time: " + (sum / 10) + " nanos or "
        + (sum / 10_000) + " millis or "
        + (sum / 10_000_000) + " millis or "
        + (sum / 10_000_000_000L) + " seconds");
  }
}
