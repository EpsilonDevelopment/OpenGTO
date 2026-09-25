package com.gtolib.api.ae2.crafting;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import com.gtolib.api.machine.impl.part.CraftingInterfacePartMachine;

public interface ICraftingCPUCluster {
   void setMachine(CraftingInterfacePartMachine var1);

   static ICraftingCPUCluster of(CraftingCPUCluster cluster) {
      return (ICraftingCPUCluster)(Object)cluster;
   }

   static CraftingCPUCluster create(CraftingInterfacePartMachine machine, MachineSource src, long storage, int accelerator) {
      CraftingCPUCluster cluster = new CraftingCPUCluster(machine.getPos(), machine.getPos());
      ICraftingCPUCluster cl = of(cluster);
      cl.setMachine(machine);
      cluster.setMachineSrc(src);
      cluster.setStorage(storage);
      cluster.setAccelerator(accelerator);
      return cluster;
   }
}
