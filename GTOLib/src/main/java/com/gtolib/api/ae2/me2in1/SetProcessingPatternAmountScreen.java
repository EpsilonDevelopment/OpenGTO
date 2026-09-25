package com.gtolib.api.ae2.me2in1;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.me.common.ClientDisplaySlot;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.TabButton;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import com.google.common.primitives.Longs;
import com.gtocore.config.GTOConfig;
import com.gtolib.Client;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SetProcessingPatternAmountScreen<C extends Me2in1Menu> extends AESubScreen<C, AEBaseScreen<C>> {
   private final NumberEntryWidget amount;
   private final GenericStack currentStack;
   private final Consumer<GenericStack> setter;
   private final AETextField rename;
   private final AETextField defaultRename;
   private final AECheckbox checkBox;
   @Nullable
   private String changedName;

   public SetProcessingPatternAmountScreen(Me2in1Screen<C> parentScreen, GenericStack currentStack, Consumer<GenericStack> setter) {
      super(parentScreen, "/screens/set_rename_pattern_amount.json");
      this.currentStack = currentStack;
      this.setter = setter;
      this.changedName = null;
      this.widgets.addButton("save", GuiText.Set.text(), this::confirm);
      ItemStack icon = AEItems.PROCESSING_PATTERN.stack();
      TabButton button = new TabButton(icon, Component.translatable("gtceu.gui.title_bar.back"), btn -> this.returnToParent());
      this.widgets.add("back", button);
      this.amount = this.widgets.addNumberEntryWidget("amountToStock", NumberEntryType.of(currentStack.what()));
      this.amount.setLongValue(currentStack.amount());
      this.amount.setMaxValue(this.getMaxAmount());
      this.amount.setTextFieldStyle(this.style.getWidget("amountToStockInput"));
      this.amount.setMinValue(0L);
      this.amount.setHideValidationIcon(true);
      this.amount.setOnConfirm(this::confirm);
      this.rename = this.widgets.addTextField("rename");
      if (currentStack.what() instanceof AEItemKey itemKey) {
         this.rename.setValue(itemKey.toStack().getHoverName().getString());
         this.rename.setResponder(this::setChangedName);
         this.rename.setTooltipMessage(List.of(Component.translatable("gui.expatternprovider.renamer")));
      }

      this.defaultRename = this.widgets.addTextField("defaultRename");
      this.defaultRename.setValue(Client.autoRenameName.equals("{}") ? "" : Client.autoRenameName);
      this.defaultRename.setResponder(text -> Client.autoRenameName = text);
      this.defaultRename.setTooltipMessage(List.of(Component.translatable("gtocore.ae.appeng.me2in1.save_default_rename_pattern")));
      this.checkBox = this.widgets
         .addCheckbox("autoEncodeRenamePatternCheckbox", Component.translatable("gtocore.ae.appeng.me2in1.auto_encode_rename_pattern"), this::onCheckBoxChanged);
      this.checkBox.setSelected(this.getParent().getMenu().isAutoEncodeRenaming());
      this.checkBox.setTooltip(Tooltip.create(Component.translatable("gtocore.ae.appeng.me2in1.auto_encode_rename_pattern.1")));
      this.addClientSideSlot(new ClientDisplaySlot(currentStack), SlotSemantics.MACHINE_OUTPUT);
   }

   private void onCheckBoxChanged() {
      this.getParent().getMenu().getEncoding().setAutoEncodeRenaming(this.checkBox.isSelected());
   }

   @Override
   public boolean mouseClicked(double xCoord, double yCoord, int btn) {
      if (btn == 1 && this.rename.isMouseOver(xCoord, yCoord)) {
         this.rename.setValue("");
      }

      return super.mouseClicked(xCoord, yCoord, btn);
   }

   @Override
   protected void init() {
      super.init();
      this.setSlotsHidden(SlotSemantics.TOOLBOX, true);
   }

   private void confirm() {
      this.amount.getLongValue().ifPresent(newAmount -> {
         long var3 = Longs.constrainToRange(newAmount, 0L, this.getMaxAmount());
         if (var3 <= 0L) {
            this.setter.accept(null);
         } else {
            this.setter.accept(this.getSetterStack(var3));
         }

         if (!Objects.equals(GTOConfig.INSTANCE.gamePlay.renamePatternDefaultString, Client.autoRenameName)) {
            GTOConfig.set("renamePatternDefaultString", Client.autoRenameName, new String[]{"gamePlay"});
         }

         this.returnToParent();
      });
   }

   private GenericStack getSetterStack(long amount) {
      if (this.changedName != null && this.currentStack.what() instanceof AEItemKey itemKey) {
         ItemStack stack = itemKey.toStack();
         if (this.changedName.isEmpty()) {
            stack.removeTagKey("display");
         } else {
            stack.setHoverName(Component.literal(this.changedName));
         }

         return new GenericStack(AEItemKey.of(stack), amount);
      } else {
         return new GenericStack(this.currentStack.what(), amount);
      }
   }

   private long getMaxAmount() {
      return Long.MAX_VALUE;
   }

   public void setChangedName(@Nullable String changedName) {
      this.changedName = changedName;
   }
}
