package com.gtolib.mixin.lowdraglib;

import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.FieldUpdateSubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedKey;
import com.lowdragmc.lowdraglib.syncdata.managed.IRef;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.List;
import java.util.Map;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FieldManagedStorage.class)
public class FieldManagedStorageMixin {
   @Mutable
   @Shadow(remap = false)
   @Final
   private Map<ManagedKey, List<FieldUpdateSubscription>> listeners;
   @Shadow(remap = false)
   private IRef[] syncFields;

   @Inject(method = "<init>", at = @At("TAIL"), remap = false)
   private void init(IManaged var1, CallbackInfo var2) {
      this.listeners = new Reference2ReferenceOpenHashMap<>();
   }

   @Overwrite(remap = false)
   public boolean hasDirtySyncFields() {
      for (IRef var4 : this.syncFields) {
         if (var4.isSyncDirty()) {
            return true;
         }
      }

      return false;
   }

   @Overwrite(remap = false)
   public boolean hasDirtyPersistedFields() {
      return false;
   }
}
