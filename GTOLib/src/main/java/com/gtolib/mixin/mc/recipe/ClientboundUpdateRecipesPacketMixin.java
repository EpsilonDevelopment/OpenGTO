package com.gtolib.mixin.mc.recipe;

import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gtocore.common.data.GTORecipes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf.Reader;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientboundUpdateRecipesPacket.class)
public class ClientboundUpdateRecipesPacketMixin {
   @Redirect(
      method = "<init>(Ljava/util/Collection;)V",
      at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList(Ljava/lang/Iterable;)Ljava/util/ArrayList;", remap = false)
   )
   private ArrayList<Recipe<?>> newArrayList(Iterable<Recipe<?>> var1) {
      ArrayList var2 = new ArrayList(((Collection)var1).size() - GTRecipes.RECIPE_MAP.size());
      var1.forEach(var1x -> {
         if (!GTRecipes.RECIPE_MAP.containsKey(var1x.getId())) {
            var2.add(var1x);
         }
      });
      return var2;
   }

   @Redirect(
      method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readList(Lnet/minecraft/network/FriendlyByteBuf$Reader;)Ljava/util/List;")
   )
   private List readList(FriendlyByteBuf var1, Reader var2) {
      if (GTORecipes.cache) {
         var1.clear();
         return Collections.emptyList();
      } else {
         return var1.readList(var2);
      }
   }
}
