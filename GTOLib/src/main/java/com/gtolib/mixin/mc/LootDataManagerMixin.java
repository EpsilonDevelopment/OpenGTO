package com.gtolib.mixin.mc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.gtocore.common.data.GTOLoots;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LootDataManager.class, priority = 0)
public class LootDataManagerMixin {
   @Shadow
   private Multimap<LootDataType<?>, ResourceLocation> typeKeys;
   @Shadow
   private Map<LootDataId<?>, ?> elements;

   @Inject(method = "scheduleElementParse", at = @At("HEAD"), cancellable = true)
   private static <T> void scheduleElementParse(
      LootDataType<T> var0,
      ResourceManager var1,
      Executor var2,
      Map<LootDataType<?>, Map<ResourceLocation, ?>> var3,
      CallbackInfoReturnable<CompletableFuture<?>> var4
   ) {
      if (GTOLoots.cache) {
         var4.setReturnValue(CompletableFuture.runAsync(() -> {}, var2));
      }
   }

   @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
   private void apply(Map<LootDataType<?>, Map<ResourceLocation, ?>> var1, CallbackInfo var2) {
      var2.cancel();
      if (!GTOLoots.cache) {
         GTOLoots.cache = true;
         Pair var3 = GTOLoots.apply(var1);
         GTOLoots.ELEMENTS_CACHE = (ImmutableMap)var3.getFirst();
         GTOLoots.TYPEKEYS_CACHE = (ImmutableMultimap)var3.getSecond();
      }

      this.elements = GTOLoots.ELEMENTS_CACHE;
      this.typeKeys = GTOLoots.TYPEKEYS_CACHE;
   }
}
