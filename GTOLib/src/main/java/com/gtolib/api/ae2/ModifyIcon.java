package com.gtolib.api.ae2;

import com.google.common.collect.ImmutableMap.Builder;
import com.gtolib.GTOCore;
import gto_ae.hooks.gui.IIcon;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public enum ModifyIcon implements IIcon {
   MULTIPLY_2(0, 0, 2),
   MULTIPLY_3(16, 0, 3),
   MULTIPLY_5(32, 0, 5),
   DIVISION_2(0, 16, -2),
   DIVISION_3(16, 16, -3),
   DIVISION_5(32, 16, -5),
   TOOLBAR_BUTTON_BACKGROUND(32, 32),
   QUICK_REMOVE(48, 0),
   DIRECTLY_ENCODE_TO_GRID(48, 16),
   RECORD_RECIPE_INFO(48, 32),
   CLEAR_SEC_OUTPUT(48, 48);

   private final int x;
   private final int y;
   private final int getBy;
   private static final ResourceLocation TEXTURE = GTOCore.id("textures/gui/states.png");
   private static final int TEXTURE_WIDTH = 64;
   private static final int TEXTURE_HEIGHT = 64;
   static final Map<Integer, ModifyIcon> getByMap;

   ModifyIcon(int x, int y, int getBy) {
      this.x = x;
      this.y = y;
      this.getBy = getBy;
   }

   ModifyIcon(int x, int y) {
      this(x, y, 0);
   }

   @Override
   public ResourceLocation getIconTexture() {
      return TEXTURE;
   }

   @Override
   public int getIconX() {
      return this.x;
   }

   @Override
   public int getIconY() {
      return this.y;
   }

   @Override
   public int getIconWidth() {
      return 16;
   }

   @Override
   public int getIconHeight() {
      return 16;
   }

   @Override
   public int getIconTextureHeight() {
      return 64;
   }

   @Override
   public int getIconTextureWidth() {
      return 64;
   }

   public static ModifyIcon getBy(int value) {
      return getByMap.get(value);
   }

   // $VF: synthetic method
   private static ModifyIcon[] $values() {
      return new ModifyIcon[]{
         MULTIPLY_2,
         MULTIPLY_3,
         MULTIPLY_5,
         DIVISION_2,
         DIVISION_3,
         DIVISION_5,
         TOOLBAR_BUTTON_BACKGROUND,
         QUICK_REMOVE,
         DIRECTLY_ENCODE_TO_GRID,
         RECORD_RECIPE_INFO,
         CLEAR_SEC_OUTPUT
      };
   }

   static {
      Builder<Integer, ModifyIcon> mapBuilder = new Builder<>();

      for (ModifyIcon icon : values()) {
         if (icon.getBy != 0) {
            mapBuilder.put(icon.getBy, icon);
         }
      }

      getByMap = mapBuilder.build();
   }
}
