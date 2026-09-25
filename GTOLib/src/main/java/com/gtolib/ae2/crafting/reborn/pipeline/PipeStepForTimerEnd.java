package com.gtolib.ae2.crafting.reborn.pipeline;

import appeng.api.crafting.IPatternDetails;
import com.gtocore.config.GTOConfig;
import com.gtolib.GTOCore;
import com.gtolib.ae2.crafting.reborn.Context;
import com.gtolib.ae2.crafting.reborn.api.pipeline.PipelineStep;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.time.Duration;
import java.time.Instant;

public class PipeStepForTimerEnd implements PipelineStep {
   private static final String GTO_AE_DEBUG_PREFIX = "[g..t.o ae计算]";
   private static final String GTO_AE_DEBUG_PREFIX_TOO_LONG_TIME = "[ae计算过长]";

   @Override
   public void execute(Context context) {
      if (GTOConfig.INSTANCE.devMode.aeLog && context.startTime != null) {
         Instant endTime = Instant.now();
         Duration duration = Duration.between(context.startTime, endTime);
         loggerSelector(
            duration, "G.T..O 优化ae算法, 请求者 {} , 计算时间: {} μs , 字节数: {} bytes", context.requester.getActionSource(), duration.toNanos() / 1000L, context.bytes
         );
         loggerSelector(duration, "模拟 : {}", context.isSimulate);
         loggerSelector(duration, "目标 : {}", context.target);
         long step = 0L;
         ObjectIterator<Entry<IPatternDetails>> it = context.patternTimes.object2LongEntrySet().fastIterator();

         while (it.hasNext()) {
            step += it.next().getLongValue();
         }

         loggerSelector(duration, "步数 : {}", step);
      }
   }

   private static void loggerSelector(Duration duration, String message, Object... objects) {
      if (duration.toMillis() <= 50L) {
         if (GTOConfig.INSTANCE.devMode.aeLog) {
            String s = "[g..t.o ae计算]" + message;
            GTOCore.LOGGER.info(s, objects);
         }
      } else {
         String s = "[ae计算过长]" + message;
         GTOCore.LOGGER.error(s, objects);
      }
   }
}
