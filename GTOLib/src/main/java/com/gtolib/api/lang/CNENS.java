package com.gtolib.api.lang;

import java.util.Arrays;

public record CNENS(String[] cns, String[] ens) {
   @Override
   public boolean equals(Object o) {
      return !(o instanceof CNENS cnens) ? false : Arrays.equals(cnens.ens, this.ens) && Arrays.equals(cnens.cns, this.cns);
   }

   public int length() {
      return this.ens.length;
   }
}
