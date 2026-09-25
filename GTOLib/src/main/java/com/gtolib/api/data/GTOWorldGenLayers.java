package com.gtolib.api.data;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gtolib.GTOCore;
import com.gtolib.utils.TagUtils;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import org.jetbrains.annotations.NotNull;

public final class GTOWorldGenLayers implements IWorldGenLayer {
   public static final GTOWorldGenLayers ALL_LAYER = new GTOWorldGenLayers(
      "all_layer",
      new TagMatchTest(TagUtils.createBlockTag(GTOCore.id("all_layer"))),
      Arrays.stream(Dimension.values()).filter(Dimension::canGenerate).map(Dimension::getLocation).collect(Collectors.toSet())
   );
   private final String id;
   private final RuleTest target;
   private final Set<ResourceLocation> levels;

   private GTOWorldGenLayers(String id, RuleTest target, Set<ResourceLocation> levels) {
      this.id = id;
      this.target = target;
      this.levels = levels;
      WorldGeneratorUtils.WORLD_GEN_LAYERS.put(id, this);
   }

   @Override
   public Set<ResourceLocation> getLevels() {
      return this.levels;
   }

   @Override
   public RuleTest getTarget() {
      return this.target;
   }

   @Override
   public boolean isApplicableForLevel(ResourceLocation level) {
      return this.levels.contains(level);
   }

   @NotNull
   @Override
   public String getSerializedName() {
      return this.id;
   }

   public static void init() {
   }
}
