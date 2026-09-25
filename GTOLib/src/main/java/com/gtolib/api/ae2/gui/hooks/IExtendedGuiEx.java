package com.gtolib.api.ae2.gui.hooks;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.client.gui.me.patternaccess.PatternContainerRecord;
import appeng.client.gui.widgets.AETextField;
import com.glodblock.github.extendedae.client.button.HighlightButton;
import com.glodblock.github.extendedae.client.gui.GuiExPatternTerminal.PatternProviderInfo;
import com.google.common.collect.HashMultimap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

public interface IExtendedGuiEx {
   default AETextField gto$getSearchProviderField() {
      return null;
   }

   default void gto$refreshSearch() {
   }

   default void gto$resetExPatternTerminalScrollbar() {
   }

   default HashMultimap<PatternContainerGroup, PatternContainerRecord> gto$getByGroup() {
      return null;
   }

   default HashMap<Long, PatternContainerRecord> gto$getById() {
      return null;
   }

   default HashMap<Integer, HighlightButton> gto$getHighlisghtsButtons() {
      return null;
   }

   default AETextField gto$searchOutField() {
      return null;
   }

   default AETextField gto$searchInField() {
      return null;
   }

   default Set<ItemStack> gto$matchedStack() {
      return null;
   }

   default Set<PatternContainerRecord> gto$matchedProvider() {
      return null;
   }

   default Map<String, Set<Object>> gto$cachedSearches() {
      return null;
   }

   default HashMap<Long, PatternProviderInfo> gto$infoMap() {
      return null;
   }

   default boolean gto$itemStackMatchesSearchTerm(ItemStack itemStack, List<String> searchTerm, boolean checkOut) {
      return false;
   }
}
