package com.gtolib.api.machine.feature;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.feature.multiblock.IDroneControlCenterMachine;
import com.gtolib.api.misc.Drone;
import java.util.function.Predicate;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IDroneInteractionMachine extends IIWirelessInteractor<IDroneControlCenterMachine>, IMachineFeature {
   @Override
   default Class<IDroneControlCenterMachine> getProviderClass() {
      return IDroneControlCenterMachine.class;
   }

   default boolean firstTestMachine(IDroneControlCenterMachine machine) {
      Level level = machine.getLevel();
      return level == null ? false : this.testMachine(machine) && machine.hasDrone(this.self().getPos(), d -> d.getCharge() > 0L);
   }

   default boolean testMachine(IDroneControlCenterMachine machine) {
      return machine.isActiveState();
   }

   @Nullable
   default Drone getFirstUsableDrone(Predicate<Drone> predicate) {
      IDroneControlCenterMachine centerMachine = this.getNetMachine();
      return centerMachine != null ? centerMachine.getFirstUsableDrone(this.self().getPos(), predicate) : null;
   }
}
