package com.gtolib.api.ae2.wtlib;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import de.mari_023.ae2wtlib.AE2wtlib;
import de.mari_023.ae2wtlib.TextConstants;
import de.mari_023.ae2wtlib.wut.WTDefinition;
import de.mari_023.ae2wtlib.wut.WUTHandler;
import gto_ae.client.gui.widgets.expandable.ExpandableGroup;
import gto_ae.client.gui.widgets.expandable.IExpandable;
import gto_ae.hooks.gui.IPopulateScreenWidget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

@DataGeneratorScanned
public class CycleTerminalButton extends IconButton implements IExpandable, IPopulateScreenWidget {
   private static final int LAYOUT_SPACING = 4;
   private final CycleTerminalButton.LayoutDirection layoutDirection;
   private boolean collapsed = true;
   private final List<CycleTerminalButton.SubButton> expandedButtons = new ArrayList<>();
   private ExpandableGroup group = null;
   @RegisterLanguage(cn = "切换到%s", en = "Switch to %s")
   public static final String SwitchTerminalTooltip = "gtocore.config_button.switch_terminal_tooltip";
   @RegisterLanguage(cn = "选择切换到其他终端", en = "Select to switch to other terminal")
   public static final String CycleTerminalButtonTooltip = "gtocore.config_button.cycle_terminal_button_tooltip";
   @RegisterLanguage(cn = "取消选择切换终端", en = "Cancel Switch Terminal")
   public static final String CycleTerminalButtonCancelTooltip = "gtocore.config_button.cycle_terminal_button_cancel_tooltip";

   @Override
   public void toggleCollapsed() {
      toggleCollapsed(this);
   }

   @Override
   public boolean isCollapsed() {
      return this.collapsed;
   }

   @Override
   public void setGroup(ExpandableGroup group) {
      this.group = group;
   }

   @Override
   public ExpandableGroup getGroup() {
      return this.group;
   }

   @Override
   public Rect2i getExpandedBound() {
      if (this.expandedButtons.isEmpty()) {
         return this.getBounds();
      }

      int x = 0;
      int y = 0;
      int w = 0;
      int h = 0;
      switch (this.layoutDirection) {
         case UP:
         case DOWN: {
            int totalWidth = this.expandedButtons.stream().mapToInt(AbstractWidget::getWidth).sum();
            int totalSpacing = 4 * (this.expandedButtons.size() - 1);
            w = Math.max(this.getWidth(), totalWidth + totalSpacing);
            int maxHeight = this.expandedButtons.stream().mapToInt(AbstractWidget::getHeight).max().orElse(0);
            h = this.getHeight() + 4 + maxHeight;
            x = this.getX() + this.getWidth() / 2 - (totalWidth + totalSpacing) / 2;
            y = this.getY() + (this.layoutDirection == CycleTerminalButton.LayoutDirection.UP ? -4 - maxHeight : 0);
            break;
         }
         case LEFT:
         case RIGHT: {
            int maxWidth = this.expandedButtons.stream().mapToInt(AbstractWidget::getWidth).max().orElse(0);
            w = this.getWidth() + 4 + maxWidth;
            int totalHeight = this.expandedButtons.stream().mapToInt(AbstractWidget::getHeight).sum();
            int totalSpacing = 4 * (this.expandedButtons.size() - 1);
            h = Math.max(this.getHeight(), totalHeight + totalSpacing);
            x = this.getX() + (this.layoutDirection == CycleTerminalButton.LayoutDirection.LEFT ? -4 - maxWidth : 0);
            y = this.getY() + this.getHeight() / 2 - (totalHeight + totalSpacing) / 2;
         }
      }

      return new Rect2i(x, y, w, h);
   }

   @Override
   public void populate(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
      for (CycleTerminalButton.SubButton b : this.expandedButtons) {
         addWidget.accept(b);
      }
   }

   public CycleTerminalButton(CycleTerminalButton.LayoutDirection layoutDirection) {
      super(CycleTerminalButton::onMainButtonPress);
      this.layoutDirection = layoutDirection;
      WUTHandlerExtended.FETCH_TERMINAL_LIST.send();
   }

   public void setTerminals(List<String> terminalIds) {
      this.expandedButtons.clear();

      for (String id : terminalIds) {
         CycleTerminalButton.SubButton subBtn = new CycleTerminalButton.SubButton(id);
         subBtn.visible = false;
         this.expandedButtons.add(subBtn);
      }

      this.relayout();
   }

   @Override
   public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
      this.relayout();
      super.renderWidget(guiGraphics, mouseX, mouseY, partial);
   }

   private void onPress(String value) {
      WUTHandlerExtended.SELECT_TERMINALS_PACK.send(value);
      toggleCollapsed(this);
   }

   private static void onMainButtonPress(Button btn) {
      if (btn instanceof CycleTerminalButton btnExpandable) {
         if (!btnExpandable.expandedButtons.isEmpty()) {
            if (btnExpandable.expandedButtons.size() == 1) {
               btnExpandable.expandedButtons.getFirst().onPress();
            } else {
               toggleCollapsed(btnExpandable);
            }
         }
      }
   }

   private static void toggleCollapsed(CycleTerminalButton btnExpandable) {
      if (btnExpandable.expandedButtons.size() > 1) {
         btnExpandable.collapsed = !btnExpandable.collapsed;
         btnExpandable.relayout();
         if (btnExpandable.collapsed) {
            btnExpandable.onCollapse();
         } else {
            btnExpandable.onExpand();
         }
      }
   }

   public void relayout() {
      if (!this.collapsed) {
         int candidateCount = this.expandedButtons.size();
         int halfDistance = (candidateCount * 16 + (candidateCount - 1) * 4) / 2 - 8;

         for (int i = 0; i < candidateCount; i++) {
            CycleTerminalButton.SubButton b = this.expandedButtons.get(i);
            switch (this.layoutDirection) {
               case UP:
                  b.setY(this.getY() - 4 - b.getHeight());
                  b.setX(this.getX() + this.getWidth() / 2 - halfDistance + i * (b.getWidth() + 4));
                  break;
               case DOWN:
                  b.setY(this.getY() + this.getHeight() + 4);
                  b.setX(this.getX() + this.getWidth() / 2 - halfDistance + i * (b.getWidth() + 4));
                  break;
               case LEFT:
                  b.setX(this.getX() - 4 - b.getWidth());
                  b.setY(this.getY() + this.getHeight() / 2 - halfDistance + i * (b.getHeight() + 4));
                  break;
               case RIGHT:
                  b.setX(this.getX() + this.getWidth() + 4);
                  b.setY(this.getY() + this.getHeight() / 2 - halfDistance + i * (b.getHeight() + 4));
            }

            b.visible = true;
         }
      } else {
         for (CycleTerminalButton.SubButton b : this.expandedButtons) {
            b.visible = false;
         }
      }
   }

   @Override
   protected Item getItemOverlay() {
      return AE2wtlib.UNIVERSAL_TERMINAL;
   }

   @Nullable
   @Override
   protected Icon getIcon() {
      return Icon.TOOLBAR_BUTTON_BACKGROUND;
   }

   @Override
   public List<Component> getTooltipMessage() {
      if (this.expandedButtons.size() <= 1) {
         return Collections.singletonList(TextConstants.CYCLE_TOOLTIP);
      }

      ArrayList<Component> list = new ArrayList<>();
      list.add(
         this.collapsed
            ? Component.translatable("gtocore.config_button.cycle_terminal_button_tooltip")
            : Component.translatable("gtocore.config_button.cycle_terminal_button_cancel_tooltip")
      );
      return list;
   }

   public enum LayoutDirection {
      UP,
      DOWN,
      LEFT,
      RIGHT;

      // $VF: synthetic method
      private static CycleTerminalButton.LayoutDirection[] $values() {
         return new CycleTerminalButton.LayoutDirection[]{UP, DOWN, LEFT, RIGHT};
      }
   }

   private class SubButton extends IconButton {
      private final WTDefinition app;

      public SubButton(String value) {
         super(b -> CycleTerminalButton.this.onPress(value));
         this.app = WUTHandler.wirelessTerminals.get(value);
      }

      @Override
      protected Item getItemOverlay() {
         return (Item)this.app.item();
      }

      @Nullable
      @Override
      protected Icon getIcon() {
         return Icon.TOOLBAR_BUTTON_BACKGROUND;
      }

      @Override
      public List<Component> getTooltipMessage() {
         return Collections.singletonList(Component.translatable("gtocore.config_button.switch_terminal_tooltip", this.app.terminalName()));
      }
   }
}
