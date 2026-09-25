package com.gtolib.mixin.apotheosis;

import com.mojang.authlib.GameProfile;
import dev.shadowsoffire.apotheosis.util.BlockUtil;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockUtil.class)
public class BlockUtilMixin {
   @Overwrite(remap = false)
   public static boolean breakExtraBlock(ServerLevel var0, BlockPos var1, ItemStack var2, @Nullable UUID var3) {
      BlockState var4 = var0.getBlockState(var1);
      Player var6 = null;
      FakePlayer var5;
      if (var3 != null) {
         var5 = FakePlayerFactory.get(var0, new GameProfile(var3, UsernameCache.getLastKnownUsername(var3)));
         var6 = var0.getPlayerByUUID(var3);
         if (var6 != null) {
            var5.setPos(var6.position());
         }
      } else {
         var5 = FakePlayerFactory.getMinecraft(var0);
      }

      var5.getInventory().items.set(var5.getInventory().selected, var2);
      if (!(var4.getDestroySpeed(var0, var1) < 0.0F) && var4.canHarvestBlock(var0, var1, var5)) {
         GameType var7 = var5.getAbilities().instabuild ? GameType.CREATIVE : GameType.SURVIVAL;
         int var8 = ForgeHooks.onBlockBreakEvent(var0, var7, var5, var1);
         if (var8 == -1) {
            return false;
         }

         BlockEntity var9 = var0.getBlockEntity(var1);
         Block var10 = var4.getBlock();
         if ((var10 instanceof CommandBlock || var10 instanceof StructureBlock || var10 instanceof JigsawBlock) && !var5.canUseGameMasterBlocks()) {
            var0.sendBlockUpdated(var1, var4, var4, 3);
            return false;
         }

         if (var5.getMainHandItem().onBlockStartBreak(var1, var5)) {
            return false;
         }

         if (var5.blockActionRestricted(var0, var1, var7)) {
            return false;
         }

         if (var5.getAbilities().instabuild) {
            BlockUtil.removeBlock(var0, var5, var1, false);
            return true;
         }

         ItemStack var11 = var5.getMainHandItem();
         ItemStack var12 = var11.copy();
         boolean var13 = var4.canHarvestBlock(var0, var1, var5);
         var11.mineBlock(var0, var4, var1, var5);
         if (var11.isEmpty() && !var12.isEmpty()) {
            ForgeEventFactory.onPlayerDestroyItem(var5, var12, InteractionHand.MAIN_HAND);
         }

         boolean var14 = BlockUtil.removeBlock(var0, var5, var1, var13);
         if (var14 && var13) {
            var10.playerDestroy(var0, var5, var1, var4, var9, var11);
         }

         gtolib$dropOrgiveBackItems(var5, (ServerPlayer)var6, var0, var1, var11);
         if (var14 && var8 > 0) {
            var4.getBlock().popExperience(var0, var1, var8);
         }

         return true;
      } else {
         return false;
      }
   }

   @Unique
   private static void gtolib$dropOrgiveBackItems(FakePlayer var0, @Nullable ServerPlayer var1, ServerLevel var2, BlockPos var3, ItemStack var4) {
      ArrayList<ItemEntity> var5 = new ArrayList<>();
      if (var1 != null) {
         for (ItemStack var7 : var0.getInventory().items) {
            if (var7 != null && !var7.isEmpty() && !var7.equals(var4)) {
               if (var1.getInventory().add(var7)) {
                  var0.getInventory().removeItem(var7);
               } else {
                  var5.add(new ItemEntity(var2, 0.0, 0.0, 0.0, var7));
               }
            }
         }
      } else {
         for (ItemStack var9 : var0.getInventory().items) {
            if (var9 != null && !var9.isEmpty() && !var9.equals(var4)) {
               var5.add(new ItemEntity(var2, 0.0, 0.0, 0.0, var9));
            }
         }
      }

      var5.stream().filter(Objects::nonNull).forEach(var2x -> {
         var2x.setNoPickUpDelay();
         var2x.setPos(var3.getX() + 0.5, var3.getY() + 0.5, var3.getZ() + 0.5);
         var2.addFreshEntity(var2x);
      });
   }
}
