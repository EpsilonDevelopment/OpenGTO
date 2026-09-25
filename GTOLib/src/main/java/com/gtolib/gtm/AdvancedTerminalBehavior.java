package com.gtolib.gtm;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.client.renderer.MultiblockInWorldPreviewRenderer;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gtocore.common.block.BlockMap;
import com.gtolib.api.machine.MultiblockDefinition;
import com.hepdd.gtmthings.api.gui.widget.TerminalInputWidget;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory.HeldItemHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class AdvancedTerminalBehavior implements IItemUIFactory {
   private static final int xInput = 140;
   private static final int wInput = 34;
   public static final String tooltipEnabled = "attributeslib.value.boolean.enabled";
   public static final String tooltipDisabled = "attributeslib.value.boolean.disabled";
   private static final String KEY_REPEAT_COUNT = "repeatCount";
   private static final String KEY_MAIN_INDEX = "mainIndex";
   private static final String KEY_MODULE_NUMBER = "moduleNumber";
   private static final String KEY_IS_REPLACE_MODE = "isReplaceMode";
   private static final String KEY_IS_DEMOLITION_MODE = "isDemolitionMode";
   private static final String KEY_IS_USE_AE_MODE = "isUseAEMode";
   private static final String KEY_IS_FLIP_MODE = "isFlipMode";
   private static final String KEY_IS_NO_HATCH_MODE = "isNoHatchMode";

   @Override
   public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
      Player player = context.getPlayer();
      Level level = context.getLevel();
      BlockPos blockPos = context.getClickedPos();
      if (player != null && MetaMachine.getMachine(level, blockPos) instanceof IMultiController controller) {
         AutoBuildSetting var8 = getAutoBuildSetting(itemStack);
         this.usePattern(var8, controller, player, level, blockPos);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   private void usePattern(AutoBuildSetting autoBuildSetting, IMultiController controller, Player player, Level level, BlockPos blockPos) {
      Supplier<BlockPattern>[] mainPattern = controller.getPattern();
      Supplier<BlockPattern>[] subPattern = controller.getSubPattern();
      BlockPattern pattern;
      if (autoBuildSetting.module > 0 && subPattern != null && subPattern.length > 0) {
         pattern = subPattern[Math.min(subPattern.length, autoBuildSetting.module) - 1].get();
      } else {
         pattern = mainPattern[Math.min(mainPattern.length - 1, autoBuildSetting.mainIndex)].get();
      }

      if (player.isShiftKeyDown()) {
         if (!level.isClientSide) {
            controller.requestCheck();
            controller.setWaitingTime(10);
            AdvancedBlockPattern.getAdvancedBlockPattern(pattern).autoBuild((ServerPlayer)player, controller.getMultiblockState(), autoBuildSetting);
            controller.getMultiblockState().clearCache();
            if (!autoBuildSetting.isDemolitionMode) {
               controller.setWaitingTime(0);
            }
         }
      } else if (level.isClientSide) {
         MultiblockInWorldPreviewRenderer.showPreview(
            blockPos,
            controller.self().getFrontFacing(),
            controller.self().getUpwardsFacing(),
            MultiblockDefinition.getClampedMatchingShape(pattern, autoBuildSetting.repeatCount),
            ConfigHolder.INSTANCE.client.inWorldPreviewDuration * 20
         );
      }
   }

   @Override
   public ModularUI createUI(HeldItemHolder holder, Player entityPlayer) {
      ItemStack held = holder.getHeld();
      return new ModularUI(176, 166, holder, entityPlayer).widget(createWidget(held));
   }

   private static Widget createWidget(ItemStack handItem) {
      WidgetGroup group = new WidgetGroup(0, 0, 190, 162);
      int rowIndex = 1;
      group.addWidget(
         new DraggableScrollableWidgetGroup(4, 4, 182, 154)
            .setUseScissor(false)
            .setBackground(GuiTextures.DISPLAY)
            .setYScrollBarWidth(2)
            .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F))
            .addWidget(new LabelWidget(40, 5, "item.gtmthings.advanced_terminal.setting.title"))
            .addWidget(new BlockMapSelector(96, 4, 76, 12, shown -> group.setSelfPositionX(shown ? -70 : 0), handItem))
            .addWidget(
               new LabelWidget(4, 5 + 16 * rowIndex, Component.translatable("item.gtmthings.advanced_terminal.setting.2"))
                  .setHoverTooltips(Component.translatable("item.gtmthings.advanced_terminal.setting.2.tooltip"))
            )
            .addWidget(
               new TerminalInputWidget(140, 5 + 16 * rowIndex++, 34, 16, () -> getTagInt(handItem, "repeatCount"), i -> setTagInt(handItem, "repeatCount", i))
                  .setMin(0)
                  .setMax(1000)
            )
            .addWidget(new LabelWidget(4, 5 + 16 * rowIndex, "gtocore.auto_build.main"))
            .addWidget(
               new TerminalInputWidget(140, 5 + 16 * rowIndex, 26, 16, () -> getTagInt(handItem, "mainIndex"), v -> setTagInt(handItem, "mainIndex", v))
                  .setMin(0)
                  .setMax(100)
            )
            .addWidget(
               new ButtonWidget(
                  124,
                  4 + 16 * rowIndex,
                  14,
                  14,
                  new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("◀")),
                  cd -> setTagInt(handItem, "mainIndex", getTagInt(handItem, "mainIndex") - (cd.isCtrlClick ? 64 : (cd.isShiftClick ? 8 : 1)))
               )
            )
            .addWidget(
               new ButtonWidget(
                  168,
                  4 + 16 * rowIndex++,
                  14,
                  14,
                  new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("▶")),
                  cd -> setTagInt(handItem, "mainIndex", getTagInt(handItem, "mainIndex") + (cd.isCtrlClick ? 64 : (cd.isShiftClick ? 8 : 1)))
               )
            )
            .addWidget(new LabelWidget(4, 5 + 16 * rowIndex, "gtocore.auto_build.module"))
            .addWidget(
               new TerminalInputWidget(140, 5 + 16 * rowIndex, 26, 16, () -> getTagInt(handItem, "moduleNumber"), v -> setTagInt(handItem, "moduleNumber", v))
                  .setMin(0)
                  .setMax(100)
            )
            .addWidget(
               new ButtonWidget(
                  124,
                  4 + 16 * rowIndex,
                  14,
                  14,
                  new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("◀")),
                  cd -> setTagInt(handItem, "moduleNumber", getTagInt(handItem, "moduleNumber") - (cd.isCtrlClick ? 64 : (cd.isShiftClick ? 8 : 1)))
               )
            )
            .addWidget(
               new ButtonWidget(
                  168,
                  4 + 16 * rowIndex++,
                  14,
                  14,
                  new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("▶")),
                  cd -> setTagInt(handItem, "moduleNumber", getTagInt(handItem, "moduleNumber") + (cd.isCtrlClick ? 64 : (cd.isShiftClick ? 8 : 1)))
               )
            )
            .addWidget(new LabelWidget(4, 5 + 16 * rowIndex, "gtocore.auto_build.replace").setHoverTooltips("gtocore.auto_build.replace.a"))
            .addWidget(switchWidget(140, 5 + 16 * rowIndex++, () -> getTagBoolean(handItem, "isReplaceMode"), b -> setTagBoolean(handItem, "isReplaceMode", b)))
            .addWidget(new LabelWidget(4, 5 + 16 * rowIndex, "gtocore.auto_build.demolition_mode"))
            .addWidget(
               switchWidget(140, 5 + 16 * rowIndex++, () -> getTagBoolean(handItem, "isDemolitionMode"), b -> setTagBoolean(handItem, "isDemolitionMode", b))
            )
            .addWidget(
               new LabelWidget(4, 5 + 16 * rowIndex, Component.translatable("item.gtmthings.advanced_terminal.setting.5"))
                  .setHoverTooltips(Component.translatable("item.gtmthings.advanced_terminal.setting.5.tooltip"))
            )
            .addWidget(switchWidget(140, 5 + 16 * rowIndex++, () -> getTagBoolean(handItem, "isUseAEMode"), b -> setTagBoolean(handItem, "isUseAEMode", b)))
            .addWidget(new LabelWidget(4, 5 + 16 * rowIndex, "gtocore.auto_build.flip"))
            .addWidget(switchWidget(140, 5 + 16 * rowIndex++, () -> getTagBoolean(handItem, "isFlipMode"), b -> setTagBoolean(handItem, "isFlipMode", b)))
            .addWidget(
               new LabelWidget(4, 5 + 16 * rowIndex, Component.translatable("item.gtmthings.advanced_terminal.setting.3"))
                  .setHoverTooltips(Component.translatable("item.gtmthings.advanced_terminal.setting.3.tooltip"))
            )
            .addWidget(
               switchWidget(140, 5 + 16 * rowIndex++, () -> getTagBoolean(handItem, "isNoHatchMode", true), b -> setTagBoolean(handItem, "isNoHatchMode", b))
            )
      );
      group.setBackground(GuiTextures.BACKGROUND_INVERSE);
      return group;
   }

   private static SwitchWidget switchWidget(int x, int y, BooleanSupplier getter, BooleanConsumer setter) {
      SwitchWidget switchWidget = new SwitchWidget(x - 2, y - 2, 16, 16, null);
      BooleanConsumer updateTooltip = isPressed -> switchWidget.setHoverTooltips(
         isPressed ? "attributeslib.value.boolean.enabled" : "attributeslib.value.boolean.disabled"
      );
      switchWidget.setOnPressCallback((cd, result) -> {
         if (!cd.isRemote) {
            setter.accept(result.booleanValue());
         } else {
            updateTooltip.accept(result.booleanValue());
         }
      });
      switchWidget.setPressed(getter.getAsBoolean())
         .setBaseTexture(GuiTextures.BUTTON, GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy().getSubTexture(0.0, 0.0, 1.0, 0.5).scale(0.8F))
         .setPressedTexture(GuiTextures.BUTTON, GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy().getSubTexture(0.0, 0.5, 1.0, 0.5).scale(0.8F));
      updateTooltip.accept(getter.getAsBoolean());
      return switchWidget;
   }

   private static AutoBuildSetting getAutoBuildSetting(ItemStack itemStack) {
      AutoBuildSetting autoBuildSetting = new AutoBuildSetting();
      autoBuildSetting.repeatCount = getTagInt(itemStack, "repeatCount");
      autoBuildSetting.mainIndex = getTagInt(itemStack, "mainIndex");
      autoBuildSetting.module = getTagInt(itemStack, "moduleNumber");
      autoBuildSetting.isReplaceMode = getTagBoolean(itemStack, "isReplaceMode");
      autoBuildSetting.isDemolitionMode = getTagBoolean(itemStack, "isDemolitionMode");
      autoBuildSetting.isUseAEMode = getTagBoolean(itemStack, "isUseAEMode");
      autoBuildSetting.isFlipMode = getTagBoolean(itemStack, "isFlipMode");
      autoBuildSetting.isNoHatchMode = getTagBoolean(itemStack, "isNoHatchMode", true);
      CompoundTag blocks = itemStack.getOrCreateTag().getCompound("blocks");
      if (!blocks.isEmpty()) {
         Set<Block> blockSet = new ReferenceOpenHashSet<>();

         for (String category : BlockMap.MAP.keySet()) {
            Block[] categoryBlocks = (Block[])BlockMap.MAP.get(category);
            int tier0 = blocks.getInt(category);
            if (tier0 > 0 && tier0 <= categoryBlocks.length) {
               blockSet.addAll(Arrays.asList(categoryBlocks));
            }

            autoBuildSetting.categoryTierMap.put(category, tier0);
         }

         autoBuildSetting.blocks = blockSet;
      }

      return autoBuildSetting;
   }

   private static int getTagInt(ItemStack stack, String key, int defaultValue) {
      CompoundTag tag = stack.getTag();
      return tag != null && tag.contains(key) ? tag.getInt(key) : defaultValue;
   }

   private static int getTagInt(ItemStack stack, String key) {
      return getTagInt(stack, key, 0);
   }

   private static void setTagInt(ItemStack stack, String key, int value) {
      stack.getOrCreateTag().putInt(key, value);
   }

   private static boolean getTagBoolean(ItemStack stack, String key, boolean defaultValue) {
      CompoundTag tag = stack.getTag();
      return tag != null && tag.contains(key, 1) ? tag.getBoolean(key) : defaultValue;
   }

   private static boolean getTagBoolean(ItemStack stack, String key) {
      return getTagBoolean(stack, key, false);
   }

   private static void setTagBoolean(ItemStack stack, String key, boolean value) {
      stack.getOrCreateTag().putBoolean(key, value);
   }
}
