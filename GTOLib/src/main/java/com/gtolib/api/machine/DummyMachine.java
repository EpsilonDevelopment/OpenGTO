package com.gtolib.api.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class DummyMachine extends MetaMachine {
   public String id = "";
   public int temp;
   public int manat;
   public long eut;
   public int duration;
   public int circuit;
   public final GTRecipeType recipeType;
   public final CustomItemStackHandler importItems;
   public final CustomItemStackHandler exportItems;
   public final NotifiableFluidTank importFluids;
   public final NotifiableFluidTank exportFluids;

   private DummyMachine(MetaMachineBlockEntity holder, GTRecipeType type) {
      super(holder);
      this.recipeType = type;
      this.importItems = this.createImportItemHandler();
      this.exportItems = this.createExportItemHandler();
      this.importFluids = this.createImportFluidHandler();
      this.exportFluids = this.createExportFluidHandler();
   }

   public void sett(String s) {
      this.temp = Integer.parseInt(s);
   }

   public void setMANAt(String s) {
      this.manat = Integer.parseInt(s);
   }

   public void setEUt(String s) {
      this.eut = Long.parseLong(s);
   }

   public void setDuration(String s) {
      this.duration = Integer.parseInt(s);
   }

   public void setCircuit(String s) {
      this.circuit = Integer.parseInt(s);
   }

   private CustomItemStackHandler createImportItemHandler() {
      return new CustomItemStackHandler(this.recipeType.getMaxInputs(ItemRecipeInfo.INSTANCE));
   }

   private CustomItemStackHandler createExportItemHandler() {
      return new CustomItemStackHandler(this.recipeType.getMaxOutputs(ItemRecipeInfo.INSTANCE));
   }

   private NotifiableFluidTank createImportFluidHandler() {
      return new NotifiableFluidTank(this, this.recipeType.getMaxInputs(FluidRecipeInfo.INSTANCE), Integer.MAX_VALUE, IO.BOTH);
   }

   private NotifiableFluidTank createExportFluidHandler() {
      return new NotifiableFluidTank(this, this.recipeType.getMaxOutputs(FluidRecipeInfo.INSTANCE), Integer.MAX_VALUE, IO.BOTH);
   }

   public static DummyMachine createDummyMachine(MetaMachine metaMachine) {
      return createDummyMachine(
         metaMachine.holder.getHolder().getType(), metaMachine.getPos(), metaMachine.getBlockState(), ((IRecipeLogicMachine)metaMachine).getRecipeType()
      );
   }

   public static DummyMachine createDummyMachine(BlockEntityType<?> type, BlockPos pos, BlockState blockState, GTRecipeType recipeType) {
      return new DummyMachine(MetaMachineBlockEntity.createBlockEntity(type, pos, blockState), recipeType);
   }

   @Generated
   public void setId(String id) {
      this.id = id;
   }
}
