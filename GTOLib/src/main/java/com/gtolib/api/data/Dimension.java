package com.gtolib.api.data;

import com.gtolib.GTOCore;
import com.gtolib.utils.RLUtils;
import com.kyanite.deeperdarker.DeeperDarker;
import lombok.Generated;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public enum Dimension {
   OVERWORLD(Level.OVERWORLD.location(), "Overworld", "主世界", 0, 3, Galaxy.SOLAR, null),
   MOON(RLUtils.ad("moon"), "Moon", "月球", 1, 3, Galaxy.SOLAR, RLUtils.ad("moon_stone")),
   MARS(RLUtils.ad("mars"), "Mars", "火星", 2, 4, Galaxy.SOLAR, RLUtils.ad("mars_stone")),
   VENUS(RLUtils.ad("venus"), "Venus", "金星", 3, 2, Galaxy.SOLAR, RLUtils.ad("venus_stone")),
   MERCURY(RLUtils.ad("mercury"), "Mercury", "水星", 4, 1, Galaxy.SOLAR, RLUtils.ad("mercury_stone")),
   CERES(GTOCore.id("ceres"), "Ceres", "谷神星", 4, 5, Galaxy.SOLAR, GTOCore.id("ceres_stone")),
   IO(GTOCore.id("io"), "Io", "木卫一", 5, 6, Galaxy.SOLAR, GTOCore.id("io_stone")),
   GANYMEDE(GTOCore.id("ganymede"), "Ganymede", "木卫三", 5, 6, Galaxy.SOLAR, GTOCore.id("ganymede_stone")),
   ENCELADUS(GTOCore.id("enceladus"), "Enceladus", "土卫二", 6, 7, Galaxy.SOLAR, GTOCore.id("enceladus_stone")),
   TITAN(GTOCore.id("titan"), "Titan", "土卫六", 6, 7, Galaxy.SOLAR, GTOCore.id("titan_stone")),
   PLUTO(GTOCore.id("pluto"), "Pluto", "冥王星", 6, 8, Galaxy.SOLAR, GTOCore.id("pluto_stone")),
   GLACIO(RLUtils.ad("glacio"), "Glacio", "霜原星", 7, 1, Galaxy.PROXIMA_CENTAURI, RLUtils.ad("glacio_stone")),
   BARNARDA_C(GTOCore.id("barnarda_c"), "Barnarda C", "巴纳德C", 8, 1, Galaxy.BARNARDA, GTOCore.id("barnarda_c_log")),
   OTHERSIDE(DeeperDarker.rl("otherside"), "Otherside", "幽冥", 9, -1, Galaxy.NONE, DeeperDarker.rl("sculk_stone")),
   ALFHEIM(RLUtils.parse("mythicbotany:alfheim"), "Alfheim", "亚尔夫海姆", 0, -1, Galaxy.NONE, RLUtils.mybot("alfsteel_block")),
   THE_NETHER(Level.NETHER.location(), "Nether", "下界", 0, -1, Galaxy.NONE, null),
   THE_END(Level.END.location(), "End", "末地", 0, -1, Galaxy.NONE, null),
   ANCIENT_WORLD(GTOCore.id("ancient_world"), "Ancient World", "远古世界", 0, -1, Galaxy.NONE, GTOCore.id("reactor_core")),
   FLAT(GTOCore.id("flat"), "flat", "超平坦", 0, -1, Galaxy.NONE, RLUtils.mc("crying_obsidian")),
   VOID(GTOCore.id("void"), "Void", "虚空", 0, -1, Galaxy.NONE, RLUtils.mc("obsidian")),
   CREATE(GTOCore.id("create"), "Create", "创造", 10, -1, Galaxy.NONE, RLUtils.mc("obsidian"));

   private final ResourceKey<Level> resourceKey;
   private final String en;
   private final String cn;
   private final String key;
   private final int tier;
   private final Galaxy galaxy;
   private final ResourceLocation itemKey;
   private final ResourceKey<Level> orbit;

   Dimension(ResourceLocation location, String en, String cn, int tier, int stellarDistance, Galaxy galaxy, ResourceLocation itemKey) {
      this.en = en;
      this.cn = cn;
      this.tier = tier;
      this.galaxy = galaxy;
      this.resourceKey = GTODimensions.getDimensionKey(location);
      this.itemKey = itemKey;
      this.key = "gtocore.dimension." + location.getPath();
      GTODimensions.ALL_DIM.put(this.resourceKey, this);
      if (this.isWithinGalaxy()) {
         GTODimensions.ALL_GALAXY_DIM.put(this.resourceKey, stellarDistance);
         this.orbit = this.resourceKey == Level.OVERWORLD
            ? GTODimensions.getDimensionKey(RLUtils.ad("earth_orbit"))
            : GTODimensions.getDimensionKey(RLUtils.fromNamespaceAndPath(location.getNamespace(), location.getPath() + "_orbit"));
         GTODimensions.ALL_DIM_AND_ORBIT_MAP.put(this.orbit, this);
         GTODimensions.ALL_GALAXY_DIM.put(this.orbit, stellarDistance);
      } else {
         this.orbit = null;
      }
   }

   public ResourceLocation getLocation() {
      return this.resourceKey.location();
   }

   public boolean canGenerate() {
      return this.ordinal() < 18;
   }

   public boolean isWithinGalaxy() {
      return this.ordinal() < 13;
   }

   public static Dimension from(Level level) {
      return from(level.dimension());
   }

   public static Dimension from(ResourceKey<Level> location) {
      Dimension dim = GTODimensions.ALL_DIM.get(location);
      if (dim == null) {
         GTOCore.LOGGER.warn("Dimension {} is not registered in GTOCore", location);
      }

      return dim;
   }

   @Generated
   public ResourceKey<Level> getResourceKey() {
      return this.resourceKey;
   }

   @Generated
   public String getEn() {
      return this.en;
   }

   @Generated
   public String getCn() {
      return this.cn;
   }

   @Generated
   public String getKey() {
      return this.key;
   }

   @Generated
   public int getTier() {
      return this.tier;
   }

   @Generated
   public Galaxy getGalaxy() {
      return this.galaxy;
   }

   @Generated
   public ResourceLocation getItemKey() {
      return this.itemKey;
   }

   @Generated
   public ResourceKey<Level> getOrbit() {
      return this.orbit;
   }

   // $VF: synthetic method
   private static Dimension[] $values() {
      return new Dimension[]{
         OVERWORLD,
         MOON,
         MARS,
         VENUS,
         MERCURY,
         CERES,
         IO,
         GANYMEDE,
         ENCELADUS,
         TITAN,
         PLUTO,
         GLACIO,
         BARNARDA_C,
         OTHERSIDE,
         ALFHEIM,
         THE_NETHER,
         THE_END,
         ANCIENT_WORLD,
         FLAT,
         VOID,
         CREATE
      };
   }

   static {
      GTODimensions.ALL_DIM_AND_ORBIT_MAP.putAll(GTODimensions.ALL_DIM);
   }
}
