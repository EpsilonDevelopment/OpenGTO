package com.gtolib.api.machine.mana.feature;

import com.google.common.base.Predicates;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gtolib.api.capability.IManaContainer;
import com.gtolib.utils.MathUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.common.block.BotaniaBlocks;

public interface IManaMachine extends IManaContainerMachine, ManaCollector, IMachineFeature, SparkAttachable {
   @Override
   default Level getManaReceiverLevel() {
      return this.self().getLevel();
   }

   @Override
   default BlockPos getManaReceiverPos() {
      return this.self().getPos();
   }

   @Override
   default void onClientDisplayTick() {
   }

   @Override
   default float getManaYieldMultiplier(ManaBurst burst) {
      return 1.0F;
   }

   @Override
   default int getMaxMana() {
      return MathUtil.saturatedCast(this.getManaContainer().getMaxMana());
   }

   @Override
   default int getCurrentMana() {
      return MathUtil.saturatedCast(this.getManaContainer().getCurrentMana());
   }

   @Override
   default boolean isFull() {
      IManaContainer c = this.getManaContainer();
      return c.getMaxMana() <= c.getCurrentMana();
   }

   @Override
   default void receiveMana(int mana) {
      if (mana > 0) {
         this.getManaContainer().addManaUnrestricted(mana, false);
      } else if (mana < 0) {
         this.getManaContainer().removeManaUnrestricted(-mana, false);
      }
   }

   @Override
   default boolean canAttachSpark(ItemStack stack) {
      return true;
   }

   @Override
   default int getAvailableSpaceForMana() {
      Level level = this.self().getLevel();
      if (level == null) {
         return 0;
      } else {
         int space = Math.max(0, MathUtil.saturatedCast(this.getManaContainer().getMaxMana() - this.getManaContainer().getCurrentMana()));
         if (space > 0) {
            return space;
         } else {
            return level.getBlockState(this.self().getPos().below()).is(BotaniaBlocks.manaVoid) ? this.getMaxMana() : 0;
         }
      }
   }

   @Override
   default ManaSpark getAttachedSpark() {
      Level level = this.self().getLevel();
      if (level == null) {
         return null;
      } else {
         List<Entity> sparks = level.getEntitiesOfClass(
            Entity.class, new AABB(this.self().getPos().above(), this.self().getPos().above().offset(1, 1, 1)), Predicates.instanceOf(ManaSpark.class)
         );
         if (sparks.size() == 1) {
            Entity e = sparks.getFirst();
            return (ManaSpark)e;
         } else {
            return null;
         }
      }
   }

   @Override
   default boolean areIncomingTranfersDone() {
      return false;
   }
}
