package gto.native0.plugins;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService.Phase;
import java.util.EnumSet;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public final class GTOPlugin implements ILaunchPluginService {
   private static final Object YAY = EnumSet.of(Phase.BEFORE);
   private static final Object NAY = EnumSet.noneOf(Phase.class);

   GTOPlugin() {
   }

   public String name() {
      return "gto_native";
   }

   public EnumSet<Phase> handlesClass(Type var1, boolean var2) {
      throw new IllegalStateException("Outdated ModLauncher");
   }

   public EnumSet<Phase> handlesClass(Type var1, boolean var2, String var3) {
      if ("mixin".equals(var3)) {
         return (EnumSet<Phase>)NAY;
      } else if ("gto_native".equals(var3)) {
         return (EnumSet<Phase>)NAY;
      } else {
         return var1.getClassName().startsWith("com.gtolib") ? (EnumSet)YAY : (EnumSet)NAY;
      }
   }

   public int processClassWithFlags(Phase var1, ClassNode var2, Type var3, String var4) {
      GTOProvider.init_gtolib(var3);
      return 0;
   }
}
