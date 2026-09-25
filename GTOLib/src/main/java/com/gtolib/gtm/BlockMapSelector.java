package com.gtolib.gtm;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gtocore.common.block.BlockMap;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

final class BlockMapSelector extends WidgetGroup {
   public static final GuiTextureGroup CATEGORY_TEXTURE = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("◀"));
   public static final GuiTextureGroup CATEGORY_TEXTURE_OPEN = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("▶"));
   public static final GuiTextureGroup CATEGORY_TEXTURE_AQUA = new GuiTextureGroup(
      GuiTextures.VANILLA_BUTTON, new TextTexture("◀").setColor(ChatFormatting.AQUA.getColor())
   );
   public static final GuiTextureGroup CATEGORY_TEXTURE_OPEN_AQUA = new GuiTextureGroup(
      GuiTextures.VANILLA_BUTTON, new TextTexture("▶").setColor(ChatFormatting.AQUA.getColor())
   );
   public static final GuiTextureGroup BLOCK_TEXTURE = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("✔"));
   public static final GuiTextureGroup BLOCK_TEXTURE_CHOOSE = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("✘"));
   public static final GuiTextureGroup BLOCK_TEXTURE_GREEN = new GuiTextureGroup(
      GuiTextures.VANILLA_BUTTON, new TextTexture("✔").setColor(ChatFormatting.GREEN.getColor())
   );
   public static final GuiTextureGroup BLOCK_TEXTURE_CHOOSE_GREEN = new GuiTextureGroup(
      GuiTextures.VANILLA_BUTTON, new TextTexture("✘").setColor(ChatFormatting.GREEN.getColor())
   );
   static int blockMapSize;
   static int blockMapValuesMaxSize;
   static boolean frozen = false;
   private final Widget categoryScrollArea;
   private final Widget blockScrollArea;
   private final WidgetGroup categoryPlaceholder;
   private final WidgetGroup blockPlaceholder;
   private final WidgetGroup[] categoryWidgets;
   private final BlockMapSelector.BlockSelectorButton[] blockMapValuesWidgets;
   private int tier = 0;
   private final ItemStack terminalStack;
   private final BiConsumer<String, Integer> onConfirm;
   private boolean isCategoryScrollAreaOpen = false;
   private String openCategory = null;

   public BlockMapSelector(int x, int y, int width, int height, BooleanConsumer onShowingChange, ItemStack terminalStack) {
      super(x, y, width, height);
      if (!frozen) {
         blockMapSize = BlockMap.MAP.size();
         blockMapValuesMaxSize = BlockMap.MAP.values().stream().mapToInt(map -> map.length).max().orElse(0);
         frozen = true;
      }

      this.categoryWidgets = new WidgetGroup[blockMapSize];
      this.blockMapValuesWidgets = new BlockMapSelector.BlockSelectorButton[blockMapValuesMaxSize];
      this.terminalStack = terminalStack;
      this.onConfirm = (category, tier0) -> {
         if (category != null && tier0 != null) {
            CompoundTag tag = terminalStack.getOrCreateTag();
            CompoundTag tag0 = tag.getCompound("blocks");
            if (tag0.contains(category) && tag0.getInt(category) == tier0) {
               tag0.remove(category);
            } else {
               tag0.putInt(category, tier0);
            }

            tag.put("blocks", tag0);
            terminalStack.setTag(tag);
         }
      };
      this.categoryPlaceholder = new WidgetGroup();
      this.blockPlaceholder = new WidgetGroup();
      DraggableScrollableWidgetGroup scrollArea1 = new DraggableScrollableWidgetGroup(0, 4, 45, 172)
         .setYScrollBarWidth(2)
         .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F));
      scrollArea1.addWidget(this.categoryPlaceholder);
      DraggableScrollableWidgetGroup scrollArea2 = new DraggableScrollableWidgetGroup(0, 4, 45, 172)
         .setYScrollBarWidth(2)
         .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F));
      scrollArea2.addWidget(this.blockPlaceholder);
      this.categoryScrollArea = new WidgetGroup(this.getSizeWidth() + 19, -8, 50, 180)
         .addWidget(scrollArea1)
         .setBackground(GuiTextures.BACKGROUND_INVERSE)
         .setActive(false)
         .setVisible(false);
      this.blockScrollArea = new WidgetGroup(this.getSizeWidth() + 23 + 50, -8, 50, 180)
         .addWidget(scrollArea2)
         .setBackground(GuiTextures.BACKGROUND_INVERSE)
         .setActive(false)
         .setVisible(false);
      this.addWidget(this.categoryScrollArea);
      this.addWidget(this.blockScrollArea);
      this.addWidget(
         new ToggleButtonWidget(
               this.getSizeWidth() - this.getSizeHeight(), 0, this.getSizeHeight(), this.getSizeHeight(), this::isCategoryScrollAreaOpen, pressed -> {
                  this.isCategoryScrollAreaOpen = pressed;
                  if (pressed) {
                     this.categoryScrollArea.setActive(true);
                     this.categoryScrollArea.setVisible(true);
                     this.onCategoryAreaActivate();
                  } else {
                     this.collapseAll();
                  }

                  onShowingChange.accept(pressed);
               }
            )
            .setTexture(
               new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("✎")), new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("✘"))
            )
            .setHoverTooltips(Component.translatable("gtocore.adv_terminal.block.select"))
      );
   }

   public void collapseAll() {
      this.isCategoryScrollAreaOpen = false;
      this.openCategory = null;
      this.categoryScrollArea.setActive(false);
      this.categoryScrollArea.setVisible(false);
      this.blockScrollArea.setActive(false);
      this.blockScrollArea.setVisible(false);
   }

   private boolean isSelectedBlock(String category, Block block) {
      CompoundTag tag = this.terminalStack.getOrCreateTag();
      CompoundTag blocksTag = tag.getCompound("blocks");
      int tier = blocksTag.getInt(category);
      Block[] blockList = (Block[])BlockMap.MAP.get(category);
      return blockList != null && tier - 1 >= 0 && tier - 1 < blockList.length ? blockList[tier - 1] == block : false;
   }

   public void onCategoryAreaActivate() {
      AtomicInteger rowIndex = new AtomicInteger(0);

      for (Entry<String, Block[]> entry : BlockMap.MAP.entrySet()) {
         String category = entry.getKey();
         BooleanSupplier hadSelectedBlock = () -> this.terminalStack.getOrCreateTag().getCompound("blocks").contains(category);
         if (this.categoryWidgets[rowIndex.get()] != null) {
            rowIndex.getAndIncrement();
         } else {
            WidgetGroup widgetGroup = new WidgetGroup(0, 0, this.getSizeWidth() - 8, 18);
            this.categoryWidgets[rowIndex.get()] = widgetGroup;
            widgetGroup.setVisible(true);
            widgetGroup.setActive(true);
            widgetGroup.addWidget(new BlockMapSelector.ItemStackWidget(entry.getValue()[0].asItem().getDefaultInstance(), 4, 18 * rowIndex.get(), null));
            ToggleButtonWidget toggleButton = new ToggleButtonWidget(
               30, 2 + 18 * rowIndex.getAndIncrement(), 12, 12, () -> category.equals(this.openCategory), pressed -> {}
            );
            toggleButton.setTexture(
                  hadSelectedBlock.getAsBoolean() ? CATEGORY_TEXTURE_AQUA : CATEGORY_TEXTURE,
                  hadSelectedBlock.getAsBoolean() ? CATEGORY_TEXTURE_OPEN_AQUA : CATEGORY_TEXTURE_OPEN
               )
               .setHoverTooltips(Component.translatable("gtocore.adv_terminal.category.select"));
            BooleanConsumer booleanConsumer = pressed -> {
               if (pressed) {
                  this.openCategory = category;
                  this.blockScrollArea.setActive(true);
                  this.blockScrollArea.setVisible(true);
                  this.onBlockAreaActivate(
                     category,
                     () -> toggleButton.setTexture(
                        hadSelectedBlock.getAsBoolean() ? CATEGORY_TEXTURE_AQUA : CATEGORY_TEXTURE,
                        hadSelectedBlock.getAsBoolean() ? CATEGORY_TEXTURE_OPEN_AQUA : CATEGORY_TEXTURE_OPEN
                     )
                  );
               } else {
                  if (category.equals(this.openCategory)) {
                     this.openCategory = null;
                  }

                  this.blockScrollArea.setActive(false);
                  this.blockScrollArea.setVisible(false);
               }
            };
            toggleButton.setOnPressCallback((cd, bool) -> booleanConsumer.accept(bool.booleanValue()));
            widgetGroup.addWidget(toggleButton);
            this.categoryPlaceholder.addWidget(widgetGroup);
         }
      }

      this.categoryPlaceholder.setSize(this.getSizeWidth() - 8, rowIndex.get() * 18 + 20);
   }

   public void onBlockAreaActivate(String category, Runnable callback) {
      int rowIndex = 0;

      for (BlockMapSelector.BlockSelectorButton blockMapValuesWidget : this.blockMapValuesWidgets) {
         if (blockMapValuesWidget != null) {
            this.blockPlaceholder.removeWidget(blockMapValuesWidget);
         }
      }

      for (Block block : BlockMap.MAP.getOrDefault(category, new Block[0])) {
         BlockMapSelector.BlockSelectorButton widgetGroup;
         if (this.blockMapValuesWidgets[rowIndex] != null) {
            widgetGroup = this.blockMapValuesWidgets[rowIndex];
         } else {
            widgetGroup = new BlockMapSelector.BlockSelectorButton();
            this.blockMapValuesWidgets[rowIndex] = widgetGroup;
         }

         this.blockPlaceholder.addWidget(widgetGroup);
         widgetGroup.tier = rowIndex + 1;
         widgetGroup.label.setStack(block.asItem().getDefaultInstance());
         widgetGroup.label.setSelfPosition(5, 18 * rowIndex);
         widgetGroup.button.setSelfPosition(30, 2 + 18 * rowIndex++);
         widgetGroup.button
            .setTexture(
               this.isSelectedBlock(category, block) ? BLOCK_TEXTURE_GREEN : BLOCK_TEXTURE,
               this.isSelectedBlock(category, block) ? BLOCK_TEXTURE_CHOOSE_GREEN : BLOCK_TEXTURE_CHOOSE
            );
         widgetGroup.button.setSupplier(() -> this.isSelectedBlock(category, block));
         widgetGroup.button.setOnPressCallback((cd, pressed) -> {
            this.tier = widgetGroup.tier;
            this.onConfirm.accept(category, this.tier);
            callback.run();
            this.onBlockAreaActivate(category, callback);
         });
         if (this.isSelectedBlock(category, block)) {
            widgetGroup.button.setHoverTooltips(Component.translatable("gtocore.adv_terminal.block.cancel"));
         } else {
            widgetGroup.button.setHoverTooltips(Component.translatable("gtocore.adv_terminal.block.confirm"));
         }
      }

      this.blockPlaceholder.setSize(this.getSizeWidth() - 8, rowIndex * 18 + 20);
   }

   @Generated
   public boolean isCategoryScrollAreaOpen() {
      return this.isCategoryScrollAreaOpen;
   }

   private static class BlockSelectorButton extends WidgetGroup {
      private final BlockMapSelector.ItemStackWidget label = new BlockMapSelector.ItemStackWidget(ItemStack.EMPTY, 5, 5, null);
      private final ToggleButtonWidget button = new ToggleButtonWidget(20, 7, 12, 12, () -> false, p -> {});
      private int tier;

      public BlockSelectorButton() {
         super(0, 0, 100, 20);
         this.button.setTexture(BlockMapSelector.BLOCK_TEXTURE, BlockMapSelector.BLOCK_TEXTURE_CHOOSE);
         this.addWidget(this.label);
         this.addWidget(this.button);
      }
   }

   private static class ItemStackWidget extends Widget {
      private ItemStack stack;

      public ItemStackWidget(ItemStack itemStack, int x, int y, @Nullable String altTxt) {
         super(x, y, 16, 16);
         this.stack = itemStack;
         if (altTxt != null) {
            this.setHoverTooltips(Component.literal(altTxt));
         }
      }

      @Override
      public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
         Position position = this.getPosition();
         int stackX = position.x;
         int stackY = position.y;
         DrawerHelper.drawItemStack(graphics, this.stack, stackX, stackY, -1, null);
      }

      @Override
      public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
         super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
         this.setHoverTooltips(Screen.getTooltipFromItem(Minecraft.getInstance(), this.stack));
      }

      @Generated
      public void setStack(ItemStack stack) {
         this.stack = stack;
      }
   }
}
