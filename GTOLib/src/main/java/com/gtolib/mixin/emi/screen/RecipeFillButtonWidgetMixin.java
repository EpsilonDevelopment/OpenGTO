package com.gtolib.mixin.emi.screen;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import com.gtocore.client.Message.Client;
import com.gtocore.integration.emi.GTEmiEncodingHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.RecipeFillButtonWidget;
import dev.emi.emi.widget.RecipeButtonWidget;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(RecipeFillButtonWidget.class)
public class RecipeFillButtonWidgetMixin extends RecipeButtonWidget {
   @Unique
   private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator.comparing(GridInventoryEntry::isCraftable)
      .thenComparing(GridInventoryEntry::getStoredAmount);
   @Unique
   private boolean gto$aeScreen = false;

   public RecipeFillButtonWidgetMixin(int var1, int var2, int var3, int var4, EmiRecipe var5) {
      super(var1, var2, var3, var4, var5);
   }

   @Inject(method = "<init>", at = @At("RETURN"), remap = false)
   private void gto$detectAEScreen(int var1, int var2, EmiRecipe var3, CallbackInfo var4) {
      AbstractContainerScreen var5 = EmiApi.getHandledScreen();
      this.gto$aeScreen = var5 instanceof MEStorageScreen;
   }

   @Inject(method = "getTooltip", at = @At("RETURN"), remap = false, cancellable = true)
   public void getTooltip(int var1, int var2, CallbackInfoReturnable<List<ClientTooltipComponent>> var3) {
      List var4 = (List)var3.getReturnValue();
      if (this.gto$aeScreen) {
         var4.add(
            ClientTooltipComponent.create(Component.translatable("gtocore.ae.appeng.craft.temp_order").withStyle(ChatFormatting.GRAY).getVisualOrderText())
         );
      }

      var3.setReturnValue(var4);
   }

   @Inject(method = "mouseClicked", at = @At("HEAD"), remap = false, cancellable = true)
   private void gto$onMouseClicked(int var1, int var2, int var3, CallbackInfoReturnable<Boolean> var4) {
      if (this.gto$aeScreen && var3 == 2 && EmiApi.getHandledScreen() instanceof MEStorageScreen var5) {
         var4.setReturnValue(true);
         Reference2IntOpenHashMap var12 = EncodingHelper.getIngredientPriorities((MEStorageMenu)var5.getMenu(), ENTRY_COMPARATOR);
         List<List<GenericStack>> var7 = GTEmiEncodingHelper.ofInputs(this.recipe);
         KeyCounter var8 = new KeyCounter();

         for (List<GenericStack> var10 : var7) {
            if (!var10.isEmpty()) {
               GenericStack var11 = gto$findBestIngredient(var12, var10);
               var8.add(var11.what(), var11.amount());
            }
         }

         Client.orderItem(var8, 1L);
      }
   }

   @Unique
   private static GenericStack gto$findBestIngredient(Reference2IntOpenHashMap<AEKey> var0, List<GenericStack> var1) {
      return var1.stream()
         .map(var1x -> Pair.of(var1x, var0.getOrDefault(var1x.what(), Integer.MIN_VALUE)))
         .max(Comparator.comparingInt(Pair::getRight))
         .map(Pair::getLeft)
         .orElseThrow();
   }
}
