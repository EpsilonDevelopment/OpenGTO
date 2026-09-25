package com.gtolib.api.player.attribute;

import com.gto.datasynclib.datastream.data.ListData;
import lombok.Generated;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public final class NumericValue extends AttributeValue<NumericValue, NumericAttribute<?>> {
   private float min;
   private float max;
   private float current;

   public NumericValue(NumericAttribute<?> attribute, Runnable dirtyCallback, boolean available, float min, float max, float current) {
      super(attribute, dirtyCallback, available);
      this.reset(available, min, max, current);
   }

   @Override
   public void setAvailable(boolean available) {
      boolean previousAvailable = this.available;
      float previousCurrent = this.current;
      this.available = available;
      if (!available) {
         this.current = 0.0F;
      } else {
         this.current = Mth.clamp(this.current, this.min, this.max);
      }

      if (previousAvailable != this.available || Float.compare(previousCurrent, this.current) != 0) {
         this.dirtyCallback.run();
      }
   }

   public void setRange(float min, float max) {
      float previousMin = this.min;
      float previousMax = this.max;
      float previousCurrent = this.current;
      this.min = Math.min(min, max);
      this.max = Math.max(min, max);
      this.current = Mth.clamp(this.current, this.min, this.max);
      if (Float.compare(previousMin, this.min) != 0 || Float.compare(previousMax, this.max) != 0 || Float.compare(previousCurrent, this.current) != 0) {
         this.dirtyCallback.run();
      }
   }

   public float getCurrentFloat() {
      return this.current;
   }

   public int getCurrentInt() {
      return Mth.floor(this.current);
   }

   public NumericValue setCurrent(float current) {
      float nextCurrent = Mth.clamp(current, this.min, this.max);
      if (Float.compare(this.current, nextCurrent) != 0) {
         this.current = nextCurrent;
         this.dirtyCallback.run();
      }

      return this;
   }

   private void read(boolean available, float min, float max, float current) {
      boolean previousAvailable = this.available;
      float previousMin = this.min;
      float previousMax = this.max;
      float previousCurrent = this.current;
      this.available = available;
      this.min = Math.min(min, max);
      this.max = Math.max(min, max);
      this.current = Mth.clamp(current, this.min, this.max);
      if (!available) {
         this.current = 0.0F;
      }

      if (previousAvailable != this.available
         || Float.compare(previousMin, this.min) != 0
         || Float.compare(previousMax, this.max) != 0
         || Float.compare(previousCurrent, this.current) != 0) {
         this.dirtyCallback.run();
      }
   }

   private void reset(boolean available, float min, float max, float current) {
      this.read(available, min, max, current);
   }

   @Override
   public void reset() {
      this.read(this.definition.defaultAvailable, this.definition.defaultMin, this.definition.defaultMax, this.definition.defaultCurrent);
   }

   public void copy(NumericValue other) {
      this.read(other.available, other.min, other.max, other.current);
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      this.available = buf.readBoolean();
      this.max = buf.readFloat();
      this.min = buf.readFloat();
      this.current = buf.readFloat();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBoolean(this.available);
      buf.writeFloat(this.max);
      buf.writeFloat(this.min);
      buf.writeFloat(this.current);
   }

   @Override
   public void read(ListData tag, int dataVersion) {
      this.read(this.available, tag.getFloat(0), tag.getFloat(1), tag.getFloat(2));
   }

   @Override
   public void write(ListData tag) {
      tag.addFloat(this.max);
      tag.addFloat(this.min);
      tag.addFloat(this.current);
   }

   @Generated
   public float getMin() {
      return this.min;
   }

   @Generated
   public float getMax() {
      return this.max;
   }
}
