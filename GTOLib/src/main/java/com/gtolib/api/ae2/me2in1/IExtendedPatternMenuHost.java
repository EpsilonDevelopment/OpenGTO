package com.gtolib.api.ae2.me2in1;

import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;

public interface IExtendedPatternMenuHost extends ITerminalHost, IActionHost {
   ExtendedEncodingLogic getLogic();
}
