package com.gtolib.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gtolib.api.machine.trait.MultiblockTrait;
import java.util.List;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public interface IMultiblockTraitHolder extends IMultiController {
   List<MultiblockTrait> getMultiblockTraits();

   default void customText(@NotNull List<Component> textList) {
      this.getMultiblockTraits().forEach(trait -> trait.customText(textList));
   }
}
