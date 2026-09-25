package com.gtolib.utils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtolib.GTOCore;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.mc.ILevel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

public final class GTOUtils {
   public static final BiConsumer NOOP_BI_CONSUMER = (a, b) -> {};
   public static final ExecutorService ASYNC_SINGLE_THREAD_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r);
      thread.setDaemon(true);
      thread.setPriority(1);
      return thread;
   });

   private GTOUtils() {
   }

   public static <T> Consumer<T> noopConsumer() {
      return GTUtil.NOOP_CONSUMER;
   }

   public static <T> T of(Object o) {
      return (T)o;
   }

   @SafeVarargs
   public static <T> T[] array(T... e) {
      return e;
   }

   public static void asyncExecute(Lock lock, Runnable runnable) {
      asyncExecute(() -> {
         lock.lock();

         try {
            runnable.run();
         } catch (Throwable t) {
            Configurator.setRootLevel(Level.DEBUG);
            t.printStackTrace();
         } finally {
            lock.unlock();
         }
      });
   }

   public static void asyncExecute(Runnable runnable) {
      ASYNC_SINGLE_THREAD_EXECUTOR.execute(runnable);
   }

   public static void gc() {
      asyncExecute(System::gc);
   }

   public static int getGeneratorAmperage(int tier) {
      return tier > 0 && tier < 4 ? 2 : 1;
   }

   public static int getGeneratorEfficiency(GTRecipeType recipeType, int tier) {
      int base = 105 - 5 * tier;
      if (recipeType == GTRecipeTypes.STEAM_TURBINE_FUELS) {
         base = 135 - 35 * tier;
      }

      if (recipeType == GTORecipeTypes.NAQUADAH_REACTOR) {
         base = 100 + 50 * (tier - 5);
      }

      if (recipeType == GTORecipeTypes.THERMAL_GENERATOR_FUELS) {
         base = 100 - 25 * tier;
      }

      return base + 30 - 15 * GTOCore.difficulty;
   }

   public static boolean isGeneration(TagPrefix tagPrefix, Material material) {
      Predicate<Material> condition = tagPrefix.generationCondition();
      return condition == null ? true : condition.test(material);
   }

   public static boolean probability(double chance) {
      return GTValues.RNG.nextDouble() < chance;
   }

   public static boolean safe(ServerPlayer player) {
      Vec3 vec3 = player.position();
      List<Monster> list = player.level()
         .getEntitiesOfClass(
            Monster.class, new AABB(vec3.x() - 8.0, vec3.y() - 5.0, vec3.z() - 8.0, vec3.x() + 8.0, vec3.y() + 5.0, vec3.z() + 8.0), p_9062_ -> true
         );
      if (!list.isEmpty()) {
         player.displayClientMessage(Component.translatable("gtocore.not_safe"), true);
         return false;
      } else {
         return true;
      }
   }

   public static int adjacentBlock(Function<Direction, Block> function, Block block) {
      int a = 0;

      for (Direction side : GTUtil.DIRECTIONS) {
         if (function.apply(side) == block) {
            a++;
         }
      }

      return a;
   }

   public static double calculateDistance(BlockPos pos1, BlockPos pos2) {
      int deltaX = pos2.getX() - pos1.getX();
      int deltaY = pos2.getY() - pos1.getY();
      int deltaZ = pos2.getZ() - pos1.getZ();
      return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
   }

   public static int getVoltageMultiplier(Material material) {
      int t = material.getBlastTemperature();
      if (t > 8460) {
         return GTValues.VA[3];
      } else if (t > 2700) {
         return GTValues.VA[2];
      } else {
         return t > 0 ? GTValues.VA[1] : GTValues.VA[0];
      }
   }

   public static double getSunIntensity(long dayTime) {
      long var4 = dayTime % 24000L;
      if (var4 >= 1200L && var4 < 6000L) {
         double normalizedTime = (var4 - 1200L) / 4800.0;
         return Math.pow(normalizedTime, 2.0) * 100.0;
      } else if (var4 >= 6000L && var4 < 12000L) {
         double normalizedTime = (12000L - var4) / 6000.0;
         return Math.pow(normalizedTime, 2.0) * 100.0;
      } else {
         return 0.0;
      }
   }

   public static boolean canSeeSunClearly(net.minecraft.world.level.Level world, BlockPos blockPos) {
      if (!world.canSeeSky(blockPos.above())) {
         return false;
      } else if (GTODimensions.isVoid(world)) {
         return true;
      } else {
         Biome biome = world.getBiome(blockPos.above()).value();
         if (!world.isRaining() || !biome.warmEnoughToRain(blockPos.above()) && !biome.coldEnoughToSnow(blockPos.above())) {
            return world.getBiome(blockPos.above()).is(BiomeTags.IS_END) ? false : world.isDay();
         } else {
            return false;
         }
      }
   }

   public static Map<String, Ingredient> reconstructKeys(NonNullList<Ingredient> ingredients) {
      Map<String, Ingredient> keys = new O2OOpenCacheHashMap<>();
      Set<Ingredient> usedIngredients = new OpenCacheHashSet<>();
      char nextKey = 'A';

      for (Ingredient ingredient : ingredients) {
         if (ingredient != Ingredient.EMPTY && !usedIngredients.contains(ingredient)) {
            String key = String.valueOf(nextKey++);
            keys.put(key, ingredient);
            usedIngredients.add(ingredient);
         }
      }

      return keys;
   }

   public static String[] reconstructPattern(NonNullList<Ingredient> ingredients, Map<String, Ingredient> keys, int patternWidth, int patternHeight) {
      String[] pattern = new String[patternHeight];

      for (int i = 0; i < patternHeight; i++) {
         StringBuilder row = new StringBuilder();

         for (int j = 0; j < patternWidth; j++) {
            Ingredient ingredient = ingredients.get(j + patternWidth * i);
            if (ingredient == Ingredient.EMPTY) {
               row.append(" ");
            } else {
               for (Entry<String, Ingredient> entry : keys.entrySet()) {
                  if (entry.getValue().equals(ingredient)) {
                     row.append(entry.getKey());
                     break;
                  }
               }
            }
         }

         pattern[i] = row.toString();
      }

      return pattern;
   }

   public static GlobalPos readGlobalPos(String dimension, long pos) {
      if (dimension.isEmpty()) {
         return null;
      }

      if (pos == 0L) {
         return null;
      }

      ResourceLocation key = ResourceLocation.tryParse(dimension);
      return key == null ? null : GlobalPos.of(GTODimensions.getDimensionKey(key), BlockPos.of(pos));
   }

   public static ItemStack loadItemStack(CompoundTag tag) {
      Item item = RegistriesUtils.getItem(tag.getString("id"));
      ItemStack stack = item.getDefaultInstance();
      if (tag.contains("tag", 10)) {
         stack.setTag(tag.getCompound("tag"));
         if (stack.getTag() != null) {
            stack.getItem().verifyTagAfterLoad(stack.getTag());
         }
      }

      if (stack.getItem().canBeDepleted()) {
         stack.setDamageValue(stack.getDamageValue());
      }

      return stack;
   }

   public static FluidStack loadFluidStack(CompoundTag tag) {
      Fluid fluid = FluidUtils.getFluid(tag.getString("FluidName"));
      FluidStack stack = new FluidStack(fluid, 1);
      if (tag.contains("Tag", 10)) {
         stack.setTag(tag.getCompound("Tag"));
      }

      return stack;
   }

   public static void fastRemoveBlock(net.minecraft.world.level.Level level, BlockPos pos, boolean breakBedrock, boolean updateLight) {
      ILevel.fastRemoveBlock(level, pos, breakBedrock, updateLight);
   }

   public static Collector<Component, MutableComponent, MutableComponent> joiningComponent(Component delimiter) {
      return Collector.of(
         Component::empty,
         (c, t) -> c.append(c.getString().isEmpty() ? t : Component.empty().append(delimiter).append(t)),
         (c1, c2) -> c1.append(c2.getString().isEmpty() ? c2 : Component.empty().append(delimiter).append(c2))
      );
   }
}
