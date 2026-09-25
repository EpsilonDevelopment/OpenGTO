package com.gtolib.api.emi.stack;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.gtolib.api.ae2.stacks.TagPrefixKey;
import com.gtolib.api.ae2.stacks.TagPrefixKeyType;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EmiTagPrefixUtils {
   @OnlyIn(Dist.CLIENT)
   public static EmiStack toEmiStack(TagPrefixKey key, long amount) {
      EmiTagprefixStack emiStack = key.isFluidKey() ? new EmiTagprefixStack(key.getStorageKey()) : new EmiTagprefixStack(key.getPrefix());
      emiStack.setAmount(amount);
      return emiStack;
   }

   @OnlyIn(Dist.CLIENT)
   public static GenericStack toGenericStack(EmiTagprefixStack prefix) {
      long amount = prefix.getAmount();
      AEKey key = prefix.isFluidKey() ? TagPrefixKeyType.fluidMap.getCache(prefix.getStorageKey()) : TagPrefixKeyType.map.getCache(prefix.getTagPrefix());
      return new GenericStack(key, amount);
   }
}
