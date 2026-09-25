package com.gtolib.api.wireless;

import java.math.BigInteger;

public interface IWirelessContainer {
   String getUnit();

   BigInteger getStorage();

   BigInteger unrestrictedRemoveStorage(BigInteger var1);
}
