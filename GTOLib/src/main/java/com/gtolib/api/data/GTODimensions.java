package com.gtolib.api.data;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gtolib.mc.ILevel;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class GTODimensions {
   public static final Set<String> PLANET_NAMESPACE = Set.of("ad_astra", "gtocore");
   static final Map<String, Galaxy> GALAXY_MAP = new O2OOpenCacheHashMap<>();
   static final Reference2ReferenceOpenHashMap<ResourceKey<Level>, Dimension> ALL_DIM = new Reference2ReferenceOpenHashMap<>();
   static final Reference2ReferenceOpenHashMap<ResourceKey<Level>, Dimension> ALL_DIM_AND_ORBIT_MAP = new Reference2ReferenceOpenHashMap<>();
   static final Reference2IntOpenHashMap<ResourceKey<Level>> ALL_GALAXY_DIM = new Reference2IntOpenHashMap<>();
   public static final ResourceKey<Level> OVERWORLD = Dimension.OVERWORLD.getResourceKey();
   public static final ResourceKey<Level> THE_NETHER = Dimension.THE_NETHER.getResourceKey();
   public static final ResourceKey<Level> THE_END = Dimension.THE_END.getResourceKey();
   public static final ResourceKey<Level> MOON = Dimension.MOON.getResourceKey();
   public static final ResourceKey<Level> MARS = Dimension.MARS.getResourceKey();
   public static final ResourceKey<Level> VENUS = Dimension.VENUS.getResourceKey();
   public static final ResourceKey<Level> MERCURY = Dimension.MERCURY.getResourceKey();
   public static final ResourceKey<Level> GLACIO = Dimension.GLACIO.getResourceKey();
   public static final ResourceKey<Level> ANCIENT_WORLD = Dimension.ANCIENT_WORLD.getResourceKey();
   public static final ResourceKey<Level> TITAN = Dimension.TITAN.getResourceKey();
   public static final ResourceKey<Level> PLUTO = Dimension.PLUTO.getResourceKey();
   public static final ResourceKey<Level> IO = Dimension.IO.getResourceKey();
   public static final ResourceKey<Level> GANYMEDE = Dimension.GANYMEDE.getResourceKey();
   public static final ResourceKey<Level> ENCELADUS = Dimension.ENCELADUS.getResourceKey();
   public static final ResourceKey<Level> CERES = Dimension.CERES.getResourceKey();
   public static final ResourceKey<Level> BARNARDA_C = Dimension.BARNARDA_C.getResourceKey();
   public static final ResourceKey<Level> OTHERSIDE = Dimension.OTHERSIDE.getResourceKey();
   public static final ResourceKey<Level> FLAT = Dimension.FLAT.getResourceKey();
   public static final ResourceKey<Level> VOID = Dimension.VOID.getResourceKey();
   public static final ResourceKey<Level> CREATE = Dimension.CREATE.getResourceKey();
   public static final ResourceKey<Level> ALFHEIM = Dimension.ALFHEIM.getResourceKey();
   private static final Set<ResourceKey<Level>> VOID_SET = Set.of(VOID, FLAT);

   public static boolean isVoid(ResourceKey<Level> key) {
      return key != null && VOID_SET.contains(key);
   }

   public static boolean isVoid(Level level) {
      return ((ILevel)level).gtolib$isVoid();
   }

   public static boolean isOverworld(ResourceKey<Level> key) {
      return OVERWORLD == key ? true : isVoid(key);
   }

   public static boolean isPlanet(ResourceKey<Level> key) {
      Dimension dim = ALL_DIM.get(key);
      return dim != null && dim.isWithinGalaxy();
   }

   public static Set<ResourceKey<Level>> getDimensionKeys(ResourceLocation location) {
      return Collections.singleton(getDimensionKey(location));
   }

   public static ResourceLocation getLocation(ResourceKey<Level> key) {
      return key.location();
   }

   public static ResourceKey<Level> getDimensionKey(ResourceLocation location) {
      return ResourceKey.create(Registries.DIMENSION, location);
   }

   public static String getTranslationKey(ResourceKey<Level> key) {
      Dimension dim = ALL_DIM.get(key);
      return dim != null ? dim.getKey() : key.location().toString();
   }

   public static int getTier(ResourceKey<Level> key) {
      Dimension dim = ALL_DIM.get(key);
      return dim == null ? 0 : dim.getTier();
   }

   public static int getPlanetDistances(ResourceKey<Level> key) {
      return ALL_GALAXY_DIM.getInt(key);
   }

   @Nullable
   public static Galaxy getGalaxy(ResourceKey<Level> key) {
      Dimension dim = ALL_DIM_AND_ORBIT_MAP.get(key);
      if (dim != null) {
         Galaxy galaxy = dim.getGalaxy();
         if (galaxy != Galaxy.NONE) {
            return galaxy;
         }
      }

      return null;
   }

   public static Dimension getDimensionIncludingOrbits(ResourceKey<Level> key) {
      return ALL_DIM_AND_ORBIT_MAP.get(key);
   }

   public static void forEachPlanet(Consumer<Dimension> consumer) {
      for (Dimension d : Dimension.values()) {
         if (d.isWithinGalaxy()) {
            consumer.accept(d);
         }
      }
   }
}
