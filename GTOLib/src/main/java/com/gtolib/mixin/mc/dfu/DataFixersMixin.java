package com.gtolib.mixin.mc.dfu;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.serialization.Dynamic;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DataFixers.class, priority = 100)
public class DataFixersMixin {
   @Unique
   private static final Schema gtolib$SCHEMA = new Schema(SharedConstants.getCurrentVersion().getDataVersion().getVersion(), null) {
      @Override
      public Set<String> types() {
         return Collections.emptySet();
      }

      @Override
      public Type<?> getType(TypeReference var1) {
         return null;
      }

      @Override
      public Type<?> getChoiceType(TypeReference var1, String var2) {
         return null;
      }

      @Override
      public Type<?> getTypeRaw(TypeReference var1) {
         return null;
      }

      @Override
      public TaggedChoiceType<?> findChoiceType(TypeReference var1) {
         return null;
      }

      @Override
      public Map<String, Supplier<TypeTemplate>> registerEntities(Schema var1) {
         return Collections.emptyMap();
      }

      @Override
      public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema var1) {
         return Collections.emptyMap();
      }

      @Override
      public void registerTypes(Schema var1, Map<String, Supplier<TypeTemplate>> var2, Map<String, Supplier<TypeTemplate>> var3) {
      }

      @Override
      protected Map<String, Type<?>> buildTypes() {
         return Collections.emptyMap();
      }
   };

   @Inject(method = "createFixerUpper", at = @At("HEAD"), cancellable = true)
   private static synchronized void createFixerUpper(Set<TypeReference> var0, CallbackInfoReturnable<DataFixer> var1) {
      var1.setReturnValue(new DataFixer() {
         @Override
         public <T> Dynamic<T> update(TypeReference var1, Dynamic<T> var2, int var3, int var4) {
            return var2;
         }

         @Override
         public Schema getSchema(int var1) {
            return DataFixersMixin.gtolib$SCHEMA;
         }
      });
   }
}
