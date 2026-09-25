package com.gtolib.data.wallet.transactionStrategy;

import com.gtolib.GTOCore;
import com.gtolib.data.wallet.PlayerWallet;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class LoanCollectionStrategy implements TransactionTypeStrategy {
   private final long time;
   private final String currencyType;
   private final long amount;

   public LoanCollectionStrategy(long time, String currencyType, long amount) {
      this.time = time;
      this.currencyType = currencyType;
      this.amount = amount;
   }

   @Override
   public String getType() {
      return "loan_collection";
   }

   @Override
   public long getExpireMinutes() {
      return this.time;
   }

   @Override
   public void cleanExpired(Long2LongOpenHashMap minuteAmountMap, long currentMinute) {
   }

   @Override
   public boolean needCallback() {
      return true;
   }

   @Override
   public void execute(PlayerWallet wallet) {
      long newAmount = wallet.adjustCurrency(this.currencyType, -this.amount);
      GTOCore.LOGGER
         .info("Player [{}] repays upon maturity: After deducting {} {}, current balance {}", wallet.getPlayerName(), this.amount, this.currencyType, newAmount);
   }

   @Override
   public Tag serializeParams() {
      CompoundTag tag = new CompoundTag();
      tag.putLong("time", this.time);
      tag.putString("currency_type", this.currencyType);
      tag.putLong("amount", this.amount);
      return tag;
   }

   @Override
   public TransactionTypeStrategy deserializeParams(Tag tag) {
      CompoundTag compoundTag = (CompoundTag)tag;
      long time = compoundTag.getLong("time");
      String currencyType = compoundTag.getString("currency_type");
      long amount = compoundTag.getLong("amount");
      return new LoanCollectionStrategy(time, currencyType, amount);
   }
}
