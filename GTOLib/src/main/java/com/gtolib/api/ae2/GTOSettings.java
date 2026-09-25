package com.gtolib.api.ae2;

import appeng.api.config.Setting;
import appeng.api.config.Settings;

public final class GTOSettings {
   public static final Setting<BlockingType> BLOCKING_TYPE = Settings.register(
      "blocking_type", BlockingType.NONE, BlockingType.ALL, BlockingType.CONTAIN, BlockingType.NON_CONTAIN, BlockingType.PARALLEL
   );
   public static final Setting<ShiftTransferTo> ME2IN1_SHIFT_TRANSFER_TO = Settings.register(
      "me2in1_shift_transfer_to", ShiftTransferTo.INVENTORY_OR_BUFFER, ShiftTransferTo.CURRENTLY_VISIBLE_ACCESSOR
   );
}
