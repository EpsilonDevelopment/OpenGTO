package com.gtolib.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.capability.IWailaDisplayProvider;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.codec.DataCodec;
import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.datastream.data.ListData;
import com.gto.datasynclib.util.holder.IntHolder;
import com.gtolib.api.machine.impl.part.ThreadPartMachine;
import com.gtolib.api.machine.trait.CrossRecipeTrait;
import com.gtolib.api.recipe.IdleReason;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.wireless.IWirelessContainer;
import com.gtolib.gtm.RecipeLogicExt;
import com.gtolib.utils.NumberUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;

public interface ICrossRecipeMachine extends IEnhancedMultiblockMachine, IParallelMachine, IWailaDisplayProvider, ITieredMachine {
   CrossRecipeTrait getCrossRecipeTrait();

   @Nullable
   IWirelessContainer getWirelessContainer();

   long getOverclockMaxUnit();

   long getRecipeUnit(GTRecipe var1);

   void setRecipeUnit(long var1, RecipeBuilder var3);

   @MustBeInvokedByOverriders
   GTRecipe getRealRecipe(RecipeHandlerUnit var1, @NotNull GTRecipe var2);

   default Set<GTRecipeDefinition> getLastRecipes() {
      return this.getCrossRecipeTrait().lastRecipes;
   }

   default int getThread() {
      return this.getCrossRecipeTrait().getThread();
   }

   default double getOverclockFactor() {
      return this.getCrossRecipeTrait().getOverclockFactor();
   }

   default boolean isRepeatedRecipes() {
      ThreadPartMachine machine = this.getCrossRecipeTrait().threadHatchPartMachine;
      return machine == null ? true : machine.isRepeatedRecipes();
   }

   default boolean isIndependentThread() {
      ThreadPartMachine machine = this.getCrossRecipeTrait().threadHatchPartMachine;
      boolean support = this.getWirelessContainer() != null;
      return machine == null ? support : support && machine.isIThread();
   }

   default List<ICrossRecipeMachine.Thread> getThreads() {
      return ((ICrossRecipeMachine.Logic)this.getRecipeLogic()).threads;
   }

   @Override
   default long getMaxParallel() {
      return this.getCrossRecipeTrait().getMaxParallel();
   }

   @Override
   default long getMinParallel() {
      return this.getCrossRecipeTrait().getMinParallel();
   }

   @Override
   default long getParallel() {
      return this.getCrossRecipeTrait().getParallel();
   }

   @Override
   default void setParallel(long number) {
      this.getCrossRecipeTrait().setParallel(number);
   }

   @Override
   default RecipeLogic createRecipeLogic(Object @NotNull ... args) {
      return new ICrossRecipeMachine.Logic(this);
   }

   private static float getProgress(long progress, long maxProgress) {
      return maxProgress == 0L ? 0.0F : (float)((double)progress / maxProgress);
   }

   @Override
   default void appendWailaTooltip(CompoundTag data, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
      if (data.tags.get("recipe_thread") instanceof ListTag listTag) {
         IntHolder i = new IntHolder();
         listTag.forEach(
            tag -> {
               if (tag instanceof CompoundTag capData) {
                  iTooltip.add(
                     Component.translatable("gtocore.machine.text.thread", ICrossRecipeMachine.Thread.getID(i, capData.getInt("u")))
                        .withStyle(ChatFormatting.GRAY)
                  );
                  int currentProgress = capData.getInt("p");
                  int maxProgress = capData.getInt("d");
                  Component text;
                  if (maxProgress < 20) {
                     text = Component.translatable("gtceu.jade.progress_tick", currentProgress, maxProgress);
                  } else {
                     text = Component.translatable("gtceu.jade.progress_sec", Math.round(currentProgress / 20.0F), Math.round(maxProgress / 20.0F));
                  }

                  if (maxProgress > 0) {
                     int color = -11748585;
                     iTooltip.add(
                        iTooltip.getElementHelper()
                           .progress(
                              getProgress(currentProgress, maxProgress),
                              text,
                              iTooltip.getElementHelper().progressStyle().color(color).textColor(-1),
                              Util.make(BoxStyle.DEFAULT, style -> style.borderColor = -11184811),
                              true
                           )
                     );
                  }
               }
            }
         );
      }
   }

   @Override
   default void appendWailaData(CompoundTag data, BlockAccessor blockAccessor) {
      ListTag list = new ListTag();
      this.getThreads().forEach(thread -> {
         CompoundTag tag = new CompoundTag();
         tag.putInt("p", thread.progress);
         tag.putInt("d", thread.duration);
         tag.putInt("u", thread.use);
         list.add(tag);
      });
      if (!list.isEmpty()) {
         data.put("recipe_thread", list);
      }
   }

   class Logic extends RecipeLogicExt {
      @SaveToDisk
      private final ArrayList<ICrossRecipeMachine.Thread> threads = new ArrayList<>();
      private final ICrossRecipeMachine machine;
      private boolean shouldSearch;
      private int searchInterval = 20;

      public Logic(ICrossRecipeMachine machine) {
         super(machine);
         this.machine = machine;
      }

      @Override
      public void updateTickSubscription() {
         if (this.status != 3 && this.machine.isRecipeLogicAvailable()) {
            this.shouldSearch = true;
            if ((this.subscription == null || !this.subscription.stillSubscribed) && this.machine.self().getLevel() instanceof ServerLevel serverLevel) {
               boolean single = this.threads.isEmpty() && this.machine.getThread() < 2;
               this.machine.getCrossRecipeTrait().isSingleThread = single;
               this.subscription = TaskHandler.enqueueTick(
                  serverLevel, this.machine.self().holder.isRemove, single ? () -> super.serverTick() : this::serverTick, this.interval, 10
               );
               if (this.isActive || !this.threads.isEmpty()) {
                  this.subscription.cycle = 0;
               }
            }
         } else {
            this.unsubscribe();
         }
      }

      @Override
      public void serverTick() {
         if (this.status == 3) {
            this.unsubscribe();
         } else {
            if (this.threads.isEmpty()) {
               if (this.status != 0 && this.lastRecipe != null) {
                  if (this.progress < this.duration) {
                     this.handleRecipeWorking();
                  }

                  if (this.progress < this.duration) {
                     return;
                  }

                  if (this.finish()) {
                     return;
                  }
               } else if (this.find()) {
                  return;
               }
            } else {
               Iterator<ICrossRecipeMachine.Thread> it = this.threads.iterator();

               while (it.hasNext()) {
                  ICrossRecipeMachine.Thread thread = it.next();
                  if (thread.tick()) {
                     it.remove();
                     this.machine.getCrossRecipeTrait().lastRecipes.remove(thread.recipe.definition);
                     this.addResearchData(this.machine, thread.recipe, thread.recipe.definition);
                     this.machine.handleRecipeOutput(thread.recipe);
                  }
               }

               if (this.shouldSearch && !this.suspendAfterFinish && this.getMachine().getOffsetTimer() % this.searchInterval == 0) {
                  if (this.findThread()) {
                     return;
                  }

                  if (this.searchInterval < 80) {
                     this.searchInterval <<= 1;
                  }
               }

               if (!this.threads.isEmpty()) {
                  this.setStatus(1);
                  return;
               }

               if (this.onThreadFinish()) {
                  return;
               }
            }

            if (this.interval < SEARCH_MAX_INTERVAL) {
               this.interval <<= 1;
               if (this.subscription != null) {
                  this.subscription.cycle = this.interval;
               }
            }

            this.unsubscribe();
         }
      }

      @Override
      public void resetRecipeLogic() {
         super.resetRecipeLogic();
         this.threads.clear();
      }

      private void setup(@NotNull GTRecipe recipe) {
         this.progress = 0;
         this.interval = 5;
         this.lastRecipe = recipe;
         this.setStatus(1);
         this.duration = recipe.duration;
         if (this.subscription != null) {
            this.subscription.cycle = 0;
         }

         this.isActive = true;
      }

      private boolean find() {
         this.lastRecipe = null;
         this.markLastRecipeDirty();
         if (this.machine.hasCapabilityProxies()) {
            if (this.machine.isIndependentThread()) {
               return this.findThread();
            }

            GTRecipe match = this.machine.getCrossRecipeTrait().getRecipe();
            if (match != null) {
               this.setup(match);
               return true;
            }
         }

         return false;
      }

      private boolean findThread() {
         IWirelessContainer container = this.machine.getWirelessContainer();
         if (container != null) {
            CrossRecipeTrait trait = this.machine.getCrossRecipeTrait();
            trait.availableThread = this.machine.getThread() - ICrossRecipeMachine.Thread.size(this.threads);
            if (trait.availableThread > 0) {
               trait.isSeparateThread = true;
               trait.duplicateCheck = !this.machine.isRepeatedRecipes();
               trait.maxParallel = this.machine.getParallel();
               boolean add = false;

               do {
                  trait.availableParallel = trait.maxParallel * trait.availableThread;
                  GTRecipe recipe = trait.lookupRecipe((u, r) -> this.consumeWireless(trait, r, container));
                  if (recipe == null) {
                     this.shouldSearch = false;
                     break;
                  }

                  trait.availableThread = trait.availableThread - trait.useThread;
                  this.threads.addLast(new ICrossRecipeMachine.Thread(recipe, trait.useThread));
                  add = true;
               } while (trait.availableThread >= 1);

               if (add) {
                  this.interval = 5;
                  this.searchInterval = 20;
                  this.setStatus(1);
                  if (this.subscription != null) {
                     this.subscription.cycle = 0;
                  }

                  this.isActive = true;
                  return true;
               }
            }
         }

         return false;
      }

      private boolean consumeWireless(CrossRecipeTrait trait, GTRecipe recipe, IWirelessContainer container) {
         long storage = this.machine.getRecipeUnit(recipe);
         if (storage < 1L) {
            return true;
         } else {
            BigInteger consume = BigInteger.valueOf(storage).multiply(BigInteger.valueOf((long)recipe.duration * trait.useThread));
            if (container.getStorage().compareTo(consume) < 0) {
               this.machine
                  .setIdleReason(
                     IdleReason.AMOUNT_DETAILED,
                     container.getUnit(),
                     NumberUtils.formatDouble(consume.doubleValue()),
                     NumberUtils.formatDouble(container.getStorage().doubleValue())
                  );
               return false;
            } else {
               container.unrestrictedRemoveStorage(consume);
               return true;
            }
         }
      }

      private boolean onThreadFinish() {
         this.machine.getCrossRecipeTrait().lastRecipes.clear();
         this.machine.afterWorking();
         if (this.suspendAfterFinish) {
            this.setStatus(3);
            this.suspendAfterFinish = false;
         } else {
            if (this.machine.isIndependentThread() && this.findThread()) {
               return true;
            }

            this.setStatus(0);
         }

         this.isActive = false;
         return false;
      }

      private boolean finish() {
         if (this.lastRecipe != null) {
            for (GTRecipeDefinition lastDefinition : this.machine.getCrossRecipeTrait().lastRecipes) {
               this.addResearchData(this.machine, this.lastRecipe, lastDefinition);
            }
         }

         this.machine.afterWorking();
         if (this.lastRecipe != null) {
            this.machine.handleRecipeOutput(this.lastRecipe);
         }

         if (this.suspendAfterFinish) {
            this.setStatus(3);
            this.suspendAfterFinish = false;
         } else {
            if (this.find()) {
               return true;
            }

            this.setStatus(0);
         }

         this.progress = 0;
         this.duration = 0;
         this.isActive = false;
         return false;
      }
   }

   class Thread {
      public static final DataCodec<ICrossRecipeMachine.Thread> DATA_CODECS = new DataCodec<ICrossRecipeMachine.Thread>() {
         @NotNull
         public Data encode(ICrossRecipeMachine.Thread obj) {
            ListData list = new ListData();
            list.add(GTRecipe.DATA_CODEC, obj.recipe);
            list.addInt(obj.progress);
            list.addInt(obj.use);
            return list;
         }

         public ICrossRecipeMachine.Thread decode(@NotNull Data data, int dataVersion) {
            ListData list = data.asListData();
            ICrossRecipeMachine.Thread t = new ICrossRecipeMachine.Thread(list.get(0, GTRecipe.DATA_CODEC, dataVersion), list.getInt(2));
            t.progress = list.getInt(1);
            return t;
         }
      };
      protected int progress;
      protected final GTRecipe recipe;
      protected final int duration;
      protected final int use;

      private Thread(GTRecipe recipe, int use) {
         this.recipe = recipe;
         this.duration = recipe.duration;
         this.use = use;
      }

      private boolean tick() {
         if (this.progress < this.duration) {
            this.progress++;
            return false;
         } else {
            return true;
         }
      }

      public double getProgressPercent() {
         return this.duration == 0 ? 0.0 : this.progress / (this.duration * 1.0);
      }

      private static int size(ArrayList<ICrossRecipeMachine.Thread> threads) {
         int size = 0;

         for (ICrossRecipeMachine.Thread thread : threads) {
            size += thread.use;
         }

         return size;
      }

      public static String getID(IntHolder holder, int use) {
         StringBuilder sb = new StringBuilder();
         sb.append(holder.value++);
         int last = 0;

         for (int i = 1; i < use; i++) {
            last = holder.value++;
         }

         if (last > 0) {
            sb.append("-").append(last);
         }

         return sb.toString();
      }

      @Generated
      public int getProgress() {
         return this.progress;
      }

      @Generated
      public GTRecipe getRecipe() {
         return this.recipe;
      }

      @Generated
      public int getDuration() {
         return this.duration;
      }

      @Generated
      public int getUse() {
         return this.use;
      }
   }
}
