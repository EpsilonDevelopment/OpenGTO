package com.gtolib.emi;

import appeng.api.stacks.IdentityTag;
import java.util.function.Supplier;

public interface IEmiStack {
   default Supplier<IdentityTag> getUniqueNbt() {
      return null;
   }
}
