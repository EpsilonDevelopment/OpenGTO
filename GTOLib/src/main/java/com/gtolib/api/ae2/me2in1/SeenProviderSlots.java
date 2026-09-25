package com.gtolib.api.ae2.me2in1;

import appeng.client.gui.me.patternaccess.PatternSlot;
import appeng.menu.guisync.PacketWritable;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.network.FriendlyByteBuf;

public class SeenProviderSlots implements PacketWritable {
   private List<Integer> machineSlotIndex;
   private List<Long> machineSlotServerId;

   public SeenProviderSlots() {
      this.machineSlotIndex = new ArrayList<>();
      this.machineSlotServerId = new ArrayList<>();
   }

   public SeenProviderSlots(FriendlyByteBuf data) {
      int length = data.readInt();
      this.machineSlotIndex = new ArrayList<>(length);
      this.machineSlotServerId = new ArrayList<>(length);

      for (int i = 0; i < length; i++) {
         this.machineSlotIndex.add(data.readInt());
         this.machineSlotServerId.add(data.readLong());
      }
   }

   public void add(PatternSlot patternSlot) {
      this.machineSlotIndex.add(patternSlot.getSlotIndex());
      this.machineSlotServerId.add(patternSlot.getMachineInv().getServerId());
   }

   public void combine(SeenProviderSlots seenProviderSlots) {
      this.machineSlotIndex.addAll(seenProviderSlots.getMachineSlotIndex());
      this.machineSlotServerId.addAll(seenProviderSlots.getMachineSlotServerId());
   }

   @Override
   public void writeToPacket(FriendlyByteBuf data) {
      int length = this.machineSlotIndex.size();
      data.writeInt(length);

      for (int i = 0; i < length; i++) {
         data.writeInt(this.machineSlotIndex.get(i));
         data.writeLong(this.machineSlotServerId.get(i));
      }
   }

   @Generated
   public void setMachineSlotIndex(List<Integer> machineSlotIndex) {
      this.machineSlotIndex = machineSlotIndex;
   }

   @Generated
   public void setMachineSlotServerId(List<Long> machineSlotServerId) {
      this.machineSlotServerId = machineSlotServerId;
   }

   @Generated
   public List<Integer> getMachineSlotIndex() {
      return this.machineSlotIndex;
   }

   @Generated
   public List<Long> getMachineSlotServerId() {
      return this.machineSlotServerId;
   }
}
