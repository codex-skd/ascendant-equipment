package com.skd.ascendantequipment.attachments;

import com.mojang.serialization.Codec;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;

public record BonusLootTables(List<ResourceKey<LootTable>> tables) {
    public static final BonusLootTables EMPTY = new BonusLootTables(List.of());
    public static final Codec<BonusLootTables> CODEC = ResourceKey.codec(Registries.LOOT_TABLE).listOf().xmap(BonusLootTables::new, BonusLootTables::tables);

    public void drop(Mob owner, DamageSource source, boolean hitByPlayer) {
        ServerLevel serverLevel = (ServerLevel) owner.level();

        for (ResourceKey<LootTable> key : this.tables) {
            LootTable table = serverLevel.getServer().reloadableRegistries().getLootTable(key);
            if (table != LootTable.EMPTY) {
                Builder builder = new Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, owner)
                    .withParameter(LootContextParams.ORIGIN, owner.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                    .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
                    .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());
                if (hitByPlayer && owner.lastHurtByPlayer != null) {
                    builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, owner.lastHurtByPlayer).withLuck(owner.lastHurtByPlayer.getLuck());
                }

                LootParams lootparams = builder.create(LootContextParamSets.ENTITY);
                table.getRandomItems(lootparams, owner.getLootTableSeed(), owner::spawnAtLocation);
            }
        }
    }

    public boolean isEmpty() {
        return this.tables.isEmpty();
    }

    public BonusLootTables mergeWith(BonusLootTables other) {
        List<ResourceKey<LootTable>> combined = new ArrayList<>();
        combined.addAll(this.tables);
        combined.addAll(other.tables);
        return new BonusLootTables(combined);
    }
}
