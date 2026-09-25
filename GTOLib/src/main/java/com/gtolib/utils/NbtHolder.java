package com.gtolib.utils;

import appeng.api.stacks.IdentityTag;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;

public final class NbtHolder implements Supplier<IdentityTag> {
   private static final NbtHolder EMPTY = new NbtHolder(() -> IdentityTag.EMPTY);
   private IdentityTag o;
   private boolean init;
   private Supplier<IdentityTag> s;

   public static Supplier<IdentityTag> of(CompoundTag tag) {
      return tag != null && !tag.tags.isEmpty() ? new NbtHolder(() -> IdentityTag.of(tag, false)) : EMPTY;
   }

   private NbtHolder(Supplier<IdentityTag> supplier) {
      this.s = supplier;
   }

   public IdentityTag get() {
      if (this.init) {
         return this.o;
      }

      this.o = this.s.get();
      this.s = null;
      this.init = true;
      return this.o;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && this.getClass() == o.getClass() ? this.get() == ((NbtHolder)o).get() : false;
      }
   }

   @Override
   public int hashCode() {
      return this.get().hashCode();
   }
}
