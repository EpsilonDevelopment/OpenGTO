package com.gtolib.api.ae2.me2in1.emi;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import gto_ae.client.gui.widgets.AEListBox;
import java.util.Map;
import lombok.Generated;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CategoryMappingSubScreen extends AEBaseScreen<CategoryMappingSubMenu> {
   AEListBox listBox = new AEListBox(this);

   public CategoryMappingSubScreen(CategoryMappingSubMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
      super(menu, playerInventory, title, style);
      this.widgets.add("listBox", this.listBox);
      this.listBox.setPosition(new Point(10, 20));
      this.refreshList();
      menu.setOnAddCategory(this::addMapping);
      Button btn = this.widgets.addButton("addMapping", Component.translatable("gtocore.ae.appeng.me2in1.add_mapping"), button -> EmiApi.displayAllRecipes());
      btn.setTooltip(Tooltip.create(Component.translatable("gtocore.ae.appeng.me2in1.add_mapping.desc")));
      this.widgets.addButton("back", Component.translatable("gui.back"), button -> menu.backToHost());
   }

   @Override
   protected void init() {
      super.init();
      this.refreshList();
   }

   public void refreshList() {
      this.listBox.clearItems();
      Map<ResourceLocation, String> map = RecipeCatecoryMapping.getCategory2NameMap();
      EmiApi.getRecipeManager().getCategories().forEach(emiCategory -> {
         if (map.containsKey(emiCategory.getId())) {
            this.listBox.addItem(new MappingConfigWidget(emiCategory, map.get(emiCategory.getId()), this, x$0 -> {
               AbstractWidget var10000 = this.addRenderableWidget(x$0);
            }));
         }
      });
   }

   public void addMapping(EmiRecipeCategory category) {
      Map<ResourceLocation, String> map = RecipeCatecoryMapping.getCategory2NameMap();
      if (!map.containsKey(category.getId())) {
         this.listBox.addItem(new MappingConfigWidget(category, map.get(category.getId()), this, x$0 -> {
            AbstractWidget var10000 = this.addRenderableWidget(x$0);
         }));
      }
   }

   @Generated
   public AEListBox getListBox() {
      return this.listBox;
   }
}
