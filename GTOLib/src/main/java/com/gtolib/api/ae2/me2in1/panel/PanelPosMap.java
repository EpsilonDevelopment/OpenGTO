package com.gtolib.api.ae2.me2in1.panel;

import appeng.menu.guisync.PacketWritable;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.ae2.me2in1.panel.PanelMapPosSyncable;
import java.awt.Point;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class PanelPosMap implements PacketWritable {
   public Map<String, Point> slotsPosition;

   public PanelPosMap() {
      this.slotsPosition = new O2OOpenCacheHashMap<>();
   }

   public PanelPosMap(Map<String, Point> slotsPosition) {
      this.slotsPosition = slotsPosition;
   }

   public PanelPosMap(FriendlyByteBuf buf) {
      int size = buf.readVarInt();
      this.slotsPosition = new O2OOpenCacheHashMap<>(size);

      for (int i = 0; i < size; i++) {
         String key = buf.readUtf();
         int x = buf.readVarInt();
         int y = buf.readVarInt();
         this.slotsPosition.put(key, new Point(x, y));
      }
   }

   public void readFromNbt(CompoundTag nbt, String childTagName) {
      if (nbt.contains(childTagName, 10)) {
         CompoundTag slotsTag = nbt.getCompound(childTagName);
         this.slotsPosition = new O2OOpenCacheHashMap<>();

         for (String key : slotsTag.getAllKeys()) {
            CompoundTag posTag = slotsTag.getCompound(key);
            int x = posTag.getInt("x");
            int y = posTag.getInt("y");
            this.slotsPosition.put(key, new Point(x, y));
         }
      } else {
         this.slotsPosition = new O2OOpenCacheHashMap<>();
      }
   }

   public void writeToNbt(CompoundTag nbt, String childTagName) {
      CompoundTag slotsTag = new CompoundTag();

      for (Entry<String, Point> entry : this.slotsPosition.entrySet()) {
         CompoundTag posTag = new CompoundTag();
         posTag.putInt("x", entry.getValue().x);
         posTag.putInt("y", entry.getValue().y);
         slotsTag.put(entry.getKey(), posTag);
      }

      nbt.put(childTagName, slotsTag);
   }

   @Override
   public void writeToPacket(FriendlyByteBuf data) {
      data.writeVarInt(this.slotsPosition.size());

      for (Entry<String, Point> entry : this.slotsPosition.entrySet()) {
         data.writeUtf(entry.getKey());
         data.writeVarInt(entry.getValue().x);
         data.writeVarInt(entry.getValue().y);
      }
   }

   public void updatePanelPos(PanelPosMap.PanelPos pos) {
      this.slotsPosition.put(pos.name, new Point(pos.x, pos.y));
   }

   @Nullable
   public Point getPanelPos(String name) {
      return name != null && this.slotsPosition.containsKey(name) ? this.slotsPosition.get(name) : null;
   }

   public PanelMapPosSyncable syncable() {
      return new PanelMapPosSyncable(this);
   }

   public static class PanelPos {
      public String name;
      public int x;
      public int y;

      public PanelPos(String name, int x, int y) {
         this.name = name;
         this.x = x;
         this.y = y;
      }

      public PanelPos() {
         this("", 0, 0);
      }
   }
}
