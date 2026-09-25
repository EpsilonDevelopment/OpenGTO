package com.gtolib.mixin.emi.screen;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.ButtonToolTips;
import appeng.integration.modules.emi.EmiStackHelper;
import com.gtocore.integration.emi.research.EmiResearchHelper;
import com.gtocore.integration.emi.research.ResearchTagEmiStack;
import com.gtocore.integration.emi.research.TechNodeEmiStack;
import com.gtolib.Client;
import com.gtolib.api.player.IEnhancedPlayer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlotWidget.class)
public abstract class SlotWidgetMixin {
   @Shadow(remap = false)
   public abstract EmiIngredient getStack();

   @WrapMethod(method = "getTooltip", remap = false)
   private List<ClientTooltipComponent> gtolib$wrapGetStack(int var1, int var2, Operation<List<ClientTooltipComponent>> var3) {
      Client.disableAdditionalEmiTooltip = true;
      List var4 = (List)var3.call(var1, var2);
      Client.disableAdditionalEmiTooltip = false;
      return var4;
   }

   @Inject(method = "addSlotTooltip", at = @At("RETURN"), remap = false)
   private void gtolib$onAddSlotTooltip(List<ClientTooltipComponent> var1, CallbackInfo var2) {
      List var3 = this.getStack().getEmiStacks();
      LocalPlayer var4 = Minecraft.getInstance().player;
      boolean var5 = IEnhancedPlayer.isClientAEReachable(var4);
      if (!var3.isEmpty() && var4 != null) {
         if (var3.size() == 1) {
            EmiStack var6 = (EmiStack)var3.getFirst();
            if (var6 instanceof ResearchTagEmiStack var16) {
               var1.add(ClientTooltipComponent.create(EmiResearchHelper.getResearchTagTeamTotal(var16.tag).getVisualOrderText()));
               return;
            }

            if (var6 instanceof TechNodeEmiStack var15) {
               var1.add(ClientTooltipComponent.create(EmiResearchHelper.getTechNodeState(var15.data).getVisualOrderText()));
               return;
            }
         }

         List<GenericStack> var14 = EmiStackHelper.toGenericStack(this.getStack());
         if (Screen.hasShiftDown()) {
            var14.stream()
               .map(GenericStack::what)
               .map(var1x -> new GenericStack(var1x, IEnhancedPlayer.getClientAEAmount(var4, var1x)))
               .sorted(Comparator.comparingLong(var0 -> -var0.amount()))
               .map(
                  var1x -> gto$getTooltipComponent(var1x.what().getDisplayName(), IEnhancedPlayer.getClientAEStatusText(var4, var1x.what(), AmountFormat.FULL))
               )
               .filter(var1x -> var5)
               .forEach(var1::add);
         } else {
            boolean var7 = var3.size() > 1;
            MutableComponent var18 = ((EmiStack)var3.getFirst()).getName().copy();
            if (var7) {
               var18.append("...");
            }

            AEKeyType var9 = var14.stream().findFirst().map(var0 -> var0.what().getType()).orElse(AEKeyType.items());
            long var10 = var14.stream().map(GenericStack::what).distinct().mapToLong(var1x -> IEnhancedPlayer.getClientAEAmount(var4, var1x)).sum();
            boolean var12 = var14.stream()
               .map(GenericStack::what)
               .distinct()
               .anyMatch(IEnhancedPlayer.of(var4).getPlayerData().getMeStorageInfoManager().getClientKnownCraftables()::contains);
            MutableComponent var13 = ButtonToolTips.Amount.text(var9.formatAmount(var10, AmountFormat.FULL));
            if (var12) {
               var13.append(" [").append(ButtonToolTips.Craftable.text()).append("]");
            }

            if (var5) {
               var1.add(gto$getTooltipComponent(var18, var13));
            }
         }
      }
   }

   @Unique
   private static ClientTooltipComponent gto$getTooltipComponent(Component var0, Component var1) {
      return ClientTooltipComponent.create(
         Component.literal("[")
            .withStyle(ChatFormatting.GRAY)
            .append(var0)
            .withStyle(ChatFormatting.GRAY)
            .append("]")
            .withStyle(ChatFormatting.GRAY)
            .append(var1)
            .withStyle(ChatFormatting.DARK_GRAY)
            .getVisualOrderText()
      );
   }
}
