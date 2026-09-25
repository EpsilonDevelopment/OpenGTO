package com.gtolib.mixin.apotheosis;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gtolib.api.item.tool.GTOToolType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import java.util.function.Predicate;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootCategory.class)
public abstract class LootCategoryMixin {
   @Unique
   private static final ImmutableList<GTToolType> gtolib$PICKAXE_TOOL_TYPES = ImmutableList.of(
      GTToolType.PICKAXE,
      GTToolType.DRILL_LV,
      GTToolType.DRILL_MV,
      GTToolType.DRILL_HV,
      GTToolType.DRILL_EV,
      GTToolType.DRILL_IV,
      GTToolType.WRENCH,
      GTToolType.WRENCH_HV,
      GTToolType.WRENCH_LV,
      GTToolType.WRENCH_IV,
      GTToolType.AXE,
      GTToolType.CHAINSAW_LV,
      GTToolType.MINING_HAMMER,
      GTToolType.HARD_HAMMER,
      GTOToolType.VAJRA_EV,
      GTOToolType.VAJRA_HV,
      GTOToolType.VAJRA_IV
   );
   @Unique
   private static final ImmutableList<GTToolType> gtolib$SHOVEL_TYPES = ImmutableList.of(GTToolType.SHOVEL, GTToolType.SPADE);

   @Shadow(remap = false)
   public static LootCategory register(@Nullable LootCategory var0, String var1, Predicate<ItemStack> var2, EquipmentSlot[] var3) {
      return null;
   }

   @Shadow(remap = false)
   private static EquipmentSlot[] arr(EquipmentSlot... var0) {
      return null;
   }

   @Inject(
      method = "register(Ljava/lang/String;Ljava/util/function/Predicate;[Lnet/minecraft/world/entity/EquipmentSlot;)Ldev/shadowsoffire/apotheosis/adventure/loot/LootCategory;",
      at = @At("HEAD"),
      remap = false,
      cancellable = true
   )
   private static void register(String var0, Predicate<ItemStack> var1, EquipmentSlot[] var2, CallbackInfoReturnable<LootCategory> var3) {
      switch (var0) {
         case "heavy_weapon":
            var3.setReturnValue(register(null, var0, var0x -> false, arr(EquipmentSlot.MAINHAND)));
            break;
         case "pickaxe":
            var3.setReturnValue(
               register(
                  null,
                  var0,
                  var0x -> var0x.canPerformAction(ToolActions.PICKAXE_DIG)
                     || var0x.getItem() instanceof GTToolItem var1x && gtolib$PICKAXE_TOOL_TYPES.contains(var1x.getToolType()),
                  arr(EquipmentSlot.MAINHAND)
               )
            );
            break;
         case "shovel":
            var3.setReturnValue(
               register(
                  null,
                  var0,
                  var0x -> var0x.canPerformAction(ToolActions.SHOVEL_DIG)
                     || var0x.getItem() instanceof GTToolItem var1x && gtolib$SHOVEL_TYPES.contains(var1x.getToolType()),
                  arr(EquipmentSlot.MAINHAND)
               )
            );
      }
   }
}
