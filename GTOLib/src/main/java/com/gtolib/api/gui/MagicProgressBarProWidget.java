package com.gtolib.api.gui;

import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@Configurable(name = "milestone_progress_bar", collapse = false)
@LDLRegister(name = "milestone_progress_bar", group = "widget.basic")
public class MagicProgressBarProWidget extends Widget implements IConfigurableWidget {
   private static final DecimalFormat MILLION_FORMAT = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
   private final List<MagicProgressBarProWidget.Milestone> milestones = new ArrayList<>();
   private int currentProgress = 0;
   private int minProgress = 0;
   private int maxProgress = 100;
   @Configurable
   private boolean showText = true;
   @Configurable
   private boolean showMilestoneMarkers = true;
   @Configurable
   private boolean showLeftLabel = true;
   @Configurable
   private Component leftLabelText = Component.literal("");
   @Configurable
   private int leftLabelColor = -1;
   @Configurable
   private int backgroundColor = -11184811;
   @Configurable
   private int borderColor = -16777216;
   @Configurable
   private int defaultProgressColor = -16711936;
   private Supplier<Integer> progressSupplier;

   private MagicProgressBarProWidget() {
      super(new Position(0, 0), new Size(100, 10));
   }

   private MagicProgressBarProWidget(int x, int y, int width, int height, int minProgress, int maxProgress) {
      super(new Position(x, y), new Size(width, height));
      this.minProgress = minProgress;
      this.maxProgress = maxProgress;
      this.currentProgress = minProgress;
   }

   public MagicProgressBarProWidget(int x, int y, int minProgress, int maxProgress) {
      this(x, y, 178, 7, minProgress, maxProgress);
   }

   public MagicProgressBarProWidget(int minProgress, int maxProgress) {
      this(10, 116, minProgress, maxProgress);
   }

   public MagicProgressBarProWidget(int minProgress, int maxProgress, int offsetY) {
      this(10, 116 - offsetY, minProgress, maxProgress);
   }

   public MagicProgressBarProWidget addStartColor(int color) {
      this.addMilestone(this.minProgress, color, Component.empty());
      return this;
   }

   public MagicProgressBarProWidget setLeftLabel(Component text) {
      this.leftLabelText = text;
      return this;
   }

   public MagicProgressBarProWidget setLeftLabel(String text) {
      this.leftLabelText = Component.literal(text);
      return this;
   }

   private float convertToPercentage(int value) {
      return this.maxProgress == this.minProgress ? 0.0F : 100.0F * (value - this.minProgress) / (this.maxProgress - this.minProgress);
   }

   public MagicProgressBarProWidget addMilestone(int position, int color, Component label) {
      int var6 = Math.max(this.minProgress, Math.min(this.maxProgress, position));
      int index = 0;

      while (index < this.milestones.size() && this.milestones.get(index).getPosition() <= var6) {
         index++;
      }

      MagicProgressBarProWidget.Milestone milestone = new MagicProgressBarProWidget.Milestone(var6, color, label);
      this.milestones.add(index, milestone);
      if (this.milestones.get(0).getPosition() != this.minProgress) {
         this.milestones.add(0, new MagicProgressBarProWidget.Milestone(this.minProgress, this.defaultProgressColor, Component.empty()));
      }

      if (this.milestones.get(this.milestones.size() - 1).getPosition() != this.maxProgress) {
         this.milestones.add(new MagicProgressBarProWidget.Milestone(this.maxProgress, this.defaultProgressColor, Component.empty()));
      }

      return this;
   }

   MagicProgressBarProWidget setMilestones(List<MagicProgressBarProWidget.Milestone> milestones) {
      this.milestones.clear();
      milestones.sort(Comparator.comparingInt(MagicProgressBarProWidget.Milestone::getPosition));
      this.milestones.addAll(milestones);
      if (this.milestones.isEmpty() || this.milestones.get(0).getPosition() != this.minProgress) {
         this.milestones.add(0, new MagicProgressBarProWidget.Milestone(this.minProgress, this.defaultProgressColor, Component.empty()));
      }

      if (this.milestones.get(this.milestones.size() - 1).getPosition() != this.maxProgress) {
         this.milestones.add(new MagicProgressBarProWidget.Milestone(this.maxProgress, this.defaultProgressColor, Component.empty()));
      }

      return this;
   }

   public MagicProgressBarProWidget clearMilestones() {
      this.milestones.clear();
      this.milestones.add(new MagicProgressBarProWidget.Milestone(this.minProgress, this.defaultProgressColor, Component.empty()));
      this.milestones.add(new MagicProgressBarProWidget.Milestone(this.maxProgress, this.defaultProgressColor, Component.empty()));
      return this;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
      if (this.progressSupplier != null) {
         this.currentProgress = this.progressSupplier.get();
      }

      this.currentProgress = Math.max(this.minProgress, Math.min(this.maxProgress, this.currentProgress));
      Position position = this.getPosition();
      Size size = this.getSize();
      int width = size.width;
      int height = size.height;
      Font fontRenderer = Minecraft.getInstance().font;
      if (this.showLeftLabel && this.leftLabelText != null) {
         String labelStr = this.leftLabelText.getString();
         if (!labelStr.isEmpty()) {
            int labelY = position.y + (height - 9) / 2;
            graphics.drawString(fontRenderer, labelStr, position.x, labelY, this.leftLabelColor, false);
            int labelWidth = fontRenderer.width(labelStr);
            position = new Position(position.x + labelWidth + 5, position.y);
            width = width - labelWidth - 5;
         }
      }

      DrawerHelper.drawSolidRect(graphics, position.x, position.y, width, height, this.backgroundColor);
      DrawerHelper.drawBorder(graphics, position.x, position.y, width, height, this.borderColor, 1);
      if (!this.milestones.isEmpty()) {
         for (int i = 0; i < this.milestones.size() - 1; i++) {
            MagicProgressBarProWidget.Milestone currentMilestone = this.milestones.get(i);
            MagicProgressBarProWidget.Milestone nextMilestone = this.milestones.get(i + 1);
            int segmentStart = currentMilestone.getPosition();
            int segmentEnd = nextMilestone.getPosition();
            int segmentColor = currentMilestone.getColor();
            if (this.currentProgress > segmentStart) {
               int segmentProgress = Math.min(this.currentProgress, segmentEnd);
               float startPct = this.convertToPercentage(segmentStart);
               float endPct = this.convertToPercentage(segmentProgress);
               float startX = position.x + startPct / 100.0F * width;
               float endX = position.x + endPct / 100.0F * width;
               float segmentWidth = endX - startX;
               if (segmentWidth > 0.0F) {
                  DrawerHelper.drawSolidRect(graphics, (int)startX, position.y + 1, (int)segmentWidth, height - 2, segmentColor);
               }
            }
         }

         if (this.showMilestoneMarkers) {
            for (MagicProgressBarProWidget.Milestone milestone : this.milestones) {
               float pct = this.convertToPercentage(milestone.getPosition());
               int markerX = position.x + (int)(pct / 100.0F * width);
               if (milestone.getPosition() > this.minProgress && milestone.getPosition() < this.maxProgress && milestone.isVisible) {
                  DrawerHelper.drawSolidRect(graphics, markerX - 1, position.y, 2, height, -1);
               }
            }
         }
      }

      if (this.showText) {
         String progressText = this.getCenterDataShow();
         int textWidth = fontRenderer.width(progressText);
         int textX = position.x + (width - textWidth) / 2;
         int textY = position.y + (height - 9) / 2;
         graphics.drawString(fontRenderer, progressText, textX + 1, textY + 1, -16777216, false);
         graphics.drawString(fontRenderer, progressText, textX, textY, -1, false);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
      if (this.isMouseOverElement(mouseX, mouseY)) {
         Position position = this.getPosition();
         Size size = this.getSize();
         int effectiveX = position.x;
         int effectiveWidth = size.width;
         if (this.showLeftLabel && this.leftLabelText != null && !this.leftLabelText.getString().isEmpty()) {
            int labelWidth = Minecraft.getInstance().font.width(this.leftLabelText.getString());
            effectiveX += labelWidth + 5;
            effectiveWidth -= labelWidth + 5;
         }

         if (mouseX >= effectiveX && mouseX <= effectiveX + effectiveWidth) {
            float hoverPercentage = (float)(mouseX - effectiveX) / effectiveWidth * 100.0F;
            int hoverValue = (int)(this.minProgress + hoverPercentage / 100.0F * (this.maxProgress - this.minProgress));
            MagicProgressBarProWidget.Milestone closestMilestone = null;
            float minDistance = Float.MAX_VALUE;

            for (MagicProgressBarProWidget.Milestone milestone : this.milestones) {
               float distance = Math.abs(milestone.getPosition() - hoverValue);
               if (distance < minDistance && !milestone.getLabel().getString().isEmpty()) {
                  minDistance = distance;
                  closestMilestone = milestone;
               }
            }

            float thresholdDistance = (this.maxProgress - this.minProgress) * 0.05F;
            if (closestMilestone != null && minDistance < thresholdDistance) {
               List<Component> tooltips = new ArrayList<>();
               tooltips.add(closestMilestone.getLabel());
               tooltips.add(Component.literal(this.getMilestoneDataShow(closestMilestone)));
               if (this.gui != null) {
                  this.gui.getModularUIGui().setHoverTooltip(tooltips, ItemStack.EMPTY, null, null);
               }
            }
         }
      }
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
      if (this.progressSupplier != null) {
         int newProgress = this.progressSupplier.get();
         if (newProgress != this.currentProgress) {
            this.currentProgress = newProgress;
         }
      }
   }

   @Override
   public void detectAndSendChanges() {
      super.detectAndSendChanges();
      if (this.progressSupplier != null) {
         int newProgress = this.progressSupplier.get();
         if (newProgress != this.currentProgress) {
            this.currentProgress = newProgress;
            this.writeUpdateInfo(1, buf -> buf.writeInt(this.currentProgress));
         }
      }
   }

   @Override
   public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
      super.readUpdateInfo(id, buffer);
      if (id == 1) {
         this.currentProgress = buffer.readInt();
      }
   }

   @Override
   public void buildConfigurator(ConfiguratorGroup father) {
      MagicProgressBarProWidget previewWidget = new MagicProgressBarProWidget() {
         @Override
         public void updateScreen() {
            super.updateScreen();
            this.setMilestones(new ArrayList<>(MagicProgressBarProWidget.this.milestones));
            this.setCurrentProgress(MagicProgressBarProWidget.this.currentProgress);
            this.setMinProgress(MagicProgressBarProWidget.this.minProgress);
            this.setMaxProgress(MagicProgressBarProWidget.this.maxProgress);
            this.setShowText(MagicProgressBarProWidget.this.showText);
            this.setShowMilestoneMarkers(MagicProgressBarProWidget.this.showMilestoneMarkers);
            this.setShowLeftLabel(MagicProgressBarProWidget.this.showLeftLabel);
            this.setLeftLabelText(MagicProgressBarProWidget.this.leftLabelText);
            this.setLeftLabelColor(MagicProgressBarProWidget.this.leftLabelColor);
            this.setBackgroundColor(MagicProgressBarProWidget.this.backgroundColor);
            this.setBorderColor(MagicProgressBarProWidget.this.borderColor);
            this.setDefaultProgressColor(MagicProgressBarProWidget.this.defaultProgressColor);
         }
      };
      previewWidget.currentProgress = (this.maxProgress - this.minProgress) / 4 * 3 + this.minProgress;
      father.addConfigurators(new WrapperConfigurator("ldlib.gui.editor.group.preview", previewWidget));
      IConfigurableWidget.super.buildConfigurator(father);
   }

   @NotNull
   private String getCenterDataShow() {
      return format(this.currentProgress) + "/" + format(this.maxProgress);
   }

   @NotNull
   private String getMilestoneDataShow(MagicProgressBarProWidget.Milestone closestMilestone) {
      return this.leftLabelText.getString() + " " + format(closestMilestone.getPosition()) + "/" + format(this.maxProgress);
   }

   private static String format(long number) {
      return number < 1000000L ? FormattingUtil.formatNumbers(number) : MILLION_FORMAT.format(number / 1000000.0) + "M";
   }

   void setCurrentProgress(int currentProgress) {
      this.currentProgress = currentProgress;
   }

   void setMinProgress(int minProgress) {
      this.minProgress = minProgress;
   }

   void setMaxProgress(int maxProgress) {
      this.maxProgress = maxProgress;
   }

   void setShowText(boolean showText) {
      this.showText = showText;
   }

   void setShowMilestoneMarkers(boolean showMilestoneMarkers) {
      this.showMilestoneMarkers = showMilestoneMarkers;
   }

   void setShowLeftLabel(boolean showLeftLabel) {
      this.showLeftLabel = showLeftLabel;
   }

   void setLeftLabelText(Component leftLabelText) {
      this.leftLabelText = leftLabelText;
   }

   void setLeftLabelColor(int leftLabelColor) {
      this.leftLabelColor = leftLabelColor;
   }

   void setBackgroundColor(int backgroundColor) {
      this.backgroundColor = backgroundColor;
   }

   void setBorderColor(int borderColor) {
      this.borderColor = borderColor;
   }

   void setDefaultProgressColor(int defaultProgressColor) {
      this.defaultProgressColor = defaultProgressColor;
   }

   @Generated
   public Component getLeftLabelText() {
      return this.leftLabelText;
   }

   @Generated
   public void setProgressSupplier(Supplier<Integer> progressSupplier) {
      this.progressSupplier = progressSupplier;
   }

   static class Milestone {
      private int position;
      private int color;
      private final Component label;
      private boolean isVisible = true;

      Milestone(int position, int color, Component label) {
         this.position = position;
         this.color = color;
         this.label = label;
      }

      int getPosition() {
         return this.position;
      }

      int getColor() {
         return this.color;
      }

      Component getLabel() {
         return this.label;
      }

      @Generated
      public void setPosition(int position) {
         this.position = position;
      }

      @Generated
      public void setColor(int color) {
         this.color = color;
      }

      @Generated
      public boolean isVisible() {
         return this.isVisible;
      }
   }
}
