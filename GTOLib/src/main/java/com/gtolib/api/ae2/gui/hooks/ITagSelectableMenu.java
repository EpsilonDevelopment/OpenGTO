package com.gtolib.api.ae2.gui.hooks;

import appeng.api.stacks.AEKey;
import appeng.menu.slot.FakeSlot;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Unique;

public interface ITagSelectableMenu {
   @Unique
   FakeSlot gtolib$getBlacklistSlot();

   @Unique
   FakeSlot gtolib$getWhitelistSlot();

   @Unique
   void gtolib$setWhitelistChanged(Consumer<AEKey> var1);

   @Unique
   void gtolib$setBlacklistChanged(Consumer<AEKey> var1);
}
