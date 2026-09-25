package com.gtolib.api.ae2.me2in1.panel;

import appeng.menu.guisync.PacketWritable;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.ae2.me2in1.panel.PanelMapSizeSyncable;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class PanelSizeMap implements PacketWritable {
   private Map<String, PanelSizeMap.PanelSize> panelSizes;

   public PanelSizeMap() {
      this.panelSizes = new O2OOpenCacheHashMap<>();
   }

   public PanelSizeMap(FriendlyByteBuf buf) {
      int size = buf.readVarInt();
      this.panelSizes = new O2OOpenCacheHashMap<>(size);

      for (int i = 0; i < size; i++) {
         String name = buf.readUtf();
         int rows = buf.readVarInt();
         int columns = buf.readVarInt();
         this.panelSizes.put(name, new PanelSizeMap.PanelSize(name, rows, columns));
      }
   }

   public void readFromNbt(CompoundTag nbt, String childTagName) {
      if (!nbt.contains(childTagName, 10)) {
         this.panelSizes = new O2OOpenCacheHashMap<>();
      } else {
         CompoundTag sizesTag = nbt.getCompound(childTagName);
         this.panelSizes = new O2OOpenCacheHashMap<>();

         for (String key : sizesTag.getAllKeys()) {
            CompoundTag sizeTag = sizesTag.getCompound(key);
            this.panelSizes.put(key, new PanelSizeMap.PanelSize(key, sizeTag.getInt("rows"), sizeTag.getInt("columns")));
         }
      }
   }

   public void writeToNbt(CompoundTag nbt, String childTagName) {
      CompoundTag sizesTag = new CompoundTag();

      for (Entry<String, PanelSizeMap.PanelSize> entry : this.panelSizes.entrySet()) {
         CompoundTag sizeTag = new CompoundTag();
         sizeTag.putInt("rows", entry.getValue().rows);
         sizeTag.putInt("columns", entry.getValue().columns);
         sizesTag.put(entry.getKey(), sizeTag);
      }

      nbt.put(childTagName, sizesTag);
   }

   @Override
   public void writeToPacket(FriendlyByteBuf data) {
      data.writeVarInt(this.panelSizes.size());

      for (Entry<String, PanelSizeMap.PanelSize> entry : this.panelSizes.entrySet()) {
         data.writeUtf(entry.getKey());
         data.writeVarInt(entry.getValue().rows);
         data.writeVarInt(entry.getValue().columns);
      }
   }

   public void updatePanelSize(PanelSizeMap.PanelSize size) {
      this.panelSizes.put(size.name, new PanelSizeMap.PanelSize(size.name, size.rows, size.columns));
   }

   @Nullable
   public PanelSizeMap.PanelSize getPanelSize(String name) {
      return name != null && this.panelSizes.containsKey(name) ? this.panelSizes.get(name) : null;
   }

   public PanelMapSizeSyncable syncable() {
      return new PanelMapSizeSyncable(this);
   }

   public static class PanelSize {
      public String name;
      public int rows;
      public int columns;

      public PanelSize(String name, int rows, int columns) {
         this.name = name;
         this.rows = rows;
         this.columns = columns;
      }

      public PanelSize() {
         this("", 0, 0);
      }
   }
}
