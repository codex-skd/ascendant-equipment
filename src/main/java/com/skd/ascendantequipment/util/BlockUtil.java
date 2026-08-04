package com.skd.ascendantequipment.util;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.UsernameCache;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class BlockUtil {

    public static boolean breakExtraBlock(ServerLevel level, BlockPos pos, ItemStack mainhand, @Nullable UUID source) {
        BlockState state = level.getBlockState(pos);
        FakePlayer player;
        if (source != null) {
            player = FakePlayerFactory.get(level, new GameProfile(source, UsernameCache.getLastKnownUsername(source)));
            Player realPlayer = level.getPlayerByUUID(source);
            if (realPlayer != null) {
                player.setPos(realPlayer.position());
            }
        }
        else {
            player = FakePlayerFactory.getMinecraft(level);
        }

        player.getInventory().setItem(player.getInventory().getSelectedSlot(), mainhand);
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        if (!(state.getDestroySpeed(level, pos) < 0.0F) && state.canHarvestBlock(level, pos, player)) {
            GameType type = player.getAbilities().instabuild ? GameType.CREATIVE : GameType.SURVIVAL;
            BreakBlockEvent event = CommonHooks.fireBlockBreak(level, type, player, pos, state);
            if (event.isCanceled()) {
                return false;
            }

            BlockEntity tile = level.getBlockEntity(pos);
            Block block = state.getBlock();
            if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
                level.sendBlockUpdated(pos, state, state, 3);
                return false;
            }

            if (player.blockActionRestricted(level, pos, type)) {
                return false;
            }

            BlockState newState = block.playerWillDestroy(level, pos, state, player);
            if (type.isCreative()) {
                removeBlock(level, player, pos, newState, false);
                return true;
            }

            ItemStack held = player.getMainHandItem();
            ItemStack heldCopy = held.copy();
            boolean canHarvest = newState.canHarvestBlock(level, pos, player);
            held.mineBlock(level, state, pos, player);
            boolean removed = removeBlock(level, player, pos, newState, canHarvest);
            if (removed && canHarvest) {
                block.playerDestroy(level, player, pos, newState, tile, heldCopy);
            }

            if (held.isEmpty() && !heldCopy.isEmpty()) {
                EventHooks.onPlayerDestroyItem(player, heldCopy, InteractionHand.MAIN_HAND);
            }

            return true;
        }
        return false;
    }

    public static boolean removeBlock(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, boolean canHarvest) {
        boolean removed = state.onDestroyedByPlayer(level, pos, player, player.getMainHandItem(), canHarvest, level.getFluidState(pos));
        if (removed) {
            state.getBlock().destroy(level, pos, state);
        }
        return removed;
    }
}
