package com.gtolib.api.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterEnumLang;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;

@DataGeneratorScanned
public interface IPatternProviderLogic {
   String keyPrefix = "gtocore.ae2.pattern_provider.push_result";

   BlockingType gtolib$getBlocking();

   IPatternDetails gtolib$getCurrentPattern();

   IPatternDetails gtolib$getCachePattern(long var1, int var3);

   IPatternProviderLogic.PushResult gtolib$pushPattern(IPatternDetails var1, ObjHolder<KeyCounter[]> var2, Supplier<IPatternProviderLogic.PushResult> var3);

   default GlobalPos gto$getPos() {
      return GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO);
   }

   @RegisterEnumLang(keyPrefix = "gtocore.ae2.pattern_provider.push_result")
   @DataGeneratorScanned
   enum PushResult {
      SUCCESS(0, "已推送", "Pushed"),
      BREAK(1, "已完成推送", "Push Completed"),
      BREAK_TASK_LOOP(2, "已完成当前轮次推送", "Current Push Round Completed"),
      PATTERN_DOES_NOT_EXIST(-1, "样板不存在", "Pattern Does Not Exist"),
      GRID_NODE_MISSING(-2, "相关AE节点不在线", "Related AE Nodes Offline"),
      NOWHERE_TO_PUSH(-3, "推送到达机器可处理上限", "Nowhere To Push"),
      PATTERN_PROVIDER_LOCKED(-4, "样板供应器被锁定", "Pattern Provider Locked"),
      REJECTED(-5, "机器拒绝接受样板", "Machine Rejected"),
      INSUFFICIENT_PRIORITY(-6, "此样板的材料虽然充足，但需要供给优先级更高的样板", "Insufficient Priority");

      public final int code;
      @RegisterEnumLang.CnValue("name")
      private final String cn;
      @RegisterEnumLang.EnValue("name")
      private final String en;

      PushResult(int code, String cn, String en) {
         this.code = code;
         this.cn = cn;
         this.en = en;
      }

      public boolean success() {
         return this.code >= 0;
      }

      public boolean needBreak() {
         return this.code > 0;
      }

      public String getTranslationKey() {
         return "gtocore.ae2.pattern_provider.push_result.name." + this.name();
      }

      // $VF: synthetic method
      private static IPatternProviderLogic.PushResult[] $values() {
         return new IPatternProviderLogic.PushResult[]{
            SUCCESS,
            BREAK,
            BREAK_TASK_LOOP,
            PATTERN_DOES_NOT_EXIST,
            GRID_NODE_MISSING,
            NOWHERE_TO_PUSH,
            PATTERN_PROVIDER_LOCKED,
            REJECTED,
            INSUFFICIENT_PRIORITY
         };
      }
   }
}
