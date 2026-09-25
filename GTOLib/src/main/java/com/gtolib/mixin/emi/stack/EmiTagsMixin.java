package com.gtolib.mixin.emi.stack;

import dev.emi.emi.api.stack.EmiRegistryAdapter;
import dev.emi.emi.registry.EmiTags;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiTags.class, remap = false)
public class EmiTagsMixin {
   @Mutable
   @Shadow
   @Final
   public static Map<Registry<?>, EmiRegistryAdapter<?>> ADAPTERS_BY_REGISTRY;
   @Mutable
   @Shadow
   @Final
   public static Map<TagKey<?>, ResourceLocation> MODELED_TAGS;
   @Mutable
   @Shadow
   @Final
   private static Map<TagKey<?>, List<?>> TAG_VALUES;

   @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Ldev/emi/emi/util/InheritanceMap;<init>(Ljava/util/Map;)V"))
   private static Map map(Map var0) {
      return new Reference2ObjectOpenHashMap();
   }

   @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
   private static void init(CallbackInfo var0) {
      ADAPTERS_BY_REGISTRY = new Reference2ObjectOpenHashMap<>();
      MODELED_TAGS = new Reference2ObjectOpenHashMap<>();
      TAG_VALUES = new Reference2ObjectOpenHashMap<>();
   }
}
