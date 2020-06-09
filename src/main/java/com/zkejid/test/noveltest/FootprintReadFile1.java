package com.zkejid.test.noveltest;

import java.util.HashMap;
import java.util.Map;

/**
 * Проверка объёма занимаемой памяти структурой хранения данных:
 * <ul>
 *   <li>Map + HashSet = 740 Mb на 2kk записей
 *   <li>Map + ArrayList = 500 Mb на 2kk записей
 *   <li>Map + ArrayList размером 1 элемент = 250 Mb на 2kk записей
 *   <li>Map строки на лонг = 200 Mb на 2kk записей для ключа вида @idXXXX
 * </ul>
 */
public class FootprintReadFile1 {

  public static void main(String[] args) throws InterruptedException {

    Thread.sleep(10_000);
    final Map<String, Long> currentData = new HashMap<>();
    for (int i = 0; i < 2_000_000; i++) {
      currentData.put("@id" + i, (long) i);
      if (i % 100_000 == 0) {
        Thread.sleep(500);
      }
    }
    Thread.sleep(100_000);
  }
}
