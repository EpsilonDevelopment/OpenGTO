package com.gtolib.api.registries;

import com.gregtechceu.gtceu.GTCEu;
import com.gto.registrate.AbstractRegistrate;
import com.gto.registrate.providers.DataGenContext;
import com.gto.registrate.providers.ProviderType;
import com.gto.registrate.providers.RegistrateItemModelProvider;
import com.gto.registrate.util.nullness.NonNullBiConsumer;
import com.gto.registrate.util.nullness.NonNullFunction;
import com.gtolib.api.item.IItem;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.NotNull;

public final class ItemBuilder<T extends Item, P> extends com.gto.registrate.builders.ItemBuilder<T, P> {
   private ItemBuilder(AbstractRegistrate<?> owner, P parent, String name, NonNullFunction<Properties, T> factory) {
      super(owner, parent, name, factory);
   }

   public static <T extends Item, P> ItemBuilder<T, P> c(AbstractRegistrate<?> owner, P parent, String name, NonNullFunction<Properties, T> factory) {
      return new ItemBuilder<T, P>(owner, parent, name, factory).defaultModel().defaultLang();
   }

   public ItemBuilder<T, P> toolTips(Component... component) {
      if (!GTCEu.isClientSide()) {
         return this;
      }

      Supplier<Component>[] array = new Supplier[component.length];

      for (int i = 0; i < component.length; i++) {
         Component c = component[i];
         array[i] = () -> c;
      }

      return (ItemBuilder<T, P>)this.onRegister(item -> ((IItem)item).gtolib$setToolTips(array));
   }

   public ItemBuilder<T, P> toolTips(Component component) {
      return !GTCEu.isClientSide() ? this : (ItemBuilder)this.onRegister(item -> ((IItem)item).gtolib$setToolTips(() -> component));
   }

   public ItemBuilder<T, P> toolTips(Supplier<Component> componentSupplier) {
      return !GTCEu.isClientSide() ? this : (ItemBuilder)this.onRegister(item -> ((IItem)item).gtolib$setToolTips(componentSupplier));
   }

   public ItemBuilder<T, P> toolTips(Supplier... componentSupplier) {
      return !GTCEu.isClientSide() ? this : (ItemBuilder)this.onRegister(item -> ((IItem)item).gtolib$setToolTips(componentSupplier));
   }

   @NotNull
   public ItemBuilder<T, P> defaultLang() {
      return (ItemBuilder<T, P>)this.lang(Item::getDescriptionId);
   }

   @NotNull
   public ItemBuilder<T, P> defaultModel() {
      return this.model((ctx, prov) -> prov.generated(ctx::getEntry));
   }

   @NotNull
   public ItemBuilder<T, P> model(@NotNull NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> cons) {
      return (ItemBuilder<T, P>)this.setData(ProviderType.ITEM_MODEL, cons);
   }
}
