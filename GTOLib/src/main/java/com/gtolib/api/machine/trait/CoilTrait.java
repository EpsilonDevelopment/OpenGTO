package com.gtolib.api.machine.trait;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.ICoilMachine;
import com.gregtechceu.gtceu.api.pattern.Predicates.DataKey;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.block.CoilBlock.CoilType;
import com.gregtechceu.gtceu.common.data.GTRecipeDataKeys;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gtolib.api.machine.feature.multiblock.IMultiblockTraitHolder;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.IdleReason;
import java.util.List;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

public class CoilTrait extends MultiblockTrait {
   private ICoilType coilType = CoilType.CUPRONICKEL;
   private int temperature;
   private final boolean ebf;
   private final boolean check;

   public CoilTrait(ICoilMachine machine, boolean ebf, boolean check) {
      super((IMultiblockTraitHolder)machine);
      this.ebf = ebf;
      this.check = check;
   }

   @Override
   public GTRecipe modifyRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
      if (this.check && this.temperature < recipe.data.getInt(GTRecipeDataKeys.EBF_TEMP)) {
         IdleReason.INSUFFICIENT_TEMPERATURE.setReason(this.machine);
         return null;
      } else {
         return recipe;
      }
   }

   @Override
   public void customText(@NotNull List<Component> textList) {
      textList.add(
         Component.translatable(
            "gtceu.multiblock.blast_furnace.max_temperature",
            Component.translatable(FormattingUtil.formatNumbers(this.temperature) + "K").setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
         )
      );
   }

   @Override
   public void onStructureFormed() {
      if (this.getMachine().getMultiblockState().getMatchContext().get(DataKey.COIL_TYPE) instanceof ICoilType coil) {
         this.coilType = coil;
      }

      this.temperature = this.coilType.getCoilTemperature() + (this.ebf ? 100 * Math.max(0, this.getMachine().getTier() - 2) : 0);
   }

   public ElectricMultiblockMachine getMachine() {
      return (ElectricMultiblockMachine)this.machine;
   }

   @Override
   public void onStructureInvalid() {
      this.coilType = CoilType.CUPRONICKEL;
   }

   @Generated
   public ICoilType getCoilType() {
      return this.coilType;
   }

   @Generated
   public int getTemperature() {
      return this.temperature;
   }
}
