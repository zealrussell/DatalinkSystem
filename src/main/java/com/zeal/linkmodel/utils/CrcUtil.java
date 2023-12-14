package com.zeal.linkmodel.utils;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @since 2023/12/3 9:37
 */
public class CrcUtil {
   public static String encode(String message) {
      int crc = 0x000;
      int polynomial = 0x1021;
      byte[] bytes = message.getBytes();
      for (byte b : bytes) {
         for (int i = 0; i < 8; i++) {
            boolean bit = ((b >> (7 - i) & 1) == 1);
            boolean c15 = ((crc >> 15 & 1) == 1);
            crc <<= 1;
            if (c15 ^ bit) crc ^= polynomial;
         }
      }
      crc &= 0xffff;
      return message + Integer.toHexString(crc).toUpperCase();
   }

   public static String decode(String message) {
      if (check(message))
         return message.substring(0, message.length() - 4);
      return "";
   }

   private static boolean check(String str) {
        String crc = str.substring(str.length() - 4);
        String message = str.substring(0, str.length() - 4);
        return encode(message).equals(str);
   }

}
