package com.gtolib.mixin.mc;

import com.gtocore.common.data.GTOLoots;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LootTable.class)
public class LootTableMixin {
   @Redirect(
      method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraftforge/common/ForgeHooks;modifyLoot(Lnet/minecraft/resources/ResourceLocation;Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
         remap = false
      )
   )
   public ObjectArrayList<ItemStack> modifyLoot(ResourceLocation var1, ObjectArrayList<ItemStack> var2, LootContext var3) {
      return GTOLoots.modifyLoot ? ForgeHooks.modifyLoot(var1, var2, var3) : var2;
   }
}
