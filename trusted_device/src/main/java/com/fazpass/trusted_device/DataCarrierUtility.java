package com.fazpass.trusted_device;

import android.util.ArrayMap;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class DataCarrierUtility {
   private static final String telkomsel = "TELKOMSEL";
   private static final String indosat = "INDOSAT";
   private static final String xl = "XL";
   private static final String three = "THREE";
   private static final String axis = "AXIS";
   //private static final String smartfren = "SMARTFREN";

   private static Map<String, List<String>> collection() {
      Map<String, List<String>> map = new ArrayMap<>();
      map.put(telkomsel,
              Arrays.asList("0852", "0853", "0811", "0812", "0813", "0821", "0822", "0851"));
      map.put(indosat,
              Arrays.asList("0855", "0856", "0857", "0858", "0814", "0815", "0816"));
      map.put(xl,
              Arrays.asList("0817", "0818", "0819", "0859", "0877", "0878"));
      map.put(three,
              Arrays.asList("0895", "0896", "0897", "0898", "0899"));
      map.put(axis,
              Arrays.asList("0813", "0832", "0833", "0838"));
      return map;
   }

   public static boolean check(@NotNull String phonePrefix, @NotNull String carrierName) {
      String alias = getCarrierAlias(
              carrierName.toUpperCase(Locale.ROOT)
                      .split(" ")[0]
                      .split("-")[0]
      );

      List<String> list = collection().get(alias);
      if (list==null) return false;
      return list.contains(phonePrefix);
   }

   private static String getCarrierAlias(String carrierName) {
      switch (carrierName) {
         case "AS":
         case "TSEL":
         case "BY.U":
         case "LOOP":
         case "SIMPATI":
         case "HALO":
            return telkomsel;
         case "IM3":
         case "MATRIX":
         case "MENTARI":
            return indosat;
         case "EXCELCOMINDO":
         case "EXCL":
            return xl;
         case "3": return three;
         default: return carrierName;
      }
   }
}
