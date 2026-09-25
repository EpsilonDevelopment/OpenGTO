package com.gtolib.api.ae2.gui.hooks;

public interface IAEBaseScreenLifecycle {
   default void gtolib$initBeforePositionSlot() {
   }

   default void gtolib$initBeforeWidgetsInitialized() {
   }

   default void gtolib$initAfterWidgetsInitialized() {
   }
}
