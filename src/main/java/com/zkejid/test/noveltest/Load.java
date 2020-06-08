package com.zkejid.test.noveltest;

/**
 * <h1>Load test result</h1>
 * <p>Memory footprint ~ 250 byte per id-value pair in a heap.</p>
 */
public class Load {

  public static final String PAIRS_COUNT = "2000000";

  public static void main(String[] args) {
    Cleaner.main(new String[0]);
    // ~ 10 Gb file
    System.out.println("Generate " + PAIRS_COUNT + " pairs id-value in each of two input files.");
    Generator.main(new String[] {PAIRS_COUNT});
    System.out.println("Generation done.");

    System.out.println("Process input files.");
    final long start = System.nanoTime();
    Main.main(new String[0]);
    final long end = System.nanoTime();
    double sum = end - start;
    System.out.println("Processing done.");
    System.out.println("Total processing time: " + (sum / 1_000_000_000L) + " seconds");
  }
}
