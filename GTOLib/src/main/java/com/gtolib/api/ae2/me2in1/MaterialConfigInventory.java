package com.gtolib.api.ae2.me2in1;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.AEKeyFilter;
import appeng.helpers.externalstorage.GenericStackInv.Mode;
import appeng.util.ConfigInventory;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gtolib.utils.AEChemicalHelper;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import java.util.Arrays;
import java.util.Set;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaterialConfigInventory extends ConfigInventory {
   protected final Material[] materials;

   protected MaterialConfigInventory(@Nullable AEKeyFilter filter, Mode mode, int size, @Nullable Runnable listener, boolean allowOverstacking) {
      super(filter, mode, size, listener, allowOverstacking);
      this.materials = new Material[size];

      for (int i = 0; i < size; i++) {
         this.materials[i] = GTMaterials.NULL;
      }
   }

   public static MaterialConfigInventory create(int size, @Nullable Runnable listener) {
      return new MaterialConfigInventory(null, Mode.CONFIG_TYPES, size, listener, true);
   }

   @NotNull
   public Material getMaterial(int slot) {
      return slot >= 0 && slot < this.materials.length ? this.materials[slot] : GTMaterials.NULL;
   }

   @Override
   public void clear() {
      super.clear();
      Arrays.fill(this.materials, GTMaterials.NULL);
   }

   public Set<Material> materialSet() {
      ReferenceLinkedOpenHashSet<Material> result = new ReferenceLinkedOpenHashSet<>();

      for (int i = 0; i < this.stacks.length; i++) {
         Material mat = this.getMaterial(i);
         if (mat != GTMaterials.NULL) {
            result.add(mat);
         }
      }

      return result;
   }

   @Override
   public void setStack(int slot, @Nullable GenericStack stack) {
      if (stack != null) {
         if (!this.isAllowedIn(stack.what())) {
            return;
         }

         Material Material = AEChemicalHelper.getMaterial(stack.what());
         stack = new GenericStack(stack.what(), 0L);
         this.materials[slot] = Material;
      } else {
         this.materials[slot] = GTMaterials.NULL;
      }

      super.setStack(slot, stack);
   }

   public boolean isAllowedIn(AEKey what) {
      return AEChemicalHelper.getMaterial(what) != GTMaterials.NULL;
   }

   @Override
   public void readFromTag(ListTag tag) {
      super.readFromTag(tag);

      for (int i = 0; i < this.size(); i++) {
         GenericStack stack = this.getStack(i);
         if (stack != null) {
            Material material = AEChemicalHelper.getMaterial(stack.what());
            this.materials[i] = material;
         } else {
            this.materials[i] = GTMaterials.NULL;
         }
      }
   }
}
