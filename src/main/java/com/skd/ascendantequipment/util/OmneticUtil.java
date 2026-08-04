package com.skd.ascendantequipment.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.HarvestCheck;

import java.util.Arrays;

public class OmneticUtil {

    public static void applyOmneticData(BreakSpeed e, OmneticData data) {
        float speed = e.getOriginalSpeed();
        for (ItemStackTemplate template : data.items()) {
            speed = Math.max(getBaseSpeed(e.getEntity(), template.create(), e.getState(), e.getPosition().orElse(BlockPos.ZERO)), speed);
        }
        e.setNewSpeed(Math.max(speed, e.getNewSpeed()));
    }

    public static void applyOmneticData(HarvestCheck e, OmneticData data) {
        for (ItemStackTemplate template : data.items()) {
            if (template.create().isCorrectToolForDrops(e.getTargetBlock())) {
                e.setCanHarvest(true);
                break;
            }
        }
    }

    public static float getBaseSpeed(Player player, ItemStack tool, BlockState state, BlockPos pos) {
        float f = tool.getDestroySpeed(state);
        if (f > 1.0F) {
            f += (float) player.getAttributeValue(Attributes.MINING_EFFICIENCY);
        }

        if (MobEffectUtil.hasDigSpeed(player)) {
            f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
        }

        if (player.hasEffect(MobEffects.MINING_FATIGUE)) {
            f *= switch (player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
        }

        f *= (float) player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
        if (player.isEyeInFluid(FluidTags.WATER)) {
            f *= (float) player.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
        }

        if (!player.onGround()) {
            f /= 5.0F;
        }

        return f;
    }

    public record OmneticData(String name, ItemStackTemplate[] items) {
        public static Codec<OmneticData> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                Codec.STRING.fieldOf("name").forGetter(OmneticData::name),
                Codec.list(ItemStackTemplate.CODEC).xmap(l -> l.toArray(new ItemStackTemplate[0]), Arrays::asList).fieldOf("items").forGetter(OmneticData::items)
            )
                .apply(inst, OmneticData::new));
    }
}
