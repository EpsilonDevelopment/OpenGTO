package com.gtolib.api.registries;

import com.gregtechceu.gtceu.GTCEu;
import com.gto.registrate.AbstractRegistrate;
import com.gto.registrate.util.nullness.NonNullFunction;
import com.gto.registrate.util.nullness.NonNullSupplier;
import com.gtolib.api.item.IItem;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.NotNull;

public final class BlockBuilder<T extends Block, P> extends com.gto.registrate.builders.BlockBuilder<T, P> {
   private BlockBuilder(
      AbstractRegistrate<?> owner, P parent, String name, NonNullFunction<Properties, T> factory, NonNullSupplier<Properties> initialProperties
   ) {
      super(owner, parent, name, factory, initialProperties);
   }

   @NotNull
   public static <T extends Block, P> BlockBuilder<T, P> c(
      @NotNull AbstractRegistrate<?> owner, @NotNull P parent, @NotNull String name, @NotNull NonNullFunction<Properties, T> factory
   ) {
      return new BlockBuilder<T, P>(owner, parent, name, factory, Properties::of).defaultBlockstate().defaultLoot().defaultLang();
   }

   public BlockBuilder<T, P> toolTips(Component... component) {
      if (!GTCEu.isClientSide()) {
         return this;
      }

      Supplier<Component>[] array = new Supplier[component.length];

      for (int i = 0; i < component.length; i++) {
         Component c = component[i];
         array[i] = () -> c;
      }

      return (BlockBuilder<T, P>)this.onRegister(block -> ((IItem)block.asItem()).gtolib$setToolTips(array));
   }

   public BlockBuilder<T, P> toolTips(Component component) {
      return !GTCEu.isClientSide() ? this : (BlockBuilder)this.onRegister(block -> ((IItem)block.asItem()).gtolib$setToolTips(() -> component));
   }

   public BlockBuilder<T, P> toolTips(Supplier<Component> componentSupplier) {
      return !GTCEu.isClientSide() ? this : (BlockBuilder)this.onRegister(block -> ((IItem)block.asItem()).gtolib$setToolTips(componentSupplier));
   }

   public BlockBuilder<T, P> toolTips(Supplier... componentSupplier) {
      return !GTCEu.isClientSide() ? this : (BlockBuilder)this.onRegister(block -> ((IItem)block.asItem()).gtolib$setToolTips(componentSupplier));
   }

   @NotNull
   public BlockBuilder<T, P> defaultBlockstate() {
      return (BlockBuilder<T, P>)this.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry()));
   }

   @NotNull
   public BlockBuilder<T, P> defaultLoot() {
      this.defaultLoot = true;
      return this;
   }

   @NotNull
   public BlockBuilder<T, P> defaultLang() {
      return (BlockBuilder<T, P>)this.lang(Block::getDescriptionId);
   }
}
