package gto.native0.plugins;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformationService.Resource;
import java.util.List;
import java.util.Set;
import native0.Loader;
import native0.hidden.Hidden0;
import org.jetbrains.annotations.NotNull;

public final class GTOServices implements ITransformationService {
   public static final String NAME = "gto_native";

   @NotNull
   public final native String name();

   public final native void initialize(IEnvironment var1);

   public static native long initialize();

   public final native void onLoad(IEnvironment var1, Set<String> var2);

   @NotNull
   public final native List<ITransformer> transformers();

   public final native List<Resource> completeScan(IModuleLayerManager var1);

   static {
      Loader.registerNativesForClass(3, GTOServices.class);
      Hidden0.special_clinit_3_90(GTOServices.class);
   }
}
