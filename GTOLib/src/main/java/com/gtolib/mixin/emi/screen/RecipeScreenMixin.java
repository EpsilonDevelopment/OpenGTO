package com.gtolib.mixin.emi.screen;

import com.gtocore.integration.emi.multipage.CustomModularEmiRecipe;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.RecipeTab;
import dev.emi.emi.screen.WidgetGroup;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeScreen.class)
public abstract class RecipeScreenMixin extends Screen {
   @Shadow(remap = false)
   int x;
   @Shadow(remap = false)
   int backgroundHeight;
   @Shadow(remap = false)
   int backgroundWidth;
   @Shadow(remap = false)
   int y;
   @Shadow(remap = false)
   private List<RecipeTab> tabs;
   @Shadow(remap = false)
   private int tab;
   @Shadow(remap = false)
   private List<WidgetGroup> currentPage;
   @Unique
   private ScreenWrapper gtocore$oldQuest = null;

   protected RecipeScreenMixin(Component var1) {
      super(var1);
   }

   @Shadow(remap = false)
   public abstract int getResolveOffset();

   @ModifyArg(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V",
         ordinal = 4,
         remap = false
      ),
      index = 2
   )
   private int modifyx(int var1) {
      return var1 + 18 - 18 * this.gtolib$getList(this.gtolib$getWorkstationAmount());
   }

   @ModifyArg(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V",
         ordinal = 4,
         remap = false
      ),
      index = 4
   )
   private int modifyw(int var1) {
      return var1 - 18 + 18 * this.gtolib$getList(this.gtolib$getWorkstationAmount());
   }

   @ModifyArg(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V",
         ordinal = 4,
         remap = false
      ),
      index = 5
   )
   private int modifyh(int var1) {
      int var2 = EmiApi.getRecipeManager().getWorkstations(this.tabs.get(this.tab).category).size();
      return 10 + Math.min(var2, this.gtolib$maxWorkstations()) * 18 + this.getResolveOffset();
   }

   @Inject(method = "<init>", at = @At("TAIL"), remap = false)
   public void hookInit(AbstractContainerScreen<?> var1, Map<EmiRecipeCategory, List<EmiRecipe>> var2, CallbackInfo var3) {
      if (Minecraft.getInstance().screen instanceof ScreenWrapper var4) {
         this.gtocore$oldQuest = var4;
      }
   }

   @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
   public void onClose(CallbackInfo var1) {
      if (this.gtocore$oldQuest != null) {
         Minecraft.getInstance().setScreen(this.gtocore$oldQuest);
         var1.cancel();
      }
   }

   @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
   private void initKeyPressed(int var1, int var2, int var3, CallbackInfoReturnable<Boolean> var4) {
      for (WidgetGroup var6 : this.currentPage) {
         for (Widget var8 : var6.widgets) {
            if (var8 instanceof CustomModularEmiRecipe var9 && var9.keyPressed(var1, var2, var3)) {
               var4.setReturnValue(true);
            }
         }
      }
   }

   @Overwrite(remap = false)
   public Bounds getWorkstationBounds(int var1) {
      Bounds var2 = Bounds.EMPTY;
      int var3 = 0;
      if (var1 == -1) {
         var1 = 0;
         var3 = -this.getResolveOffset();
      }

      if (EmiConfig.workstationLocation == SidebarSide.LEFT) {
         var2 = new Bounds(
            this.x - this.gtolib$getList(var1) * 18, this.y + 9 + this.getResolveOffset() + var1 % this.gtolib$maxWorkstations() * 18 + var3, 18, 18
         );
      } else if (EmiConfig.workstationLocation == SidebarSide.RIGHT) {
         var2 = new Bounds(
            this.x + this.gtolib$getList(var1) * this.backgroundWidth,
            this.y + 9 + this.getResolveOffset() + var1 % this.gtolib$maxWorkstations() * 18 + var3,
            18,
            18
         );
      } else if (EmiConfig.workstationLocation == SidebarSide.BOTTOM) {
         var2 = new Bounds(this.x + 5 + this.getResolveOffset() + var1 * 18 + var3, this.y + this.backgroundHeight - 23, 18, 18);
      }

      return var2;
   }

   @Overwrite(remap = false)
   public int getMaxWorkstations() {
      return Integer.MAX_VALUE;
   }

   @Unique
   private int gtolib$getWorkstationAmount() {
      int var1 = EmiApi.getRecipeManager().getWorkstations(this.tabs.get(this.tab).category).size();
      return var1 < this.gtolib$maxWorkstations() ? var1 : var1 - 1;
   }

   @Unique
   private int gtolib$getList(int var1) {
      return (int)Math.floor((double)var1 / this.gtolib$maxWorkstations()) + 1;
   }

   @Unique
   private int gtolib$maxWorkstations() {
      return switch (EmiConfig.workstationLocation) {
         case LEFT, RIGHT -> (this.backgroundHeight - this.getResolveOffset() - 18) / 18;
         default -> 0;
         case BOTTOM -> (this.backgroundWidth - this.getResolveOffset() - 18) / 18;
      };
   }
}
