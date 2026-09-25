package com.gtolib.api.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import com.gtocore.config.GTOConfig;
import com.gtolib.GTOCore;
import com.gtolib.api.annotation.dynamic.DynamicInitialData;
import com.gtolib.cache.CacheManager;
import com.gtolib.gtm.Multiblock;
import com.gtolib.utils.FileUtils;
import com.gtolib.utils.ItemUtils;
import com.gtolib.utils.iostream.IOStreamDecoder;
import com.gtolib.utils.iostream.IOStreamEncoder;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import kotlin.Triple;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLLoader;

public final class MultiblockDefinition extends MultiblockMachineDefinition implements IGTOMachineDefinition {
   public int maxTier = -1;
   public boolean upgradable;
   private MultiblockDefinition.Pattern[] patterns;
   private DynamicInitialData dynamicInitialData;
   private boolean canWorkInSpaceIndependently;

   private MultiblockDefinition(ResourceLocation id) {
      super(id);
   }

   public static MultiblockDefinition createDefinition(ResourceLocation id) {
      return new MultiblockDefinition(id);
   }

   public static MultiblockDefinition of(MultiblockMachineDefinition definition) {
      return (MultiblockDefinition)definition;
   }

   public static void addMatchingShapes(boolean fast, BlockPattern blockPattern, ArrayList<MultiblockShapeInfo> shapes) {
      List<Supplier<BlockInfo[][][]>> list = repetitionDFS(blockPattern, new ArrayList<>(), blockPattern.aisleRepetitions, new IntArrayList());
      if (fast) {
         shapes.add(new MultiblockShapeInfo(() -> blockPattern, list.getFirst().get()));
         if (list.size() > 1) {
            shapes.add(new MultiblockShapeInfo(() -> blockPattern, list.getLast().get()));
         }
      } else {
         list.forEach(blockInfo -> shapes.add(new MultiblockShapeInfo(() -> blockPattern, blockInfo.get())));
      }
   }

   public static MultiblockShapeInfo getClampedMatchingShape(BlockPattern blockPattern, int repeatCount) {
      int[] repetitions = getClampedAisleRepetitions(blockPattern, repeatCount);
      return new MultiblockShapeInfo(() -> blockPattern, blockPattern.getPreview(repetitions));
   }

   public static int[] getClampedAisleRepetitions(BlockPattern blockPattern, int repeatCount) {
      int[][] aisleRepetitions = blockPattern.aisleRepetitions;
      int[] repetitions = new int[aisleRepetitions.length];

      for (int i = 0; i < aisleRepetitions.length; i++) {
         repetitions[i] = Math.clamp(repeatCount, aisleRepetitions[i][0], aisleRepetitions[i][1]);
      }

      return repetitions;
   }

   private static List<Supplier<BlockInfo[][][]>> repetitionDFS(
      BlockPattern pattern, List<Supplier<BlockInfo[][][]>> pages, int[][] aisleRepetitions, IntArrayList repetitionStack
   ) {
      if (repetitionStack.size() == aisleRepetitions.length) {
         int[] repetition = new int[repetitionStack.size()];

         for (int i = 0; i < repetitionStack.size(); i++) {
            repetition[i] = repetitionStack.getInt(i);
         }

         pages.add(() -> pattern.getPreview(repetition));
      } else {
         for (int i = aisleRepetitions[repetitionStack.size()][0]; i <= aisleRepetitions[repetitionStack.size()][1]; i++) {
            repetitionStack.push(i);
            repetitionDFS(pattern, pages, aisleRepetitions, repetitionStack);
            repetitionStack.popInt();
         }
      }

      return pages;
   }

   @Override
   public List<MultiblockShapeInfo> getMatchingShapes() {
      Supplier<List<MultiblockShapeInfo>> designs = this.getShapes();
      if (designs != null) {
         List<MultiblockShapeInfo> design = designs.get();
         if (!design.isEmpty()) {
            return design;
         }
      }

      ArrayList<MultiblockShapeInfo> list = new ArrayList<>();

      for (Supplier<BlockPattern> factory : this.patternFactory) {
         addMatchingShapes(true, factory.get(), list);
      }

      return list;
   }

   public void clear() {
      this.patterns = null;
      this.shapes = null;
   }

   public static void init() {
      long time = System.currentTimeMillis();

      for (MachineDefinition machine : GTRegistries.MACHINES.values()) {
         if (machine instanceof MultiblockDefinition definition && definition.isRenderXEIPreview()) {
            Supplier<List<MultiblockShapeInfo>> supplier = () -> {
               List<MultiblockShapeInfo> shapesx = definition.getShapes().get();
               if (shapesx.isEmpty()) {
                  ArrayList<MultiblockShapeInfo> list = new ArrayList<>();

                  for (Supplier<BlockPattern> factory : definition.patternFactory) {
                     addMatchingShapes(GTOConfig.INSTANCE.misc.fastMultiBlockPage, factory.get(), list);
                  }

                  if (definition.subPatternFactory != null) {
                     for (Supplier<BlockPattern> factory : definition.subPatternFactory) {
                        addMatchingShapes(GTOConfig.INSTANCE.misc.fastMultiBlockPage, factory.get(), list);
                     }
                  }

                  shapesx = list;
               }

               return shapesx;
            };
            File file = CacheManager.getCacheFile("multiblock/" + definition.getName());
            MultiblockDefinition.Pattern[] patterns;
            if (FMLLoader.isProduction() && file.exists() && file.canRead()) {
               List<List<ItemStack>> list = FileUtils.loadFromFile(file, IOStreamDecoder.list(IOStreamDecoder.list(ItemUtils.STACK_IO_CODEC)));
               patterns = new MultiblockDefinition.Pattern[list.size()];

               for (int i = 0; i < list.size(); i++) {
                  patterns[i] = new MultiblockDefinition.Pattern(GTMemoizer.memoize(supplier), list.get(i));
               }
            } else {
               List<MultiblockShapeInfo> shapes = supplier.get();
               patterns = new MultiblockDefinition.Pattern[shapes.size()];

               for (int i = 0; i < shapes.size(); i++) {
                  patterns[i] = new MultiblockDefinition.Pattern(GTMemoizer.memoize(supplier), Multiblock.initializePattern(definition, shapes.get(i)));
               }

               if (FMLLoader.isProduction()) {
                  FileUtils.saveToFile(
                     (List<List<ItemStack>>)Arrays.stream(patterns).map(p -> p.parts).collect(Collectors.toCollection(ArrayList::new)),
                     file,
                     IOStreamEncoder.collection(IOStreamEncoder.collection(ItemUtils.STACK_IO_CODEC))
                  );
               }
            }

            definition.patterns = patterns;
         }
      }

      GTOCore.LOGGER.info("Pre initialization of multiBlock took {}ms", System.currentTimeMillis() - time);
   }

   @Override
   public boolean canWorkInSpaceIndependently() {
      return this.canWorkInSpaceIndependently;
   }

   @Generated
   public MultiblockDefinition.Pattern[] getPatterns() {
      return this.patterns;
   }

   @Generated
   @Override
   public void setDynamicInitialData(DynamicInitialData dynamicInitialData) {
      this.dynamicInitialData = dynamicInitialData;
   }

   @Generated
   @Override
   public DynamicInitialData getDynamicInitialData() {
      return this.dynamicInitialData;
   }

   @Generated
   @Override
   public void setCanWorkInSpaceIndependently(boolean canWorkInSpaceIndependently) {
      this.canWorkInSpaceIndependently = canWorkInSpaceIndependently;
   }

   public record Pattern(Supplier<List<MultiblockShapeInfo>> supplier, List<ItemStack> parts) {
      public Triple<BlockPos, BlockPattern, Reference2ReferenceOpenHashMap<BlockInfo, LongOpenHashSet>> initialize(MultiblockDefinition definition, int i) {
         return Multiblock.initializeInfo(null, definition, this.supplier.get().get(i));
      }
   }
}
