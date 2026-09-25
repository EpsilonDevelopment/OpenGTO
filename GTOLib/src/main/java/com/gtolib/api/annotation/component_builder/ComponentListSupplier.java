package com.gtolib.api.annotation.component_builder;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

public class ComponentListSupplier implements Supplier<List<Component>> {
   private final Supplier<List<Component>> supplier;

   ComponentListSupplier(Supplier<List<Component>> supplier) {
      this.supplier = supplier;
   }

   public List<Component> get() {
      return this.supplier.get();
   }

   public Component[] getArray() {
      return this.get().toArray(new Component[0]);
   }
}
