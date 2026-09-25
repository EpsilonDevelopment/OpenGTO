package com.gtolib.api.ae2.me2in1.emi;

import appeng.api.storage.ISubMenuHost;
import appeng.integration.modules.emi.AbstractRecipeHandler;
import appeng.integration.modules.emi.AbstractRecipeHandler.Result;
import appeng.integration.modules.emi.AbstractRecipeHandler.Result.EncodeWithCraftables;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import java.util.Collections;
import java.util.function.Consumer;
import lombok.Generated;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public class CategoryMappingSubMenu extends AEBaseMenu implements ISubMenu {
   public static final MenuType<CategoryMappingSubMenu> TYPE = MenuTypeBuilder.create(CategoryMappingSubMenu::new, ISubMenuHost.class)
      .build("category_mapping_sub_menu");
   private final ISubMenuHost host;
   private Consumer<EmiRecipeCategory> onAddCategory;

   public CategoryMappingSubMenu(MenuType<?> menuType, int id, Inventory playerInventory, ISubMenuHost host) {
      super(menuType, id, playerInventory, host);
      this.host = host;
      this.registerClientAction("return", this::backToHost);
   }

   @Override
   public ISubMenuHost getHost() {
      return this.host;
   }

   public void backToHost() {
      if (this.isClientSide()) {
         this.sendClientAction("return");
      } else {
         this.host.returnToMainMenu(this.getPlayer(), this);
      }
   }

   @Generated
   public Consumer<EmiRecipeCategory> getOnAddCategory() {
      return this.onAddCategory;
   }

   @Generated
   public void setOnAddCategory(Consumer<EmiRecipeCategory> onAddCategory) {
      this.onAddCategory = onAddCategory;
   }

   public static class EmiHandler extends AbstractRecipeHandler<CategoryMappingSubMenu> {
      public EmiHandler() {
         super(CategoryMappingSubMenu.class);
      }

      public Result transferRecipe(CategoryMappingSubMenu menu, @Nullable Recipe<?> holder, EmiRecipe emiRecipe, boolean doTransfer) {
         if (doTransfer) {
            if (menu.getOnAddCategory() != null) {
               menu.getOnAddCategory().accept(emiRecipe.getCategory());
            }

            return Result.createSuccessful();
         } else {
            return new EncodeWithCraftables(Collections.emptySet());
         }
      }
   }
}
