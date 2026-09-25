package com.gtolib.api.machine.feature;

import appeng.api.networking.security.IActionSource;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;

public interface IMEPartMachine extends IGridConnectedMachine {
   IActionSource getActionSource();
}
