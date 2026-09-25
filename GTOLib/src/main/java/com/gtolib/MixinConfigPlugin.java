package com.gtolib;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.misc.AbstractMixinConfigPlugin;
import com.gtolib.utils.MathUtil;
import com.gtolib.utils.reflect.MethodReference;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DataGeneratorScanned
public final class MixinConfigPlugin extends AbstractMixinConfigPlugin {
   static final Logger LOGGER = LoggerFactory.getLogger("GTO Core");
   public static String hash = null;

   @Override
   public void onLoad(String mixinPackage) {
      try {
         ClassLoader classLoader = this.getClass().getClassLoader();

         try {
            MethodReference.fromClass(classLoader.loadClass("gto.native0.plugins.GTOProvider"), "init_gtolib", ClassLoader.class).invoke(classLoader);
         } catch (Throwable sealMissing) {
            LOGGER.warn("GTO Seal Runtime 未加载，使用开发回退初始化: {}", sealMissing.toString());
            MathUtil.applyDevMaskFallback();
         }

         MixinExtension.add();
      } catch (Throwable e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public List<String> getMixins() {
      try {
         MixinExtension.remove();
         return null;
      } catch (Throwable ex) {
         throw new RuntimeException(ex);
      }
   }

   @Override
   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return true;
   }
}
