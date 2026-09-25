package com.gtolib.api.player;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.crafting.CraftingLink;
import com.google.common.collect.ImmutableSet;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gtocore.common.item.armor.SpaceArmorComponentItem;
import com.gtocore.mixin.ae2.wtlib.CraftingTerminalHandlerAccessor;
import com.gtocore.utils.OrganUtilsKt;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.player.attribute.PlayerAttributes;
import com.gtolib.utils.ItemUtils;
import de.mari_023.ae2wtlib.wct.CraftingTerminalHandler;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public interface IEnhancedPlayer extends ICraftingRequester {
   IOrganService organService = new OrganService();

   PlayerData getPlayerData();

   static IEnhancedPlayer of(Player player) {
      return (IEnhancedPlayer)player;
   }

   static boolean spaceTick(ServerLevel level, LivingEntity entity) {
      if (entity instanceof IEnhancedPlayer player) {
         if (player.getPlayerData().getPlayerAttributes().getBooleanCurrent(PlayerAttributes.SPACE_STATE)) {
            return false;
         }

         ItemStack chestplate = ((Player)entity).getInventory().getArmor(2);
         if (chestplate.getItem() instanceof SpaceArmorComponentItem && SpaceArmorComponentItem.hasOxygen(entity)) {
            for (ItemStack a : entity.getArmorSlots()) {
               if (!(a.getItem() instanceof ArmorComponentItem item)
                  || !ItemUtils.getIdLocation(item).getPath().contains("nanomuscle") && !ItemUtils.getIdLocation(item).getPath().contains("quarktech")) {
                  return true;
               }
            }

            return false;
         } else {
            return !player.getPlayerData().getPlayerAttributes().getBooleanCurrent(PlayerAttributes.WARDEN_STATE)
               || level.dimension() != GTODimensions.OTHERSIDE;
         }
      } else {
         return true;
      }
   }

   @Override
   default ImmutableSet<ICraftingLink> getRequestedJobs() {
      return ImmutableSet.copyOf(this.getPlayerData().craftingLinks);
   }

   @Override
   default long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
      return 0L;
   }

   @Override
   default void jobStateChange(ICraftingLink link) {
      if (link.isCanceled() || link.isDone()) {
         this.getPlayerData().craftingLinks.remove((CraftingLink)link);
      }
   }

   @Nullable
   @Override
   default IGridNode getActionableNode() {
      if (this instanceof ServerPlayer player) {
         CraftingTerminalHandler cTHandler = CraftingTerminalHandler.getCraftingTerminalHandler(player);
         return cTHandler.getLocator() != null && cTHandler.getTargetGrid() != null
            ? ((CraftingTerminalHandlerAccessor)cTHandler).gto$getMenuHost().getActionableNode()
            : null;
      } else {
         return null;
      }
   }

   static float gravity(Entity entity, float gravity) {
      return entity instanceof IEnhancedPlayer player && player.getPlayerData().getPlayerAttributes().getBooleanCurrent(PlayerAttributes.NO_GRAVITY)
         ? 0.0F
         : gravity;
   }

   static void onTick(ServerPlayer player) {
      if (OrganUtilsKt.getSetOrganTier(of(player).getPlayerData()) < 4) {
         IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
         if (tracker != null) {
            IItemHandler inventory = player.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
            if (inventory == null) {
               return;
            }

            tracker.tick();

            for (int i = 0; i < inventory.getSlots(); i++) {
               ItemStack stack = inventory.getStackInSlot(i);
               Material material = HazardProperty.getValidHazardMaterial(stack);
               if (!material.isNull() && material.hasProperty(PropertyKey.HAZARD)) {
                  HazardProperty property = material.getProperty(PropertyKey.HAZARD);
                  if (property.hazardTrigger.protectionType().isProtected(player)) {
                     property.hazardTrigger.protectionType().damageEquipment(player, 1);
                  } else {
                     tracker.progressRelatedCondition(material);
                  }
               }
            }
         }
      }
   }

   @Nullable
   static IStorageService getMEStorageService(ServerPlayer player) {
      CraftingTerminalHandler cTHandler = CraftingTerminalHandler.getCraftingTerminalHandler(player);
      return cTHandler.getLocator() != null && cTHandler.getTargetGrid() != null && cTHandler.getTargetGrid().getStorageService() != null
         ? cTHandler.getTargetGrid().getStorageService()
         : null;
   }

   static long getClientAEAmount(Player player, AEKey key) {
      return of(player).getPlayerData().getMeStorageInfoManager().wrapper.getAEKeyAvailableAmount(key);
   }

   static MutableComponent getClientAEStatusText(Player player, AEKey key, AmountFormat format) {
      return of(player).getPlayerData().getMeStorageInfoManager().wrapper.getAEKeyStatusText(key, format);
   }

   static boolean isClientAEReachable(Player player) {
      return of(player).getPlayerData().getMeStorageInfoManager().wrapper.isReachable();
   }

   static void fetchClientAEData(Player player, AEKey key) {
      of(player).getPlayerData().getMeStorageInfoManager().fetchAEKey(key);
   }
}
