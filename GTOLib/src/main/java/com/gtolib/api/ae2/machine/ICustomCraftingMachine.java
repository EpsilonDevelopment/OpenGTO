package com.gtolib.api.ae2.machine;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderTarget;
import com.gto.datasynclib.util.holder.BooleanHolder;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.gtolib.api.ae2.IPatternProviderLogic;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.core.Direction;

public interface ICustomCraftingMachine {
   boolean customPush();

   IPatternProviderLogic.PushResult pushPattern(
      IPatternProviderLogic var1,
      IActionSource var2,
      BooleanHolder var3,
      ICustomCraftingMachine.Operate var4,
      Set<AEKey> var5,
      IPatternDetails var6,
      ObjHolder<KeyCounter[]> var7,
      Supplier<IPatternProviderLogic.PushResult> var8,
      BooleanSupplier var9,
      Direction var10,
      Direction var11
   );

   @FunctionalInterface
   interface Operate {
      IPatternProviderLogic.PushResult pushTarget(
         IPatternDetails var1,
         ObjHolder<KeyCounter[]> var2,
         Supplier<IPatternProviderLogic.PushResult> var3,
         BooleanSupplier var4,
         Direction var5,
         PatternProviderTarget var6,
         boolean var7
      );
   }
}
