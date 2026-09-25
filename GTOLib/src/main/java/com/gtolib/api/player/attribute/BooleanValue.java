package com.gtolib.api.player.attribute;

import com.gto.datasynclib.datastream.data.ListData;
import net.minecraft.network.FriendlyByteBuf;

public final class BooleanValue extends AttributeValue<BooleanValue, BooleanAttribute> {
   private boolean current;

   public BooleanValue(BooleanAttribute attribute, Runnable dirtyCallback, boolean available, boolean current) {
      super(attribute, dirtyCallback, available);
      this.current = current;
   }

   public boolean getCurrent() {
      return this.current;
   }

   public void setCurrent(boolean current) {
      boolean changed = this.current != current;
      this.current = current;
      if (changed) {
         this.dirtyCallback.run();
      }
   }

   private void read(boolean available, boolean current) {
      boolean changed = this.available != available || this.current != current;
      this.available = available;
      this.current = current;
      if (changed) {
         this.dirtyCallback.run();
      }
   }

   @Override
   public void reset() {
      this.read(this.definition.defaultAvailable, this.definition.defaultCurrent);
   }

   public void copy(BooleanValue other) {
      this.read(other.available, other.current);
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      this.available = buf.readBoolean();
      this.current = buf.readBoolean();
   }

   @Override
   public void write(FriendlyByteBuf buf) {
      buf.writeBoolean(this.available);
      buf.writeBoolean(this.current);
   }

   @Override
   public void read(ListData tag, int dataVersion) {
      this.read(this.available, tag.getBoolean(0));
   }

   @Override
   public void write(ListData tag) {
      tag.addBoolean(this.current);
   }
}
