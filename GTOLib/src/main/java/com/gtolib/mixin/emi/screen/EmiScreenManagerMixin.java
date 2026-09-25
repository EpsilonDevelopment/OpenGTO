package com.gtolib.mixin.emi.screen;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.StackWithBounds;
import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.integration.modules.emi.EmiStackHelper;
import com.glodblock.github.extendedae.network.EPPNetworkHandler;
import com.glodblock.github.glodium.network.packet.CGenericPacket;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gtocore.config.GTOConfig;
import com.gtolib.GTOCore;
import com.gtolib.api.emi.stack.EmiSearchTextStack;
import com.gtolib.emi.EMIManager;
import com.gtolib.utils.ClientUtil;
import com.gtolib.utils.ItemUtils;
import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.emi.emi.screen.EmiScreenBase;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.EmiScreenManager.ScreenSpace;
import dev.emi.emi.screen.EmiScreenManager.SidebarEmiStackInteraction;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmiScreenManager.class)
public abstract class EmiScreenManagerMixin {
   @Shadow(remap = false)
   private static Minecraft client;
   @Shadow(remap = false)
   public static EmiIngredient pressedStack;
   @Shadow(remap = false)
   public static int lastMouseX;
   @Shadow(remap = false)
   public static int lastMouseY;

   @Overwrite(remap = false)
   public static boolean isDisabled() {
      return !EmiReloadManager.isLoaded() || !EmiConfig.enabled || ClientUtil.getPlayer().containerMenu instanceof PlanetsMenu;
   }

   @Shadow(remap = false)
   @Nullable
   public static ScreenSpace getHoveredSpace(int var0, int var1) {
      throw new AssertionError();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Ldev/emi/emi/runtime/EmiReloadManager;isLoaded()Z"), remap = false)
   private static void render(EmiDrawContext var0, int var1, int var2, float var3, CallbackInfo var4, @Local(name = "screenHeight") int var5) {
      if (EMIManager.searchBaking) {
         int var6 = EmiScreenManager.getDebugTextX();
         var0.drawTextWithShadow(EmiPort.literal("Baking search"), var6, var5 - 26);
      }
   }

   @Overwrite(remap = false)
   private static boolean give(EmiStack var0, int var1, int var2) {
      ItemStack var3;
      if (var0 instanceof FluidEmiStack) {
         var3 = GTItems.FLUID_CELL.asStack();
         CompoundTag var4 = var3.getOrCreateTag();
         CompoundTag var5 = new CompoundTag();
         var5.putString("FluidName", var0.getId().toString());
         var5.putInt("Amount", 1000);
         var4.put("Fluid", var5);
         var3.setTag(var4);
      } else {
         if (var0.getItemStack().isEmpty()) {
            return false;
         }

         var3 = var0.getItemStack().copy();
      }

      var3.setCount(var1);
      if (var2 == 1 && client.player.getAbilities().instabuild && client.screen instanceof CreativeModeInventoryScreen) {
         client.player.containerMenu.setCarried(var3);
         return true;
      }

      if (EmiClient.onServer) {
         EmiNetwork.sendToServer(new CreateItemC2SPacket(var2, var3));
         return true;
      }

      if (!var3.isEmpty()) {
         ResourceLocation var6 = ItemUtils.getIdLocation(var3.getItem());
         String var7 = "give @s " + var6;
         if (var3.hasTag()) {
            var7 = var7 + var3.getTag().toString();
         }

         String var8 = var7 + " " + var1;
         if (var8.length() < 256) {
            client.player.connection.sendCommand(var8);
            return true;
         }
      }

      return false;
   }

   @Inject(remap = false, method = "mouseClicked", at = @At("HEAD"), cancellable = true)
   private static void onMouseClicked(double var0, double var2, int var4, CallbackInfoReturnable<Boolean> var5) {
      if (Screen.hasShiftDown() && var4 == 0 && client.screen instanceof CraftingCPUScreen var7) {
         StackWithBounds var8 = var7.getStackUnderMouse(var0, var2);
         if (var8 == null) {
            return;
         }

         var5.setReturnValue(false);
         EPPNetworkHandler.INSTANCE.sendToServer(new CGenericPacket("requestPendingBlocks", GenericStack.wrapInItemStack(var8.stack())));
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }
   }

   @Redirect(
      remap = false,
      method = "mouseClicked",
      at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/stack/EmiStackInteraction;getStack()Ldev/emi/emi/api/stack/EmiIngredient;", ordinal = 1)
   )
   private static EmiIngredient onMouseClicked1(EmiStackInteraction var0) {
      return var0.getStack().getEmiStacks().stream().anyMatch(var0x -> var0x instanceof EmiSearchTextStack) ? EmiStack.EMPTY : var0.getStack();
   }

   @Inject(remap = false, method = "mouseReleased", at = @At("HEAD"))
   private static void onMouseRleased(double var0, double var2, int var4, CallbackInfoReturnable<Boolean> var5) {
      Screen var6 = client.screen;
      if (var6 != null) {
         if (!EmiApi.isCheatMode()) {
            if (client.options.keyPickItem.matchesMouse(var4)) {
               AbstractContainerMenu var7 = switch (var6) {
                  case AbstractContainerScreen var10 -> var10.getMenu();
                  case RecipeScreen var11 -> var11.old.getMenu();
                  default -> null;
               };
               if (var7 == null) {
                  return;
               }

               GenericStack var12 = EmiStackHelper.toGenericStack(pressedStack.getEmiStacks().stream().findFirst().orElse(EmiStack.EMPTY));
               if (var12 == null) {
                  return;
               }

               com.gtolib.network.EmiNetwork.AUTO_CRAFT.send(var2x -> {
                  var2x.writeVarInt(var7.containerId);
                  GenericStack.writeBuffer(var12, var2x);
               });
            }
         }
      }
   }

   @Redirect(method = "stackInteraction", at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/EmiApi;isCheatMode()Z", remap = false), remap = false)
   private static boolean nonCheatCallback(
      @Local(argsOnly = true) EmiStackInteraction var0, @Local(argsOnly = true) Function<EmiBind, Boolean> var1, @Local(name = "ingredient") EmiIngredient var2
   ) {
      if (gto$shouldAct()) {
         GTOCore.LOGGER.debug("Non-cheat EMI interaction triggered");
         if (var2.getEmiStacks().size() == 1 && var0 instanceof SidebarEmiStackInteraction) {
            if ((Boolean)var1.apply(EmiConfig.cheatOneToInventory)) {
               gto$packet(var2.getEmiStacks().getFirst(), 1, 0);
            } else if ((Boolean)var1.apply(EmiConfig.cheatStackToInventory)) {
               gto$packet(var2.getEmiStacks().getFirst(), var2.getEmiStacks().getFirst().getItemStack().getMaxStackSize(), 0);
            } else if ((Boolean)var1.apply(EmiConfig.cheatOneToCursor)) {
               gto$packet(var2.getEmiStacks().getFirst(), 1, 1);
            } else if ((Boolean)var1.apply(EmiConfig.cheatStackToCursor)) {
               gto$packet(var2.getEmiStacks().getFirst(), var2.getEmiStacks().getFirst().getItemStack().getMaxStackSize(), 1);
            }
         }
      }

      return EmiApi.isCheatMode();
   }

   @Unique
   private static void gto$packet(EmiStack var0, int var1, int var2) {
      if (client.screen instanceof AbstractContainerScreen var3) {
         com.gtolib.network.EmiNetwork.INTERACTION.send(var4 -> {
            var4.writeVarInt(var3.getMenu().containerId);
            GenericStack.writeBuffer(EmiStackHelper.toGenericStack(var0.copy().setAmount(var1)), var4);
            var4.writeVarInt(var2);
         });
      }
   }

   @Inject(remap = false, method = "keyPressed", at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/EmiApi;isCheatMode()Z"), cancellable = true)
   private static void onNonCheatInteractionKey(int var0, int var1, int var2, CallbackInfoReturnable<Boolean> var3) {
      if (gto$shouldAct() && EmiConfig.deleteCursorStack.matchesKey(var0, var1) && gto$insertCursor(lastMouseX, lastMouseY)) {
         var3.setReturnValue(true);
      }
   }

   @Inject(remap = false, method = "mouseReleased", at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/EmiApi;isCheatMode()Z"), cancellable = true)
   private static void onNonCheatInteractionMouse(double var0, double var2, int var4, CallbackInfoReturnable<Boolean> var5) {
      if (gto$shouldAct() && EmiConfig.deleteCursorStack.matchesMouse(var4) && gto$insertCursor(lastMouseX, lastMouseY)) {
         var5.setReturnValue(false);
      }
   }

   @Inject(remap = false, method = "renderCurrentTooltip", at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/EmiApi;isCheatMode()Z"), cancellable = true)
   private static void onNonCheatInteractionTooltip(
      EmiDrawContext var0,
      int var1,
      int var2,
      float var3,
      EmiScreenBase var4,
      CallbackInfo var5,
      @Local(name = "cursor") ItemStack var6,
      @Local(name = "space") ScreenSpace var7
   ) {
      if (gto$shouldAct() && !var6.isEmpty() && var7 != null && var7.getType() == SidebarType.INDEX && EmiConfig.deleteCursorStack.isBound()) {
         var5.cancel();
         List var8 = List.of(
            ClientTooltipComponent.create(EmiPort.ordered(EmiPort.translatable("gtocore.emi.insert_item_into_ae"))),
            ClientTooltipComponent.create(EmiPort.ordered(EmiConfig.deleteCursorStack.getBindText()))
         );
         if (var7.rtl) {
            EmiRenderHelper.drawLeftTooltip(var4.screen(), var0, var8, var1, var2);
         } else {
            EmiRenderHelper.drawTooltip(var4.screen(), var0, var8, var1, var2);
         }
      }
   }

   @Unique
   private static boolean gto$insertCursor(int var0, int var1) {
      if (client.screen instanceof AbstractContainerScreen var2) {
         ItemStack var5 = var2.getMenu().getCarried();
         ScreenSpace var4 = getHoveredSpace(var0, var1);
         if (!var5.isEmpty() && var4 != null && var4.getType() == SidebarType.INDEX) {
            var2.getMenu().setCarried(ItemStack.EMPTY);
            com.gtolib.network.EmiNetwork.INTERACTION.send(var2x -> {
               var2x.writeVarInt(var2.getMenu().containerId);
               GenericStack.writeBuffer(GenericStack.fromItemStack(var5), var2x);
               var2x.writeVarInt(2);
            });
            return true;
         }
      }

      return false;
   }

   @Unique
   private static boolean gto$shouldAct() {
      return !EmiApi.isCheatMode() && GTOConfig.INSTANCE.gamePlay.nonCheatEmiInteraction;
   }
}
