package com.gtolib.api.ae2.me2in1.encoding;

import appeng.api.config.ActionItems;
import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.ButtonToolTips;
import com.gtolib.ae2.me2in1.panel.ModePanel;
import com.gtolib.api.ae2.me2in1.ExtendedEncodingMenu;
import com.gtolib.api.ae2.me2in1.Me2in1Menu;
import com.gtolib.api.ae2.me2in1.Me2in1Screen;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public abstract class EncodingSubPanel implements ICompositeWidget {
   private static final Blitter FRAME = Blitter.texture("guis/encoding_module.png").src(0, 0, 140, 86);
   protected final ActionButton clearBtn;
   protected final ExtendedEncodingMenu encodingMenu;
   protected final Me2in1Menu menu;
   protected final Me2in1Screen<?> screen;
   protected boolean visible = false;
   protected final ModePanel parent;
   protected int x;
   protected int y;
   private static final int BG_X_OFFSET = -9;
   private static final int BG_Y_OFFSET = -12;

   public EncodingSubPanel(ModePanel parent) {
      this.screen = parent.getScreen();
      this.parent = parent;
      this.menu = this.screen.getMenu();
      this.encodingMenu = this.menu.getEncoding();
      this.clearBtn = new ActionButton(ActionItems.CLOSE, act -> this.encodingMenu.clear());
      this.clearBtn.setHalfSize(true);
      this.screen.getSubWidgets().put("clearBtn" + this.getClass().getSimpleName(), this.clearBtn);
   }

   public abstract ItemStack getTabIconItem();

   public abstract Component getTabTooltip();

   @Override
   public void setPosition(Point position) {
      this.x = position.getX();
      this.y = position.getY();
   }

   @Override
   public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      FRAME.dest(this.x + bounds.getX() + -9, this.y + bounds.getY() + -12).blit(guiGraphics);
   }

   @Override
   public void setSize(int width, int height) {
   }

   @Override
   public Rect2i getBounds() {
      return new Rect2i(this.x, this.y, 126, 68);
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
      this.clearBtn.setVisibility(visible);
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   protected ToggleButton createSubstitutionButton() {
      ToggleButton button = new ToggleButton(Icon.SUBSTITUTION_ENABLED, Icon.SUBSTITUTION_DISABLED, this.encodingMenu::setSubstitute);
      button.setHalfSize(true);
      button.setTooltipOn(List.of(ButtonToolTips.SubstitutionsOn.text(), ButtonToolTips.SubstitutionsDescEnabled.text()));
      button.setTooltipOff(List.of(ButtonToolTips.SubstitutionsOff.text(), ButtonToolTips.SubstitutionsDescDisabled.text()));
      return button;
   }

   protected int getGuiLeft() {
      return this.screen.getGuiLeft();
   }

   protected int getGuiTop() {
      return this.screen.getGuiTop();
   }

   @Generated
   public Me2in1Screen<?> getScreen() {
      return this.screen;
   }
}
