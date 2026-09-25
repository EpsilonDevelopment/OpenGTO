package com.gtolib.api.recipe;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.DataComponentKey;

public final class TierDataKey extends DataComponentKey<Integer> {
   public TierDataKey(String name) {
      super(name, DataSyncCodec.INT_CODEC);
   }
}
