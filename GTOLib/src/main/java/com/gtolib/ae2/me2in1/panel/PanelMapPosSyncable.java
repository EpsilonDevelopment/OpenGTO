package com.gtolib.ae2.me2in1.panel;

import appeng.menu.guisync.PacketWritable;
import com.gtolib.api.ae2.me2in1.panel.PanelPosMap;
import net.minecraft.network.FriendlyByteBuf;

public record PanelMapPosSyncable(PanelPosMap map) implements PacketWritable {
   public PanelMapPosSyncable(FriendlyByteBuf buf) {
      this(new PanelPosMap(buf));
   }

   @Override
   public void writeToPacket(FriendlyByteBuf data) {
      this.map.writeToPacket(data);
   }
}
