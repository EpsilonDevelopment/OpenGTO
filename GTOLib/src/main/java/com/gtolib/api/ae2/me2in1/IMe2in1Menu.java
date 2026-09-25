package com.gtolib.api.ae2.me2in1;

import appeng.api.util.IConfigurableObject;
import appeng.client.gui.widgets.ISortSource;
import appeng.helpers.IMenuCraftingPacket;
import appeng.menu.me.common.IMEInteractionHandler;
import com.gtolib.api.ae2.IPatterEncodingTermMenu;
import gto_ae.hooks.gui.menu.IRepoMenu;

public interface IMe2in1Menu extends IMEInteractionHandler, IMenuCraftingPacket, IPatterEncodingTermMenu, IConfigurableObject, ISortSource, IRepoMenu {
}
