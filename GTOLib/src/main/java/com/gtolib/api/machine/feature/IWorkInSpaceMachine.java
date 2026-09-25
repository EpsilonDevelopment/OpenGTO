package com.gtolib.api.machine.feature;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gtolib.api.machine.IGTOMachineDefinition;
import com.gtolib.api.recipe.IdleReason;
import earth.terrarium.adastra.api.planets.PlanetApi;
import org.jetbrains.annotations.Nullable;

public interface IWorkInSpaceMachine extends IRecipeLogicMachine {
   @Nullable
   ISpaceWorkspaceMachine getWorkspaceProvider();

   void setWorkspaceProvider(@Nullable ISpaceWorkspaceMachine var1);

   default boolean canWorkInSpaceIndependently() {
      return this.self().getDefinition() instanceof IGTOMachineDefinition def && def.canWorkInSpaceIndependently();
   }

   default boolean canWorkInSpace() {
      ISpaceWorkspaceMachine provider = this.getWorkspaceProvider();
      return this.canWorkInSpaceIndependently() || provider != null && provider.isWorkspaceReady();
   }

   @Override
   default boolean checkConditions(RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
      if (PlanetApi.API.isSpace(this.self().getLevel()) && !this.canWorkInSpace()) {
         IdleReason.CANNOT_WORK_IN_SPACE.reason(this);
         return false;
      } else {
         return IRecipeLogicMachine.super.checkConditions(unit, recipe);
      }
   }
}
