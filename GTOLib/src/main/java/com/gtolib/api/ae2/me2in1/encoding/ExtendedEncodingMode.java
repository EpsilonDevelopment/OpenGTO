package com.gtolib.api.ae2.me2in1.encoding;

public enum ExtendedEncodingMode {
   CRAFTING(true),
   PROCESSING(false),
   SMITHING_TABLE(true),
   STONECUTTING(true),
   BATCH(false);

   public final boolean certernRecipe;

   ExtendedEncodingMode(boolean certernRecipe) {
      this.certernRecipe = certernRecipe;
   }

   // $VF: synthetic method
   private static ExtendedEncodingMode[] $values() {
      return new ExtendedEncodingMode[]{CRAFTING, PROCESSING, SMITHING_TABLE, STONECUTTING, BATCH};
   }
}
