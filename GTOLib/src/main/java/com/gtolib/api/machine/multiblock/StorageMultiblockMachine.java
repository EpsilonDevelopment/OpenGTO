package com.gtolib.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import lombok.Generated;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StorageMultiblockMachine extends ElectricMultiblockMachine implements IStorageMultiblock {
   @SyncToClient
   @SaveToDisk
   protected final NotifiableItemStackHandler machineStorage;
   private final int limit;

   public StorageMultiblockMachine(MetaMachineBlockEntity holder, int limit, @Nullable Predicate<ItemStack> filter) {
      super(holder);
      this.limit = limit;
      this.machineStorage = this.createMachineStorage(filter);
   }

   @Override
   public int getSlotLimit() {
      return this.limit;
   }

   @Override
   public Widget createUIWidget() {
      return this.createUIWidget(super.createUIWidget());
   }

   @Generated
   @Override
   public NotifiableItemStackHandler getMachineStorage() {
      return this.machineStorage;
   }
}
