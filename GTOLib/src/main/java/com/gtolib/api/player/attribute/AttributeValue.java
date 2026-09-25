package com.gtolib.api.player.attribute;

import com.gto.datasynclib.datastream.data.ListData;
import lombok.Generated;
import net.minecraft.network.FriendlyByteBuf;

public abstract class AttributeValue<T extends AttributeValue<T, D>, D extends AttributeDefinition<T, D>> {
   public final D definition;
   protected final Runnable dirtyCallback;
   protected boolean available;

   protected AttributeValue(D definition, Runnable dirtyCallback, boolean available) {
      this.definition = definition;
      this.dirtyCallback = dirtyCallback;
      this.available = available;
   }

   public void setAvailable(boolean available) {
      boolean changed = this.available != available;
      this.available = available;
      if (changed) {
         this.dirtyCallback.run();
      }
   }

   public final void copyFrom(AttributeValue<?, ?> other) {
      this.copy((T)other);
   }

   public abstract void reset();

   protected abstract void copy(T var1);

   public abstract void read(FriendlyByteBuf var1);

   public abstract void write(FriendlyByteBuf var1);

   public abstract void read(ListData var1, int var2);

   public abstract void write(ListData var1);

   @Generated
   public boolean isAvailable() {
      return this.available;
   }
}
