package com.gtolib.api.ae2;

import java.util.UUID;
import net.minecraft.network.chat.Component;

public interface IPatterEncodingTermMenu {
   default void gtolib$modifyPatter(Integer value) {
   }

   default void gtolib$clearSecOutput() {
   }

   default void gtolib$addRecipe(String id) {
   }

   default void gtolib$addUUID(UUID uuid) {
   }

   default void gtolib$clickRecipeInfo() {
   }

   default Component gtolib$getRecipeInfoTooltip() {
      return Component.empty();
   }
}
