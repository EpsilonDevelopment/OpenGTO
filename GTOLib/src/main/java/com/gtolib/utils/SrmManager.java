package com.gtolib.utils;

import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtolib.forge.ForgeCommonEvent;
import java.util.Arrays;
import java.util.Set;

public class SrmManager {
   static Set<String> whitelist = new OpenCacheHashSet<>(
      Arrays.asList(
         "help",
         "list",
         "me",
         "tell",
         "teammsg",
         "trigger",
         "kick",
         "pardon",
         "pardon-ip",
         "setidletimeout",
         "whitelist",
         "jfr",
         "perf",
         "publish",
         "save-all",
         "save-off",
         "save-on",
         "stop",
         "restart",
         "seed",
         "reload",
         "save",
         "openpac",
         "openpac-claims",
         "spark",
         "backup",
         "ftbteams"
      )
   );

   public static boolean isSrmMode() {
      return ForgeCommonEvent.isSrmMode;
   }

   public static boolean isWhiteList(String cmdHead) {
      return whitelist.contains(cmdHead);
   }
}
