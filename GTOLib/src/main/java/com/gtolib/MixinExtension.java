package com.gtolib;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import com.bawnorton.mixinsquared.canceller.MixinCancellerRegistrar;
import com.bawnorton.mixinsquared.ext.MixinSquaredExtension;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.gtolib.ae2.crafting2.utils.PerfLogger;
import com.gtolib.utils.reflect.FieldReference;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Predicate;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.IExtensionRegistry;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

final class MixinExtension implements IExtension, MixinSquaredExtension, Predicate<IMixinInfo> {
   static final IExtension INSTANCE = new MixinExtension();
   static final Predicate<IExtension> MIXIN_EXTENSION_REMOVE;
   private static final Set<String> FILTER;
   private static final Class<?> CONTEXT_CLASS;
   private static final Field MIXINS;
   private static MethodHandle m;
   private static Set<MixinCanceller> cancellers;

   private MixinExtension() {
   }

   @Override
   public boolean checkActive(MixinEnvironment environment) {
      return true;
   }

   @Override
   public void preApply(ITargetClassContext context) {
      try {
         SortedSet<IMixinInfo> mixins = (SortedSet<IMixinInfo>)MIXINS.get(context);
         mixins.removeIf(this);
      } catch (Throwable e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void postApply(ITargetClassContext context) {
   }

   @Override
   public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
   }

   public boolean test(IMixinInfo mixin) {
      IMixinConfigPlugin plugin = mixin.getConfig().getPlugin();
      Class<? extends IMixinConfigPlugin> aClass = (Class<? extends IMixinConfigPlugin>)(plugin == null ? null : plugin.getClass());
      if (aClass != MixinConfigPlugin.class && aClass != com.gtocore.config.MixinConfigPlugin.class) {
         String mixinName = mixin.getClassName();
         boolean shouldCancel = false;
         if (FILTER.contains(mixinName)) {
            shouldCancel = true;
         } else {
            List<String> classes = mixin.getTargetClasses();
            int size = classes.size();
            String[] classNames = new String[size];

            for (int i = 0; i < size; i++) {
               String name = classes.get(i).replaceAll("/", ".");
               if (name.startsWith("com.greg") || name.startsWith("com.gto") || name.startsWith("com.hepdd")) {
                  return true;
               }

               classNames[i] = name;
            }

            List<String> list = Arrays.asList(classNames);

            for (MixinCanceller c : getCancellers()) {
               if (c.shouldCancel(list, mixinName)) {
                  shouldCancel = true;
                  break;
               }
            }
         }

         if (shouldCancel) {
            MixinConfigPlugin.LOGGER.warn("Cancelled mixin {}.", mixinName);
         }

         return shouldCancel;
      } else {
         if (m == null) {
            try {
               m = MethodHandles.publicLookup()
                  .unreflectGetter(FieldReference.fromClass(ClassInfo.class, "mixins").getField())
                  .asType(MethodType.methodType(Set.class, ClassInfo.class));
               PerfLogger.Companion.setDestroy(false);
            } catch (IllegalAccessException e) {
               return false;
            }
         }

         return false;
      }
   }

   private static Set<MixinCanceller> getCancellers() {
      if (cancellers == null) {
         try {
            remove();
            FieldReference<Set<MixinCanceller>> reference = FieldReference.fromClass(MixinCancellerRegistrar.class, "cancellers");
            cancellers = reference.get();
         } catch (Throwable e) {
            throw new RuntimeException(e);
         }
      }

      return cancellers;
   }

   static void remove() throws Throwable {
      IMixinTransformer transformer = (IMixinTransformer)MixinEnvironment.getDefaultEnvironment().getActiveTransformer();
      IExtensionRegistry extensions = transformer.getExtensions();
      FieldReference<Map<Class<? extends IExtension>, IExtension>> extensionMapField = FieldReference.fromInstance(extensions, "extensionMap");
      extensionMapField.get().entrySet().removeIf(entry -> MIXIN_EXTENSION_REMOVE.test(entry.getValue()));
      FieldReference<List<IExtension>> extensionsField = FieldReference.fromInstance(extensions, "extensions");
      extensionsField.get().removeIf(MIXIN_EXTENSION_REMOVE);
      FieldReference<List<IExtension>> activeExtensionsField = FieldReference.fromInstance(extensions, "activeExtensions");
      List<IExtension> newActiveExtensions = new ArrayList<>(activeExtensionsField.get());
      newActiveExtensions.removeIf(MIXIN_EXTENSION_REMOVE);
      activeExtensionsField.set(ImmutableList.copyOf(newActiveExtensions));
   }

   static void add() throws Throwable {
      if (FMLLoader.isProduction()) {
         Configurator.setRootLevel(Level.ERROR);
      }

      IMixinTransformer transformer = (IMixinTransformer)MixinEnvironment.getDefaultEnvironment().getActiveTransformer();
      Extensions extensions = (Extensions)transformer.getExtensions();
      extensions.add(INSTANCE);
   }

   static {
      Predicate<IExtension> predicate = extension -> extension == INSTANCE ? false : extension instanceof MixinSquaredExtension;
      if (FMLEnvironment.dist.isClient()) {
         Class<?> mixinTaintDetector;
         try {
            mixinTaintDetector = Class.forName("org.embeddedt.embeddium_integrity.MixinTaintDetector", false, MixinExtension.class.getClassLoader());
         } catch (ClassNotFoundException ignored) {
            mixinTaintDetector = null;
         }

         MIXIN_EXTENSION_REMOVE = mixinTaintDetector == null ? predicate : predicate.or(mixinTaintDetector::isInstance);
      } else {
         MIXIN_EXTENSION_REMOVE = predicate;
      }

      FILTER = ImmutableSet.of(
         "dev.ftb.mods.ftblibrary.core.mixin.common.ResourceLocationMixin",
         "net.blay09.mods.balm.mixin.LootTableMixin",
         "appeng.mixins.chunkloading.ChunkMapMixin",
         "team.creative.littletiles.mixin.common.collision.BlockCollisionsMixin",
         "gripe._90.arseng.mixin.ScribesTileMixin",
         "org.embeddedt.modernfix.common.mixin.perf.faster_ingredients.IngredientMixin"
      );

      try {
         CONTEXT_CLASS = Class.forName("org.spongepowered.asm.mixin.transformer.TargetClassContext");
         MIXINS = CONTEXT_CLASS.getDeclaredField("mixins");
         MIXINS.setAccessible(true);
      } catch (Throwable e) {
         throw new RuntimeException(e);
      }
   }
}
