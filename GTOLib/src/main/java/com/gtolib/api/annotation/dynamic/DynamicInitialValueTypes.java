package com.gtolib.api.annotation.dynamic;

public interface DynamicInitialValueTypes {
   String KEY_MULTIPLY = "multiplier";
   DynamicInitialValueTypes.TypeInfo MULTIPLY = new DynamicInitialValueTypes.TypeInfo("multiplier", "乘数", "Multiplier");
   String KEY_AMOUNT = "amount";
   DynamicInitialValueTypes.TypeInfo AMOUNT = new DynamicInitialValueTypes.TypeInfo("amount", "数量", "Amount");
   String KEY_PROBABILITY = "probability";
   DynamicInitialValueTypes.TypeInfo PROBABILITY = new DynamicInitialValueTypes.TypeInfo("probability", "概率", "Probability");
   String KEY_AMPERAGE_OUT = "amperage_out";
   DynamicInitialValueTypes.TypeInfo AMPERAGE_OUT = new DynamicInitialValueTypes.TypeInfo("amperage_out", "输出电流", "Amperage Out");
   String KEY_MAX_PARALLEL = "max_parallel";
   DynamicInitialValueTypes.TypeInfo MAX_PARALLEL = new DynamicInitialValueTypes.TypeInfo("max_parallel", "并行数", "Max Parallel");
   String KEY_CAPACITY = "capacity";
   DynamicInitialValueTypes.TypeInfo CAPACITY = new DynamicInitialValueTypes.TypeInfo("capacity", "容量", "Capacity");

   record TypeInfo(String key, String cn, String en) {
   }
}
