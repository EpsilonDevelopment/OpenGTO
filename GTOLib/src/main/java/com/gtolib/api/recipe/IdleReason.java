package com.gtolib.api.recipe;

import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.network.chat.Component;

public class IdleReason {
   private static final List<IdleReason> VALUES = new ArrayList<>();
   public static final IdleReason AMOUNT_DETAILED = new IdleReason(
      "gtocore.idle_reason.amount_not_enough", "%s requires %s, but only %s is available", "%s需要%s，但只有%s可用"
   );
   public static final IdleReason LACK_MATERIAL = new IdleReason("gtocore.idle_reason.lack_material", "Lack of material", "缺少材料");
   public static final IdleReason INVALID_INPUT = new IdleReason("gtocore.idle_reason.invalid_input", "Invalid Input", "无效输入");
   public static final IdleReason NO_MATCH = new IdleReason("gtocore.idle_reason.no_match", "No Recipe Found", "没有找到配方");
   public static final IdleReason NO_MANA = new IdleReason("gtocore.idle_reason.no_mana", "Mana Not Enough", "魔力不足");
   public static final IdleReason NO_CWU = new IdleReason("gtceu.multiblock.computation.not_enough_computation", null, null);
   public static final IdleReason NO_EU = new IdleReason("behavior.prospector.not_enough_energy", null, null);
   public static final IdleReason INSUFFICIENT_OUT = new IdleReason("gtceu.recipe_logic.insufficient_out", null, null);
   public static final IdleReason OUTPUT_FULL = new IdleReason("gtocore.idle_reason.output_full", "Output Full", "输出槽已满");
   public static final IdleReason MAINTENANCE_BROKEN = new IdleReason("gtceu.top.maintenance_broken", null, null);
   public static final IdleReason MUFFLER_OBSTRUCTED = new IdleReason("gtceu.multiblock.universal.muffler_obstructed", null, null);
   public static final IdleReason MUFFLER_INSUFFICIENT = new IdleReason(
      "gtceu.multiblock.universal.muffler_insufficient", "Muffler Hatch Tier Insufficient", "消声仓等级不足"
   );
   public static final IdleReason HEAT_ACCUMULATION = new IdleReason("gtocore.idle_reason.heat_accumulation", "Heat Accumulation", "热量堆积");
   public static final IdleReason INSUFFICIENT_TEMPERATURE = new IdleReason("gtocore.idle_reason.insufficient_temperature", "Insufficient Temperature", "温度不足");
   public static final IdleReason BLOCK_TIER_NOT_SATISFIES = new IdleReason(
      "gtocore.idle_reason.block_tier_not_satisfies", "Block Tier Not Satisfies", "方块等级未达到要求"
   );
   public static final IdleReason VOLTAGE_TIER_NOT_SATISFIES = new IdleReason(
      "gtocore.idle_reason.voltage_tier_not_satisfies", "Voltage Tier Not Satisfies", "电压等级未达到要求"
   );
   public static final IdleReason NEUTRON_KINETIC_ENERGY_NOT_SATISFIES = new IdleReason(
      "gtocore.idle_reason.neutron_kinetic_energy_not_satisfies", "Neutron Kinetic Energy Not Satisfies", "中子动能未达到要求"
   );
   public static final IdleReason INSUFFICIENT_ENERGY_BUFFER = new IdleReason(
      "gtocore.idle_reason.insufficient_energy_buffer", "Insufficient energy buffer", "能量缓存不足"
   );
   public static final IdleReason NO_CRANK = new IdleReason("gtocore.idle_reason.no_crank", "Crank Not Enough", "动力不足");
   public static final IdleReason CANNOT_WORK_IN_SPACE = new IdleReason(
      "gtocore.idle_reason.cannot_work_in_space", "This machine cannot afford to work in such environment", "该机器无法在此环境下工作"
   );
   private Component reason;
   private final String key;
   private final String en;
   private final String cn;

   public IdleReason(String key, String en, String cn) {
      this.key = key;
      this.en = en;
      this.cn = cn;
      VALUES.add(this);
   }

   public Component reason() {
      if (this.reason == null) {
         this.reason = Component.translatable(this.key);
      }

      return this.reason;
   }

   public Component reason(Object... args) {
      return Component.translatable(this.key, args);
   }

   public void setReason(IRecipeHandlerHolder machine, Object... args) {
      machine.setIdleReason(() -> this.reason(args));
   }

   public void setReason(IRecipeHandlerHolder machine) {
      machine.setIdleReason(this::reason);
   }

   public void setReason(Object machine, Object... args) {
      if (machine instanceof ICustomRecipeLogicHolder holder) {
         holder.setIdleReason(() -> this.reason(args));
      }
   }

   public void setReason(Object machine) {
      if (machine instanceof ICustomRecipeLogicHolder holder) {
         holder.setIdleReason(this::reason);
      }
   }

   public static List<IdleReason> values() {
      return VALUES;
   }

   @Generated
   public String getKey() {
      return this.key;
   }

   @Generated
   public String getEn() {
      return this.en;
   }

   @Generated
   public String getCn() {
      return this.cn;
   }
}
