package com.gtolib.api.capability;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gtolib.mc.ILevel;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import java.util.Set;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IIWirelessInteractor<T> {
   @Nullable
   Level getLevel();

   T getNetMachineCache();

   void setNetMachineCache(T var1);

   Class<T> getProviderClass();

   boolean testMachine(T var1);

   boolean firstTestMachine(T var1);

   default void removeNetMachineCache() {
      this.setNetMachineCache(null);
   }

   default Level getTargetLevel() {
      return this.getLevel();
   }

   @Nullable
   default T getNetMachine() {
      if (this.getNetMachineCache() == null) {
         Level level = this.getTargetLevel();
         if (level != null) {
            Set<T> set = getMachineNet(level, this.getProviderClass());
            if (set.isEmpty()) {
               return null;
            }

            for (T machine : set) {
               MetaMachine m = (MetaMachine)machine;
               if (!m.isInValid() && this.firstTestMachine(machine)) {
                  this.setNetMachineCache(machine);
                  return machine;
               }
            }
         }
      }

      T machine = this.getNetMachineCache();
      MetaMachine m = (MetaMachine)machine;
      if (machine != null) {
         if (!m.isInValid() && this.testMachine(machine)) {
            return machine;
         }

         this.removeNetMachineCache();
      }

      return null;
   }

   static <T> ReferenceSet<T> getMachineNet(Level level, Class<T> clazz) {
      return ((ILevel)level).gtolib$getMachineNet().getOrDefault(clazz, ReferenceSets.emptySet());
   }

   static void addToNet(@NotNull Object m) {
      addToNet(m, m.getClass());
   }

   static void addToNet(@NotNull Object m, Class<?> clazz) {
      IIWirelessInteractor.IWirelessProvider machine = (IIWirelessInteractor.IWirelessProvider)m;
      if (!machine.isRemote()) {
         Level level = machine.getLevel();
         if (level != null) {
            ((ILevel)level).gtolib$getMachineNet().computeIfAbsent(clazz, k -> new ReferenceOpenHashSet()).add(m);
         }
      }
   }

   static void removeFromNet(@NotNull Object m) {
      removeFromNet(m, m.getClass());
   }

   static void removeFromNet(@NotNull Object m, Class<?> clazz) {
      IIWirelessInteractor.IWirelessProvider machine = (IIWirelessInteractor.IWirelessProvider)m;
      if (!machine.isRemote()) {
         Level level = machine.getLevel();
         if (level != null) {
            ReferenceSet machines = ((ILevel)level).gtolib$getMachineNet().get(clazz);
            if (machines != null) {
               machines.remove(m);
            }
         }
      }
   }

   interface IWirelessProvider {
      boolean isInValid();

      boolean isRemote();

      Level getLevel();

      MetaMachine self();
   }
}
