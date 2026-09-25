package com.gtolib.api.ae2.me2in1;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import appeng.core.AELog;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.emi.AbstractRecipeHandler;
import appeng.integration.modules.emi.EmiStackHelper;
import appeng.integration.modules.emi.AbstractRecipeHandler.Result;
import appeng.integration.modules.emi.AbstractRecipeHandler.Result.EncodeWithCraftables;
import appeng.integration.modules.jeirei.TransferHelper;
import appeng.menu.AEBaseMenu;
import appeng.menu.me.common.IClientRepo;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtocore.config.GTOConfig;
import com.gtocore.integration.emi.GTEMIRecipe;
import com.gtocore.integration.emi.GTEmiEncodingHelper;
import com.gtolib.ae2.me2in1.emi.ExtendedEncodingHelper;
import com.gtolib.api.ae2.me2in1.emi.RecipeCatecoryMapping;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.AEChemicalHelper;
import com.gtolib.utils.ClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public class ME2in1Helper {
   public static void setSlotPos(Slot slot, int x, int y) {
      slot.x = x;
      slot.y = y;
   }

   private static <MENU extends Me2in1Menu> ME2in1Helper.EMIHandler<MENU> createEMIHandler(Class<MENU> menuClass) {
      return new ME2in1Helper.EMIHandler<>(menuClass);
   }

   public static AbstractRecipeHandler<Me2in1Menu> createEMI2in1() {
      return createEMIHandler(Me2in1Menu.class);
   }

   public static AbstractRecipeHandler<Wireless> createEMIWireless() {
      return createEMIHandler(Wireless.class);
   }

   public static IconButton createIconButton(final Icon icon, OnPress onPress) {
      return new IconButton(onPress) {
         @Override
         public Icon getIcon() {
            return icon;
         }
      };
   }

   private static final class EMIHandler<T extends Me2in1Menu> extends AbstractRecipeHandler<T> {
      private EMIHandler(Class<T> containerClass) {
         super(containerClass);
      }

      public Result transferRecipe(T menu, @Nullable Recipe<?> recipe, EmiRecipe emiRecipe, boolean doTransfer) {
         ResourceLocation recipeId = recipe != null ? recipe.getId() : null;
         ExtendedEncodingMenu encodingPart = menu.getEncoding();
         boolean craftingRecipe = this.isCraftingRecipe(recipe, emiRecipe);
         if (craftingRecipe && !this.fitsIn3x3Grid(recipe, emiRecipe)) {
            return Result.createFailed(ItemModText.RECIPE_TOO_LARGE.text());
         }

         boolean gtBatchSupport = ExtendedEncodingHelper.isGTBatchEncodeSupported(emiRecipe);
         if (!doTransfer) {
            IClientRepo repo = encodingPart.getClientRepo();
            Set<AEKey> craftableKeys = repo != null ? repo.getCraftableKeys() : Collections.emptySet();
            return gtBatchSupport ? new ME2in1Helper.EMIHandler.CanBeBatchEncoded(craftableKeys) : new EncodeWithCraftables(craftableKeys);
         }

         menu.gtolib$addUUID(ClientUtil.getUUID());
         if (craftingRecipe && recipeId != null) {
            ExtendedEncodingHelper.encodeCraftingRecipe(encodingPart, recipe, this.getGuiIngredientsForCrafting(emiRecipe), stack -> true);
         } else {
            if (emiRecipe instanceof GTEMIRecipe gtemiRecipe && RecipeBuilder.get(gtemiRecipe.getId()) != null) {
               menu.gtolib$addRecipe(gtemiRecipe.getId().toString());
               if (GTOConfig.INSTANCE.devMode.aeLog) {
                  AELog.info("EMI: Recipe recorded: " + gtemiRecipe.getId());
               }
            } else {
               if (GTOConfig.INSTANCE.devMode.aeLog) {
                  AELog.warn("EMI: Recipe not found in GT recipe map: " + emiRecipe.getId());
               }

               menu.gtolib$addRecipe("");
            }

            if (gtBatchSupport && Screen.hasAltDown()) {
               ExtendedEncodingHelper.encodeBatchRecipe(encodingPart, GTEmiEncodingHelper.ofInputs(emiRecipe), EmiStackHelper.ofOutputs(emiRecipe));
            } else {
               ExtendedEncodingHelper.encodeProcessingRecipe(
                  encodingPart,
                  GTEmiEncodingHelper.ofInputs(emiRecipe),
                  EmiStackHelper.ofOutputs(emiRecipe),
                  ExtendedEncodingHelper.isGTAssemblyLineRecipe(emiRecipe)
               );
            }
         }

         String recipeCategoryName;
         if (RecipeCatecoryMapping.getCategory2NameMap().containsKey(emiRecipe.getCategory().getId())) {
            recipeCategoryName = RecipeCatecoryMapping.getCategory2NameMap().get(emiRecipe.getCategory().getId());
         } else if (craftingRecipe && recipeId != null) {
            recipeCategoryName = Component.translatable("gtocore.ae.appeng.me2in1.vanilla_craft_station").getString();
         } else {
            recipeCategoryName = emiRecipe.getCategory().getName().getString();
         }

         if (menu.getScreen() != null && menu.autoSearchProviders) {
            menu.getScreen().gto$getSearchProviderField().setValue(recipeCategoryName);
         }

         return Result.createSuccessful();
      }

      private List<List<GenericStack>> getGuiIngredientsForCrafting(EmiRecipe emiRecipe) {
         ArrayList<List<GenericStack>> result = new ArrayList<>(9);

         for (int i = 0; i < 9; i++) {
            ArrayList<GenericStack> stacks = new ArrayList<>();
            if (i < emiRecipe.getInputs().size()) {
               for (EmiStack emiStack : emiRecipe.getInputs().get(i).getEmiStacks()) {
                  GenericStack genericStack = EmiStackHelper.toGenericStack(emiStack);
                  if (genericStack != null && genericStack.what() instanceof AEItemKey) {
                     stacks.add(genericStack);
                  }
               }
            }

            result.add(stacks);
         }

         return result;
      }

      @Override
      public boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
         return true;
      }

      private static class CanBeBatchEncoded extends Result {
         private final Set<AEKey> craftableKeys;

         public CanBeBatchEncoded(Set<AEKey> craftableKeys) {
            this.craftableKeys = craftableKeys;
         }

         @Override
         public boolean canCraft() {
            return true;
         }

         @Override
         public List<Component> getTooltip(EmiRecipe emiRecipe, EmiCraftContext<?> context) {
            boolean anyCraftable = emiRecipe.getInputs().stream().anyMatch(ing -> isCraftable(this.craftableKeys, ing));
            List<Component> gatheredTooltip = anyCraftable ? TransferHelper.createEncodingTooltip(true) : new ArrayList<>();
            boolean hasGTMaterial = emiRecipe.getInputs()
               .stream()
               .anyMatch(ing -> ing.getEmiStacks().stream().anyMatch(ME2in1Helper.EMIHandler.CanBeBatchEncoded::hasGTMaterial));
            boolean anyCatalyst = !emiRecipe.getCatalysts().isEmpty();
            if (anyCatalyst) {
               gatheredTooltip.add(Component.translatable("gtocore.ae.appeng.me2in1.emi.catalyst").withStyle(ChatFormatting.AQUA));
               gatheredTooltip.add(Component.translatable("gtocore.ae.appeng.me2in1.emi.catalyst.fill").withStyle(ChatFormatting.GREEN));
               gatheredTooltip.add(Component.translatable("gtocore.ae.appeng.me2in1.emi.catalyst.virtual").withStyle(ChatFormatting.DARK_GREEN));
            }

            if (hasGTMaterial) {
               gatheredTooltip.add(Component.translatable("gtocore.ae.appeng.me2in1.emi.gt_batch_encode").withStyle(ChatFormatting.YELLOW));
               gatheredTooltip.add(Component.translatable("gtocore.ae.appeng.me2in1.emi.gt_batch_encode.1").withStyle(ChatFormatting.GRAY));
            }

            return gatheredTooltip.isEmpty() ? null : gatheredTooltip;
         }

         @Override
         public void render(EmiRecipe recipe, EmiCraftContext<? extends AEBaseMenu> context, List<Widget> widgets, GuiGraphics guiGraphics) {
            for (Widget widget : widgets) {
               if (widget instanceof SlotWidget slot && AbstractRecipeHandler.isInputSlot(slot) && isCraftable(this.craftableKeys, slot.getStack())) {
                  renderSlotOverlay(guiGraphics, slot, 1073742079);
               }

               if (widget instanceof SlotWidget slot
                  && slot.getStack().getEmiStacks().stream().anyMatch(ME2in1Helper.EMIHandler.CanBeBatchEncoded::hasGTMaterial)) {
                  renderSlotOverlay(guiGraphics, slot, 1728052992);
               }
            }
         }

         private static boolean isCraftable(Set<AEKey> craftableKeys, EmiIngredient ingredient) {
            return ingredient.getEmiStacks().stream().anyMatch(emiIngredient -> {
               GenericStack stack = EmiStackHelper.toGenericStack(emiIngredient);
               return stack != null && craftableKeys.contains(stack.what());
            });
         }

         private static void renderSlotOverlay(GuiGraphics guiGraphics, SlotWidget slot, int color) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 400.0F);
            Bounds bounds = AbstractRecipeHandler.getInnerBounds(slot);
            guiGraphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), color);
            poseStack.popPose();
         }

         private static boolean hasGTMaterial(EmiStack ing) {
            return EmiStackHelper.toGenericStack(ing) != null && AEChemicalHelper.getMaterial(EmiStackHelper.toGenericStack(ing).what()) != GTMaterials.NULL;
         }
      }
   }
}
