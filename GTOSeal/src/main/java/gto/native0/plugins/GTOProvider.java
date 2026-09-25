package gto.native0.plugins;

import cpw.mods.jarhandling.SecureJar.ModuleDataProvider;
import native0.Loader;
import native0.hidden.Hidden0;
import org.objectweb.asm.Type;

public final class GTOProvider {
   private static ModuleDataProvider ㅤࣦ࣮ࣶ;
   private static ModuleDataProvider ㅤࣰࣲࣥ;
   private static int ㅤࣺ࣭ࣤ;
   private static int ㅤ࣮࣯ࣧ;
   private static long ㅤࣸࣼࣻ;

   private static native int ㅤࣦ࣮ࣶ(Object var0, Object var1);

   protected static native void init_gtolib(Type var0);

   protected static native void init_gtolib(ClassLoader var0);

   protected static native void decrypt(byte[] var0, String var1);

   static {
      Loader.registerNativesForClass(2, GTOProvider.class);
      Hidden0.special_clinit_2_50(GTOProvider.class);
   }
}
