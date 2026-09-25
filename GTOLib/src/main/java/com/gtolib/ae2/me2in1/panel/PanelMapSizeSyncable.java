package com.gtolib.ae2.me2in1.panel;

import appeng.menu.guisync.PacketWritable;
import com.gtolib.api.ae2.me2in1.panel.PanelSizeMap;
import net.minecraft.network.FriendlyByteBuf;

public record PanelMapSizeSyncable(PanelSizeMap map) implements PacketWritable {
   public PanelMapSizeSyncable(FriendlyByteBuf buf) {
      this(new PanelSizeMap(buf));
   }

   @Override
   public void writeToPacket(FriendlyByteBuf data) {
      this.map.writeToPacket(data);
   }
}
