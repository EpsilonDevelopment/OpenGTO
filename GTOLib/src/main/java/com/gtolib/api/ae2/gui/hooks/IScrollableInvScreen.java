package com.gtolib.api.ae2.gui.hooks;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.Scrollbar;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.slot.AppEngSlot;
import com.gtolib.api.ae2.gui.BlitterGroup;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.NotNull;

public interface IScrollableInvScreen<MENU extends AEBaseMenu, SCREEN extends AEBaseScreen<MENU>> {
   BlitterGroup gto$ae$SCROLLBAR_BG = BlitterGroup.createFromSplittingBlitter(Blitter.texture("guis/pattern_provider_scrollbar.png").src(0, 0, 21, 103), 17, 18);
   int SLOT_HEIGHT = 18;
   int SLOT_NUMBER_PER_ROW = 9;
   int BG_WIDTH = 21;

   @NotNull
   MENU gto$ae$getMenu();

   SCREEN gto$ae$getSelf();

   boolean gto$ae$shouldAddScrollBar();

   void gto$ae$setScrollBar(Scrollbar var1);

   Scrollbar gto$ae$getScrollBar();

   List<AppEngSlot> gto$ae$getScrollerSlots();

   SlotSemantic gto$ae$getScrollableSlotSemantic();

   int gto$ae$getEffectiveRowCount();

   int gto$ae$getBGHeight();

   String gto$ae$getStylesheetName();

   default boolean gto$ae$isVisible() {
      return true;
   }

   default void gto$ae$initScrollBar() {
      if (this.gto$ae$shouldAddScrollBar()) {
         Scrollbar gto$ae$scrollbar = (new Scrollbar(Scrollbar.DEFAULT) {
            @Override
            public boolean isVisible() {
               return IScrollableInvScreen.this.gto$ae$isVisible();
            }
         }).setHeight(18 * this.gto$ae$getEffectiveRowCount());
         gto$ae$scrollbar.setRange(
            0,
            (int)Math.ceil((this.gto$ae$getMenu().getSlots(this.gto$ae$getScrollableSlotSemantic()).size() - this.gto$ae$getEffectiveRowCount() * 9) / 9.0F),
            1
         );
         ((AEBaseScreen)this).getWidgets().add(this.gto$ae$getStylesheetName() + "Scrollbar", gto$ae$scrollbar);
         this.gto$ae$setScrollBar(gto$ae$scrollbar);
         ((AEBaseScreen)this)
            .getWidgets()
            .add(
               this.gto$ae$getStylesheetName() + "ScrollbarBackground",
               new ICompositeWidget() {
                  private int x = 0;
                  private int y = 0;

                  @Override
                  public void setPosition(Point position) {
                     this.x = position.getX();
                     this.y = position.getY();
                  }

                  @Override
                  public void setSize(int width, int height) {
                  }

                  @Override
                  public Rect2i getBounds() {
                     return new Rect2i(this.x, this.y, 21, IScrollableInvScreen.this.gto$ae$getBGHeight());
                  }

                  @Override
                  public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
                     ICompositeWidget.super.drawBackgroundLayer(guiGraphics, bounds, mouse);
                     IScrollableInvScreen.gto$ae$SCROLLBAR_BG
                        .copy()
                        .dest(this.x + IScrollableInvScreen.this.gto$ae$getSelf().getGuiLeft(), this.y + IScrollableInvScreen.this.gto$ae$getSelf().getGuiTop())
                        .vBlitFixedHeight(guiGraphics, IScrollableInvScreen.this.gto$ae$getBGHeight());
                  }

                  @Override
                  public boolean isVisible() {
                     return IScrollableInvScreen.this.gto$ae$isVisible();
                  }
               }
            );
      }
   }

   default void gto$ae$repositionSlots() {
      if (this.gto$ae$getScrollBar() != null && this.gto$ae$isVisible()) {
         this.gto$ae$getSelf().repositionSlots(this.gto$ae$getScrollableSlotSemantic());

         for (int i = 0; i < this.gto$ae$getScrollerSlots().size(); i++) {
            AppEngSlot slot = this.gto$ae$getScrollerSlots().get(i);
            int effectiveRow = i / 9 - this.gto$ae$getScrollBar().getCurrentScroll();
            slot.setActive(effectiveRow >= 0 && effectiveRow < this.gto$ae$getEffectiveRowCount());
            slot.y = slot.y - this.gto$ae$getScrollBar().getCurrentScroll() * 18;
         }
      }
   }
}
