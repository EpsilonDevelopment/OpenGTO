package com.gtolib.api.ae2.gui;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.client.gui.Icon;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.ButtonToolTips;
import com.gtolib.api.ae2.BlockingType;
import com.gtolib.api.ae2.GTOSettings;
import com.gtolib.api.ae2.ShiftTransferTo;
import gto_ae.client.gui.IconsExtended;
import gto_ae.core.localization.ExtendedLangs;
import net.minecraft.network.chat.Component;

public class GTOButtonAppearance {
   public static void registerButtons() {
      SettingToggleButton.registerApp(
         Icon.CONDENSER_OUTPUT_TRASH, Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH, ButtonToolTips.CondenserOutput, ButtonToolTips.Trash
      );
      SettingToggleButton.registerApp(
         Icon.BLOCKING_MODE_NO,
         GTOSettings.BLOCKING_TYPE,
         BlockingType.NONE,
         ButtonToolTips.InterfaceBlockingMode,
         Component.translatable("gui.tooltips.ae2.NonBlocking")
      );
      SettingToggleButton.registerApp(
         IconsExtended.BLOCKING_MODE_ALL,
         GTOSettings.BLOCKING_TYPE,
         BlockingType.ALL,
         ButtonToolTips.InterfaceBlockingMode,
         Component.translatable("gtocore.pattern.blocking_mode")
      );
      SettingToggleButton.registerApp(
         IconsExtended.BLOCKING_MODE_CONTAIN,
         GTOSettings.BLOCKING_TYPE,
         BlockingType.CONTAIN,
         ButtonToolTips.InterfaceBlockingMode,
         Component.translatable("gui.tooltips.ae2.Blocking")
      );
      SettingToggleButton.registerApp(
         IconsExtended.BLOCKING_MODE_NON_CONTAIN,
         GTOSettings.BLOCKING_TYPE,
         BlockingType.NON_CONTAIN,
         ButtonToolTips.InterfaceBlockingMode,
         Component.translatable("gtocore.pattern.blocking_reverse")
      );
      SettingToggleButton.registerApp(
         IconsExtended.BLOCKING_MODE_PARALLEL,
         GTOSettings.BLOCKING_TYPE,
         BlockingType.PARALLEL,
         ButtonToolTips.InterfaceBlockingMode,
         Component.translatable("gtocore.pattern.blocking_parallel")
      );
      SettingToggleButton.registerApp(
         Icon.ARROW_DOWN,
         GTOSettings.ME2IN1_SHIFT_TRANSFER_TO,
         ShiftTransferTo.INVENTORY_OR_BUFFER,
         ExtendedLangs.Me2In1ShiftTransferTo,
         ExtendedLangs.Me2In1ShiftTransferToInventoryOrBuffer
      );
      SettingToggleButton.registerApp(
         Icon.ARROW_RIGHT,
         GTOSettings.ME2IN1_SHIFT_TRANSFER_TO,
         ShiftTransferTo.CURRENTLY_VISIBLE_ACCESSOR,
         ExtendedLangs.Me2In1ShiftTransferTo,
         ExtendedLangs.Me2In1ShiftTransferToAccessor
      );
   }
}
