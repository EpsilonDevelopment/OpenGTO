package com.gtolib.ae2.me2in1;

import appeng.api.implementations.blockentities.IViewCellStorage;
import appeng.api.storage.ITerminalHost;
import appeng.util.inv.InternalInventoryHost;
import com.gtolib.api.ae2.me2in1.IExtendedPatternMenuHost;
import net.minecraft.world.level.Level;

public interface IMe2in1Host extends IExtendedPatternMenuHost, ITerminalHost, IViewCellStorage, InternalInventoryHost {
   void markForSave();

   Level getLevel();
}
