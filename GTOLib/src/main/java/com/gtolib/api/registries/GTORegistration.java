package com.gtolib.api.registries;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gto.registrate.Registrate;
import com.gto.registrate.util.nullness.NonNullFunction;
import com.gtolib.api.machine.BasicMachineDefinition;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.data.event.GatherDataEvent;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

public final class GTORegistration extends GTRegistrate {
   public static final GTORegistration GTO = new GTORegistration(0);
   public static final GTORegistration GTMT = new GTORegistration(1);
   public static final GTORegistration GTM = new GTORegistration(2);
   static final Pattern PATTERN = Pattern.compile("\\.");
   public final boolean gtm;
   public final boolean gtmt;

   @Override
   protected void onData(GatherDataEvent event) {
      if (!this.gtmt) {
         super.onData(event);
      }
   }

   private GTORegistration(int id) {
      super(id == 0 ? "gtocore" : (id == 1 ? "gtmthings" : "gtceu"));
      this.gtm = id == 2;
      this.gtmt = id == 1;
   }

   @NotNull
   public GTOMachineBuilder machine(
      @NotNull String name,
      @NotNull Function<MetaMachineBlockEntity, MetaMachine> metaMachine,
      TriFunction<BlockEntityType<?>, BlockPos, BlockState, MetaMachineBlockEntity> blockEntityFactory
   ) {
      return new GTOMachineBuilder(
         this, name, BasicMachineDefinition::createDefinition, metaMachine, MetaMachineBlock::new, MetaMachineItem::new, blockEntityFactory
      );
   }

   @NotNull
   public GTOMachineBuilder machine(@NotNull String name, @NotNull Function<MetaMachineBlockEntity, MetaMachine> metaMachine) {
      return new GTOMachineBuilder(
         this,
         name,
         BasicMachineDefinition::createDefinition,
         metaMachine,
         MetaMachineBlock::new,
         MetaMachineItem::new,
         MetaMachineBlockEntity::createBlockEntity
      );
   }

   @NotNull
   public MultiblockBuilder multiblock(@NotNull String name, @NotNull Function<MetaMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine) {
      return new MultiblockBuilder(this, name, metaMachine, MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
   }

   @NotNull
   public <T extends Block> BlockBuilder<T, Registrate> block(@NotNull String name, @NotNull NonNullFunction<Properties, T> factory) {
      return this.block(this.self(), name, factory);
   }

   @NotNull
   public <T extends Block, P> BlockBuilder<T, P> block(@NotNull P parent, @NotNull String name, @NotNull NonNullFunction<Properties, T> factory) {
      return BlockBuilder.c(this, parent, name, factory);
   }

   @NotNull
   public <T extends Item> ItemBuilder<T, Registrate> item(@NotNull String name, @NotNull NonNullFunction<Item.Properties, T> factory) {
      return (ItemBuilder<T, Registrate>)super.item(name, factory).lang(FormattingUtil.toEnglishName(PATTERN.matcher(name).replaceAll("_")));
   }

   @NotNull
   public <T extends Item, P> ItemBuilder<T, P> item(@NotNull P parent, @NotNull String name, @NotNull NonNullFunction<Item.Properties, T> factory) {
      return ItemBuilder.c(this, parent, name, factory);
   }

   static {
      GTO.defaultCreativeTab((ResourceKey<CreativeModeTab>)null);
      GTMT.defaultCreativeTab((ResourceKey<CreativeModeTab>)null);
      GTM.defaultCreativeTab((ResourceKey<CreativeModeTab>)null);
   }
}
