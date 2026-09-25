package com.gtolib.ae2.crafting.reborn;

import appeng.api.stacks.AEKey;
import com.gtolib.ae2.crafting.utils.ErrorMessageUtils;
import java.util.List;
import net.minecraft.network.chat.Component;

public class CycleDetectedResult {
   private final Context context;
   public final List<List<AEKey>> cycles;

   public CycleDetectedResult(Context context, List<List<AEKey>> cycles) {
      this.cycles = cycles;
      this.context = context;
   }

   public void execute() {
      Component errorComponent = ErrorMessageUtils.buildCycleErrorMessage(this.cycles);
      this.context.getPlayer().ifPresent(player -> player.sendSystemMessage(errorComponent));
   }
}
