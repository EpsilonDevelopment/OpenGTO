package com.gtolib.mixin.emi.stack;

import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;
import com.gregtechceu.gtceu.api.item.DrumMachineItem;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gtocore.config.GTOConfig;
import com.gtocore.integration.emi.research.EmiResearchHelper;
import com.gtocore.integration.emi.research.ResearchTagEmiStack;
import com.gtocore.integration.emi.research.TechNodeEmiStack;
import com.gtolib.Client;
import com.gtolib.api.player.IEnhancedPlayer;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmiStack.class)
public final class EmiStackMixin {
   @Unique
   private static final Set<Item> gtolib$CELL = Set.of(
      GTItems.FLUID_CELL.asItem(),
      GTItems.FLUID_CELL_UNIVERSAL.asItem(),
      GTItems.FLUID_CELL_LARGE_STEEL.asItem(),
      GTItems.FLUID_CELL_LARGE_ALUMINIUM.asItem(),
      GTItems.FLUID_CELL_LARGE_STAINLESS_STEEL.asItem(),
      GTItems.FLUID_CELL_LARGE_TITANIUM.asItem(),
      GTItems.FLUID_CELL_GLASS_VIAL.asItem()
   );

   @Shadow(remap = false)
   public static EmiStack of(Fluid var0, long var1) {
      return null;
   }

   @Inject(method = "of(Lnet/minecraft/world/item/ItemStack;)Ldev/emi/emi/api/stack/EmiStack;", at = @At("HEAD"), cancellable = true, remap = false)
   private static void onItemStackOf(ItemStack var0, CallbackInfoReturnable<EmiStack> var1) {
      EmiStack var2 = gtolib$extracted(var0);
      if (var2 != null) {
         var1.setReturnValue(var2);
      }
   }

   @Inject(method = "of(Lnet/minecraft/world/item/ItemStack;J)Ldev/emi/emi/api/stack/EmiStack;", at = @At("HEAD"), cancellable = true, remap = false)
   private static void onItemStackOfWithAmount(ItemStack var0, long var1, CallbackInfoReturnable<EmiStack> var3) {
      if (var1 == 1L) {
         EmiStack var4 = gtolib$extracted(var0);
         if (var4 != null) {
            var3.setReturnValue(var4);
         }
      }
   }

   @Unique
   private static EmiStack gtolib$extracted(ItemStack var0) {
      if (var0.hasTag()) {
         Item var1 = var0.getItem();
         if (var1 instanceof DrumMachineItem || gtolib$CELL.contains(var1)) {
            AtomicReference var2 = new AtomicReference();
            var0.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(var1x -> {
               FluidStack var2x = var1x.getFluidInTank(0);
               if (!var2x.isEmpty()) {
                  var2.set(of(var2x.getFluid(), var2x.getAmount()));
               }
            });
            return (EmiStack)var2.get();
         }
      }

      return null;
   }

   @Inject(method = "getTooltip", at = @At("RETURN"), remap = false, require = 0)
   private void gtceu$addTooltip(CallbackInfoReturnable<List<ClientTooltipComponent>> var1) {
      LocalPlayer var2 = Minecraft.getInstance().player;
      if (GTOConfig.INSTANCE.gamePlay.showAEAmountTooltipEverywhereEmi && var2 != null && !Client.disableAdditionalEmiTooltip) {
         if (((Object)this) instanceof ResearchTagEmiStack var6) {
            ((List)var1.getReturnValue()).add(ClientTooltipComponent.create(EmiResearchHelper.getResearchTagTeamTotal(var6.tag).getVisualOrderText()));
         } else if (((Object)this) instanceof TechNodeEmiStack var5) {
            ((List)var1.getReturnValue()).add(ClientTooltipComponent.create(EmiResearchHelper.getTechNodeState(var5.data).getVisualOrderText()));
         } else {
            GenericStack var3 = EmiStackHelper.toGenericStack((EmiStack)(Object)this);
            if (var3 != null && var3.what() != null) {
               IEnhancedPlayer.fetchClientAEData(var2, var3.what());
               if (IEnhancedPlayer.isClientAEReachable(var2)) {
                  ((List)var1.getReturnValue())
                     .add(
                        ClientTooltipComponent.create(
                           IEnhancedPlayer.getClientAEStatusText(var2, var3.what(), AmountFormat.SLOT).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText()
                        )
                     );
               }
            }
         }
      }
   }
}
