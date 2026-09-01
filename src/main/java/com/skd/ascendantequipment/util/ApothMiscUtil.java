package com.skd.ascendantequipment.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.serialization.JsonOps;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.commontoolkit.util.EnchantmentUtils;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Holder.Reference;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ApothMiscUtil {

    public static int getExpCostForSlot(int level, int slot) {
        int cost = 0;
        for (int i = 0; i <= slot; i++) {
            cost += EnchantmentUtils.getExperienceForLevel(level - i);
        }
        return cost - 1;
    }

    public static int[] doubleUpGradient(int[] data) {
        int[] out = new int[data.length * 2];
        System.arraycopy(data, 0, out, 0, data.length);
        for (int i = data.length - 1; i >= 0; i--) {
            out[data.length * 2 - 1 - i] = data[i];
        }
        return out;
    }

    @Nullable
    public static Player getClientPlayer() {
        return FMLEnvironment.dist.isClient() ? ClientInternal.getClientPlayer() : null;
    }

    public static double duraProd(double result, double element) {
        return result + (1.0 - result) * element;
    }

    @SafeVarargs
    public static <T> Set<T> linkedSet(T... objects) {
        LinkedHashSet<T> set = new LinkedHashSet<>();
        for (T t : objects) {
            set.add(t);
        }
        return set;
    }

    public static boolean hasAdvancement(Player player, ResourceLocation key) {
        if (player.level().isClientSide()) {
            return ClientInternal.hasAdvancment(key);
        }

        PlayerAdvancements advancements = ((ServerPlayer) player).getAdvancements();
        ServerAdvancementManager manager = player.level().getServer().getAdvancements();
        AdvancementHolder holder = manager.get(key);
        if (holder == null) {
            return false;
        }

        AdvancementProgress progress = advancements.progress.get(holder);
        return progress != null && progress.isDone();
    }

    public static <T> Reference<T> standaloneHolder(Provider registries, ResourceKey<T> key) {
        HolderOwner<T> owner = registries.createSerializationContext(JsonOps.INSTANCE).owner(key.registryKey()).get();
        return Reference.createStandAlone(owner, key);
    }

    public static MutableComponent dotPrefix(Component comp) {
        return AscendantEquipment.lang("text", "dot_prefix", comp);
    }

    public static MutableComponent starPrefix(Component comp) {
        return AscendantEquipment.lang("text", "star_prefix", comp);
    }

    public static <T> T getRandomElement(Collection<T> set, RandomSource rand) {
        int index = rand.nextInt(set.size());
        Iterator<T> iter = set.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        return iter.next();
    }

    public static class ClientInternal {
        public static Player getClientPlayer() {
            return Minecraft.getInstance().player;
        }

        public static boolean hasAdvancment(ResourceLocation key) {
            ClientAdvancements advancements = Minecraft.getInstance().getConnection().getAdvancements();
            AdvancementHolder holder = advancements.get(key);
            if (holder == null) {
                return false;
            }

            AdvancementProgress progress = advancements.progress.get(holder);
            return progress != null && progress.isDone();
        }

        public static boolean isKeyReallyDown(KeyMapping mapping) {
            Key key = mapping.getKey();
            if (key == InputConstants.UNKNOWN) {
                return false;
            }
            IKeyConflictContext context = mapping.getKeyConflictContext();
            if (context.isActive() && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.getValue())) {
                KeyModifier modifier = mapping.getKeyModifier();
                return modifier != KeyModifier.NONE ? modifier.isActive(context) : KeyModifier.isKeyCodeModifier(key) || modifier.isActive(context);
            }
            return false;
        }
    }
}
