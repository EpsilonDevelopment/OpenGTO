package com.gtolib.data.wallet.transactionStrategy;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.nbt.Tag;

public final class TransactionStrategyFactory {
   private static final Map<String, Supplier<TransactionTypeStrategy>> STRATEGY_REGISTRY = new O2OOpenCacheHashMap<>();

   public static TransactionTypeStrategy createStrategy(String type, Tag paramsTag) {
      Supplier<TransactionTypeStrategy> strategySupplier = STRATEGY_REGISTRY.get(type);
      if (strategySupplier == null) {
         throw new IllegalArgumentException("Unregistered trading strategy type: " + type + ", please check if it is registered in STRATEGY_REGISTRY");
      }

      try {
         TransactionTypeStrategy baseStrategy = strategySupplier.get();
         return baseStrategy.deserializeParams(paramsTag);
      } catch (Exception e) {
         throw new IllegalArgumentException("Deserialization of strategy parameters failed, type: " + type, e);
      }
   }

   private TransactionStrategyFactory() {
   }

   static {
      STRATEGY_REGISTRY.put("none", NoneStrategy::new);
      STRATEGY_REGISTRY.put("timing_compression", () -> new TimingCompressionStrategy(0L));
      STRATEGY_REGISTRY.put("scheduled_deletion", () -> new ScheduledDeletionStrategy(0L));
      STRATEGY_REGISTRY.put("loan_collection", () -> new LoanCollectionStrategy(0L, "", 0L));
   }
}
