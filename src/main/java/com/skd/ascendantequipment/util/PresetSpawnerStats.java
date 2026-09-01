package com.skd.ascendantequipment.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.skd.ascendantspawners.block.AscendantSpawnerTile;
import com.skd.ascendantspawners.stats.SpawnerStat;
import com.skd.ascendantspawners.stats.SpawnerStats;

import java.util.Map;
import java.util.Map.Entry;

public record PresetSpawnerStats(Map<SpawnerStat<?>, Object> stats) {
    public static final Codec<PresetSpawnerStats> CODEC = Codec.<SpawnerStat<?>, Object>dispatchedMap(SpawnerStats.REGISTRY.byNameCodec(), SpawnerStat::getValueCodec).xmap(PresetSpawnerStats::new, PresetSpawnerStats::stats);

    private static Map<SpawnerStat<?>, Object> DEFAULT_STATS = ImmutableMap.<SpawnerStat<?>, Object>builder()
        .put(SpawnerStats.MIN_DELAY, 200)
        .put(SpawnerStats.MAX_DELAY, 800)
        .put(SpawnerStats.SPAWN_COUNT, 4)
        .put(SpawnerStats.MAX_NEARBY_ENTITIES, 6)
        .put(SpawnerStats.SPAWN_RANGE, 4)
        .put(SpawnerStats.REQ_PLAYER_RANGE, 16)
        .build();

    public PresetSpawnerStats() {
        this(DEFAULT_STATS);
    }

    public void apply(AscendantSpawnerTile entity) {
        for (Entry<SpawnerStat<?>, Object> entry : this.stats.entrySet()) {
            SpawnerStat stat = entry.getKey();
            Object value = entry.getValue();
            stat.setValue(entity, value);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected final ImmutableMap.Builder<SpawnerStat<?>, Object> stats = ImmutableMap.builder();

        public <T> Builder stat(SpawnerStat<T> stat, T value) {
            this.stats.put(stat, value);
            return this;
        }

        public PresetSpawnerStats build() {
            return new PresetSpawnerStats(this.stats.build());
        }
    }
}
