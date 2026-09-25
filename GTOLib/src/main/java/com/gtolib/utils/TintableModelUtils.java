package com.gtolib.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gto.registrate.providers.DataGenContext;
import com.gto.registrate.providers.RegistrateItemModelProvider;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;

public final class TintableModelUtils {
   public static <T extends Item> void createTintableModel(DataGenContext<Item, T> ctx, RegistrateItemModelProvider prov, String... texturePaths) {
      createTintableModel(ctx, "item/generated", prov, texturePaths);
   }

   private static <T extends Item> void createTintableModel(
      DataGenContext<Item, T> ctx, String parentModel, RegistrateItemModelProvider prov, String... texturePaths
   ) {
      ItemModelBuilder builder = prov.getBuilder(ctx.getName()).parent(new UncheckedModelFile(parentModel));
      JsonArray tints = new JsonArray();
      JsonObject json = builder.toJson();

      for (int i = 0; i < texturePaths.length; i++) {
         builder.texture("layer" + i, prov.modLoc(texturePaths[i]));
         tints.add(i);
      }

      json.add("tintindexes", tints);
   }
}
