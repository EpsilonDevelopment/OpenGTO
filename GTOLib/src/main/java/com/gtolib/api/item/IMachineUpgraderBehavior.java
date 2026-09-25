package com.gtolib.api.item;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public interface IMachineUpgraderBehavior extends IInteractionItem {
   default BlockState copyBlockStateProperties(BlockState originState, BlockState targetDefaultState) {
      BlockState state = targetDefaultState;

      for (Entry<Property<?>, Comparable<?>> sp : originState.getValues().entrySet()) {
         Property<? extends Comparable<?>> pt = (Property<? extends Comparable<?>>)sp.getKey();
         Comparable<?> va = sp.getValue();

         try {
            if (state.hasProperty(pt)) {
               state = state.setValue((Property)pt, (Comparable)va);
            }
         } catch (Exception var9) {
         }
      }

      return state;
   }

   default void replaceBlockEntityWithNBTHook(
      Level world, BlockPos pos, BlockEntity oldTile, BlockEntity newTile, BlockState newBlock, @Nullable Consumer<CompoundTag> nbtOperator
   ) {
      CompoundTag contents = oldTile.serializeNBT();
      world.removeBlockEntity(pos);
      world.removeBlock(pos, false);
      world.setBlock(pos, newBlock, 3);
      world.setBlockEntity(newTile);
      if (nbtOperator != null) {
         nbtOperator.accept(contents);
      }

      newTile.deserializeNBT(contents);
   }
}
