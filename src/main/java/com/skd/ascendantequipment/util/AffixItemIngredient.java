package com.skd.ascendantequipment.util;

import com.mojang.serialization.MapCodec;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.commontoolkit.dynreg.DynamicHolder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.stream.Stream;

public class AffixItemIngredient implements ICustomIngredient {
    public static final MapCodec<AffixItemIngredient> CODEC = RarityRegistry.INSTANCE.holderCodec().fieldOf("rarity").xmap(AffixItemIngredient::new, a -> a.rarity);
    public static final StreamCodec<ByteBuf, AffixItemIngredient> STREAM_CODEC = RarityRegistry.INSTANCE.holderStreamCodec().map(AffixItemIngredient::new, a -> a.rarity);
    public static final IngredientType<AffixItemIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

    protected final DynamicHolder<LootRarity> rarity;

    public AffixItemIngredient(DynamicHolder<LootRarity> rarity) {
        this.rarity = rarity;
    }

    @Override
    public boolean test(ItemStack stack) {
        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(stack);
        Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        return affixes.size() > 0 && rarity.isBound() && rarity == this.rarity;
    }

    @Override
    public Stream<Holder<Item>> items() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        FeatureFlagSet flags = server == null ? FeatureFlags.VANILLA_SET : server.getWorldData().enabledFeatures();
        return BuiltInRegistries.ITEM.listElements().<Holder<Item>>map(i -> i).filter(i -> i.value().isEnabled(flags) && i.value() != Items.AIR);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    public LootRarity getRarity() {
        return this.rarity.get();
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

    @Override
    public SlotDisplay display() {
        return new AffixItemSlotDisplay(this.rarity);
    }
}
