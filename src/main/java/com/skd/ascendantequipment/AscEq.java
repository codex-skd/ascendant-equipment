package com.skd.ascendantequipment;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.skd.ascendantequipment.advancements.EquippedItemTrigger;
import com.skd.ascendantequipment.advancements.GemCutTrigger;
import com.skd.ascendantequipment.advancements.predicates.AffixItemPredicate;
import com.skd.ascendantequipment.advancements.predicates.InvaderPredicate;
import com.skd.ascendantequipment.advancements.predicates.MonsterPredicate;
import com.skd.ascendantequipment.advancements.predicates.PurityItemPredicate;
import com.skd.ascendantequipment.advancements.predicates.RarityItemPredicate;
import com.skd.ascendantequipment.advancements.predicates.SocketItemPredicate;
import com.skd.ascendantequipment.affix.ItemAffixes;
import com.skd.ascendantequipment.affix.UnnamingRecipe;
import com.skd.ascendantequipment.affix.augmenting.AugmentingMenu;
import com.skd.ascendantequipment.affix.augmenting.AugmentingTableBlock;
import com.skd.ascendantequipment.affix.augmenting.AugmentingTableTile;
import com.skd.ascendantequipment.affix.reforging.ReforgingMenu;
import com.skd.ascendantequipment.affix.reforging.ReforgingRecipe;
import com.skd.ascendantequipment.affix.reforging.ReforgingTableBlock;
import com.skd.ascendantequipment.affix.reforging.ReforgingTableBlockItem;
import com.skd.ascendantequipment.affix.reforging.ReforgingTableTile;
import com.skd.ascendantequipment.affix.salvaging.SalvageItem;
import com.skd.ascendantequipment.affix.salvaging.SalvagingMenu;
import com.skd.ascendantequipment.affix.salvaging.SalvagingRecipe;
import com.skd.ascendantequipment.affix.salvaging.SalvagingTableBlock;
import com.skd.ascendantequipment.affix.salvaging.SalvagingTableTile;
import com.skd.ascendantequipment.affix.trades.AutomaticAffixTrade;
import com.skd.ascendantequipment.attachments.BonusLootTables;
import com.skd.ascendantequipment.gen.BlacklistModifier;
import com.skd.ascendantequipment.gen.BossDungeonFeature;
import com.skd.ascendantequipment.gen.BossDungeonFeature2;
import com.skd.ascendantequipment.gen.ItemFrameGemsProcessor;
import com.skd.ascendantequipment.gen.RogueSpawnerFeature;
import com.skd.ascendantequipment.item.BossSummonerItem;
import com.skd.ascendantequipment.item.PotionCharmItem;
import com.skd.ascendantequipment.item.TooltipBlockItem;
import com.skd.ascendantequipment.item.TooltipItem;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.loot.conditions.KilledByRealPlayerCondition;
import com.skd.ascendantequipment.loot.conditions.MatchesBlockCondition;
import com.skd.ascendantequipment.loot.conditions.WorldTierCondition;
import com.skd.ascendantequipment.loot.entry.AffixLootPoolEntry;
import com.skd.ascendantequipment.loot.entry.GemLootPoolEntry;
import com.skd.ascendantequipment.loot.functions.ReforgeItemFunction;
import com.skd.ascendantequipment.loot.functions.TierGatedTrade;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import com.skd.ascendantequipment.loot.modifiers.AffixConvertLootModifier;
import com.skd.ascendantequipment.loot.modifiers.AffixHookLootModifier;
import com.skd.ascendantequipment.loot.modifiers.AffixLootModifier;
import com.skd.ascendantequipment.loot.modifiers.GemLootModifier;
import com.skd.ascendantequipment.mobs.BossSpawnerBlock;
import com.skd.ascendantequipment.mobs.InvaderSpawnRules;
import com.skd.ascendantequipment.particle.RarityParticleData;
import com.skd.ascendantequipment.recipe.CharmInfusionRecipe;
import com.skd.ascendantequipment.recipe.MaliceRecipe;
import com.skd.ascendantequipment.recipe.PotionCharmRecipe;
import com.skd.ascendantequipment.recipe.SupremacyRecipe;
import com.skd.ascendantequipment.socket.AddSocketsRecipe;
import com.skd.ascendantequipment.socket.SocketingRecipe;
import com.skd.ascendantequipment.socket.WithdrawalRecipe;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemItem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.cutting.BasicGemCuttingRecipe;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingBlock;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingMenu;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingRecipe;
import com.skd.ascendantequipment.socket.gem.cutting.PurityUpgradeRecipe;
import com.skd.ascendantequipment.socket.gem.storage.GemCaseBlock;
import com.skd.ascendantequipment.socket.gem.storage.GemCaseBlockItem;
import com.skd.ascendantequipment.socket.gem.storage.GemCaseMenu;
import com.skd.ascendantequipment.socket.gem.storage.GemCaseTile;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.ascendantequipment.util.AffixItemIngredient;
import com.skd.ascendantequipment.util.GemIngredient;
import com.skd.ascendantequipment.util.LootPatternMatcher;
import com.skd.ascendantequipment.util.RadialUtil;
import com.skd.ascendantequipment.util.SingletonRecipeSerializer;
import com.skd.ascendantequipment.util.SizedUpgradeRecipe;
import com.skd.ascendantequipment.util.SpawnEggIngredient;
import com.skd.ascendantattributes.api.AscendantAttributesObjects.EquipmentSlotGroups;
import com.skd.ascendantattributes.modifiers.EntitySlotGroup;
import com.skd.ascendantenchanting.objects.GlowyBlockItem.GlowyItem;
import com.skd.commontoolkit.block_entity.TickingBlockEntityType.TickSide;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.registry.DeferredHelper;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class AscEq {
   public static final DeferredHelper R = DeferredHelper.create("ascendant_equipment");

   public static void bootstrap(IEventBus bus) {
      bus.register(R);
      AscEq.BuiltInRegs.bootstrap();
      AscEq.Attachments.bootstrap();
      AscEq.Components.bootstrap();
      AscEq.Sounds.bootstrap();
      AscEq.Blocks.bootstrap();
      AscEq.Items.bootstrap();
      AscEq.Tiles.bootstrap();
      AscEq.Menus.bootstrap();
      AscEq.Tabs.bootstrap();
      AscEq.Triggers.bootstrap();
      AscEq.Features.bootstrap();
      AscEq.Ingredients.bootstrap();
       AscEq.RecipeTypes.bootstrap();
      AscEq.LootModifiers.bootstrap();
      AscEq.LootFunctions.bootstrap();
      AscEq.LootConditions.bootstrap();
      AscEq.LootPoolEntries.bootstrap();
      AscEq.RecipeSerializers.bootstrap();
      AscEq.DataComponentPredicates.bootstrap();
      AscEq.EntitySubPredicates.bootstrap();
      AscEq.Stats.bootstrap();
      AscEq.Particles.bootstrap();
      AscEq.LootCategories.bootstrap();
      AscEq.DataMaps.bootstrap();
      R.custom("blacklist", Keys.BIOME_MODIFIER_SERIALIZERS, BlacklistModifier.CODEC);
   }

   public static final class Advancements {
      public static final ResourceLocation WORLD_TIER_HAVEN = AscendantEquipment.loc("progression/haven");
      public static final ResourceLocation WORLD_TIER_FRONTIER = AscendantEquipment.loc("progression/frontier");
      public static final ResourceLocation WORLD_TIER_ASCENT = AscendantEquipment.loc("progression/ascent");
      public static final ResourceLocation WORLD_TIER_SUMMIT = AscendantEquipment.loc("progression/summit");
      public static final ResourceLocation WORLD_TIER_PINNACLE = AscendantEquipment.loc("progression/pinnacle");
   }

   public static final class Attachments {
      public static final AttachmentType<BonusLootTables> BONUS_LOOT_TABLES = AscEq.R
         .attachment(
            "bonus_loot_tables", () -> BonusLootTables.EMPTY, b -> b.serialize(BonusLootTables.CODEC, blt -> !blt.tables().isEmpty())
         );
      public static final AttachmentType<WorldTier> WORLD_TIER = AscEq.R
         .attachment("world_tier", () -> WorldTier.HAVEN, b -> b.serialize(WorldTier.CODEC).copyOnDeath().copyHandler((t, holder, prov) -> t));
      public static final AttachmentType<Boolean> TIER_AUGMENTS_APPLIED = AscEq.R
         .attachment("tier_augments_applied", () -> false, b -> b.serialize(Codec.BOOL));
      public static final AttachmentType<Float> COLD_DAMAGE_TAKEN = AscEq.R
         .attachment("cold_damage_taken", () -> 0.0F, b -> b.serialize(Codec.FLOAT));
      public static final AttachmentType<Long> INVADER_COOLDOWN = AscEq.R
         .attachment("invader_cooldown", () -> 0L, b -> b.serialize(Codec.LONG, t -> t != 0L).copyOnDeath());
      public static final AttachmentType<Boolean> AFFIX_EFFECT_RENDER_STARTED = AscEq.R
         .attachment("affix_effect_render_started", () -> false, UnaryOperator.identity());
      public static final AttachmentType<Integer> AFFIX_EFFECT_START_TIME = AscEq.R.attachment("affix_effect_start_time", () -> 0, UnaryOperator.identity());
      public static final AttachmentType<Integer> AFFIX_EFFECT_NEXT_PARTICLE_TIME = AscEq.R
         .attachment("affix_effect_next_particle_time", () -> 0, UnaryOperator.identity());
      public static final AttachmentType<RadialUtil.RadialState> RADIAL_MINING_MODE = AscEq.R
         .attachment(
            "radial_mining_mode",
            () -> RadialUtil.RadialState.REQUIRE_NOT_SNEAKING,
            b -> b.serialize(RadialUtil.RadialState.CODEC).copyOnDeath()
         );

      private static void bootstrap() {
      }
   }

   public static final class Blocks {
      public static final Holder<Block> BOSS_SPAWNER = AscEq.R
         .block("boss_spawner", BossSpawnerBlock::new, p -> p.requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable());
      public static final Holder<Block> SIMPLE_REFORGING_TABLE = AscEq.R
         .block("simple_reforging_table", ReforgingTableBlock::new, p -> p.requiresCorrectToolForDrops().strength(2.0F, 20.0F));
      public static final Holder<Block> REFORGING_TABLE = AscEq.R
         .block("reforging_table", ReforgingTableBlock::new, p -> p.requiresCorrectToolForDrops().strength(4.0F, 1000.0F));
      public static final Holder<Block> SALVAGING_TABLE = AscEq.R
         .block("salvaging_table", SalvagingTableBlock::new, p -> p.sound(SoundType.WOOD).strength(2.5F));
      public static final Holder<Block> GEM_CUTTING_TABLE = AscEq.R
         .block("gem_cutting_table", GemCuttingBlock::new, p -> p.sound(SoundType.WOOD).strength(2.5F));
      public static final Holder<Block> AUGMENTING_TABLE = AscEq.R
         .block("augmenting_table", AugmentingTableBlock::new, p -> p.requiresCorrectToolForDrops().strength(4.0F, 1000.0F));
      public static final Holder<Block> GEM_CASE = AscEq.R
         .block(
            "gem_case",
            p -> new GemCaseBlock(GemCaseTile.BasicGemCaseTile::new, p, 32767),
            p -> p.strength(5.0F, 1200.0F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 2)
         );
      public static final Holder<Block> ENDER_GEM_CASE = AscEq.R
         .block(
            "ender_gem_case",
            p -> new GemCaseBlock(GemCaseTile.EnderGemCaseTile::new, p, Integer.MAX_VALUE),
            p -> p.strength(5.0F, 1200.0F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 2)
         );

      private static void bootstrap() {
      }
   }

   public static final class BuiltInRegs {
      public static final Registry<LootCategory> LOOT_CATEGORY = AscEq.R
         .registry("loot_category", b -> b.defaultKey(AscendantEquipment.loc("none")).onBake(LootCategory.Inner.rebuildSortedValueList()).sync(true));

      private static void bootstrap() {
      }
   }

   public static final class Components {
      public static final DataComponentType<ItemAffixes> AFFIXES = AscEq.R
         .component("affixes", b -> b.persistent(ItemAffixes.CODEC).networkSynchronized(ItemAffixes.STREAM_CODEC));
      public static final DataComponentType<DynamicHolder<LootRarity>> RARITY = AscEq.R
         .component("rarity", b -> b.persistent(RarityRegistry.INSTANCE.holderCodec()).networkSynchronized(RarityRegistry.INSTANCE.holderStreamCodec()));
      public static final DataComponentType<Component> AFFIX_NAME = AscEq.R
         .component("affix_name", b -> b.persistent(ComponentSerialization.CODEC).networkSynchronized(ComponentSerialization.TRUSTED_STREAM_CODEC));
      public static final DataComponentType<Integer> SOCKETS = AscEq.R
         .component("sockets", b -> b.persistent(Codec.intRange(0, 16)).networkSynchronized(ByteBufCodecs.VAR_INT));
      public static final DataComponentType<ItemContainerContents> SOCKETED_GEMS = AscEq.R
         .component("socketed_gems", b -> b.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));
      public static final DataComponentType<DynamicHolder<Gem>> GEM = AscEq.R
         .component("gem", b -> b.persistent(GemRegistry.INSTANCE.holderCodec()).networkSynchronized(GemRegistry.INSTANCE.holderStreamCodec()));
      public static final DataComponentType<Purity> PURITY = AscEq.R
         .component("purity", b -> b.persistent(Purity.CODEC).networkSynchronized(Purity.STREAM_CODEC));
      public static final DataComponentType<Float> DURABILITY_BONUS = AscEq.R
         .component("durability_bonus", b -> b.persistent(Codec.floatRange(0.0F, 1.0F)).networkSynchronized(ByteBufCodecs.FLOAT));
      public static final DataComponentType<Boolean> FROM_CHEST = AscEq.R.component("from_chest", b -> b.persistent(Codec.BOOL));
      public static final DataComponentType<Boolean> FROM_TRADER = AscEq.R.component("from_trader", b -> b.persistent(Codec.BOOL));
      public static final DataComponentType<Boolean> FROM_BOSS = AscEq.R.component("from_boss", b -> b.persistent(Codec.BOOL));
      public static final DataComponentType<Boolean> FROM_MOB = AscEq.R.component("from_mob", b -> b.persistent(Codec.BOOL));
      public static final DataComponentType<Boolean> CHARM_ENABLED = AscEq.R.component("charm_enabled", b -> b.persistent(Codec.BOOL));
      public static final DataComponentType<Block> STONEFORMING_TARGET = AscEq.R
         .component(
            "stoneforming_target", b -> b.persistent(BuiltInRegistries.BLOCK.byNameCodec()).networkSynchronized(ByteBufCodecs.registry(Registries.BLOCK))
         );
      public static final DataComponentType<Boolean> MALICE_MARKER = AscEq.R
         .component("malice_marker", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
      public static final DataComponentType<Boolean> TOUCHED_BY_MALICE = AscEq.R
         .component("touched_by_malice", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
      public static final DataComponentType<Float> RENDER_ALPHA = AscEq.R
         .component("render_alpha", b -> b.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));

      private static void bootstrap() {
      }
   }

   public static final class DamageTypes {
      public static final ResourceKey<DamageType> EXECUTE = ResourceKey.create(Registries.DAMAGE_TYPE, AscendantEquipment.loc("execute"));
      public static final ResourceKey<DamageType> PSYCHIC = ResourceKey.create(Registries.DAMAGE_TYPE, AscendantEquipment.loc("psychic"));
   }

   public static final class DataComponentPredicates {
      public static final ItemSubPredicate.Type<AffixItemPredicate> AFFIXED_ITEM = AscEq.R.itemSubPredicate("affixed_item", AffixItemPredicate.CODEC);
      public static final ItemSubPredicate.Type<PurityItemPredicate> ITEM_WITH_PURITY = AscEq.R.itemSubPredicate("item_with_purity", PurityItemPredicate.CODEC);
      public static final ItemSubPredicate.Type<RarityItemPredicate> ITEM_WITH_RARITY = AscEq.R.itemSubPredicate("item_with_rarity", RarityItemPredicate.CODEC);
      public static final ItemSubPredicate.Type<SocketItemPredicate> SOCKETED_ITEM = AscEq.R.itemSubPredicate("socketed_item", SocketItemPredicate.CODEC);

      private static void bootstrap() {
      }
   }

   public static final class DataMaps {
      public static final DataMapType<DimensionType, InvaderSpawnRules> INVADER_SPAWN_RULES = AscEq.R
         .dataMap("invader_spawn_rules", Registries.DIMENSION_TYPE, InvaderSpawnRules.CODEC, UnaryOperator.identity());
      public static final DataMapType<Item, LootCategory> LOOT_CATEGORY_OVERRIDES = AscEq.R
         .dataMap("loot_category_overrides", Registries.ITEM, LootCategory.CODEC, c -> c.synced(LootCategory.CODEC, true));

      private static void bootstrap() {
      }
   }

   public static final class EntitySubPredicates {
      public static final MapCodec<MonsterPredicate> IS_MONSTER = AscEq.R
         .custom("is_monster", Registries.ENTITY_SUB_PREDICATE_TYPE, MonsterPredicate.CODEC);
      public static final MapCodec<InvaderPredicate> IS_INVADER = AscEq.R
         .custom("is_invader", Registries.ENTITY_SUB_PREDICATE_TYPE, InvaderPredicate.CODEC);

      private static void bootstrap() {
      }
   }

   public static class Features {
      public static final Holder<Feature<?>> BOSS_DUNGEON = AscEq.R.feature("boss_dungeon", BossDungeonFeature::new);
      public static final Holder<Feature<?>> BOSS_DUNGEON_2 = AscEq.R.feature("boss_dungeon_2", BossDungeonFeature2::new);
      public static final Holder<Feature<?>> ROGUE_SPAWNER = AscEq.R.feature("rogue_spawner", RogueSpawnerFeature::new);
      public static final StructureProcessorType<ItemFrameGemsProcessor> ITEM_FRAME_GEMS = AscEq.R
         .structureProcessor("item_frame_gems", ItemFrameGemsProcessor.CODEC);

      private static void bootstrap() {
      }
   }

   public static final class Ingredients {
      public static final IngredientType<AffixItemIngredient> AFFIX = AscEq.R.ingredient("affix", AffixItemIngredient.TYPE);
      public static final IngredientType<GemIngredient> GEM = AscEq.R.ingredient("gem", GemIngredient.TYPE);
      public static final IngredientType<SpawnEggIngredient> SPAWN_EGG = AscEq.R.ingredient("spawn_egg", SpawnEggIngredient.TYPE);

      private static void bootstrap() {
      }
   }

   public static final class Items extends net.minecraft.world.item.Items {
      public static final Holder<Item> MYSTERIOUS_SCRAP_METAL = rarityMat("mysterious_scrap_metal", "common");
      public static final Holder<Item> TIMEWORN_FABRIC = rarityMat("timeworn_fabric", "uncommon");
      public static final Holder<Item> LUMINOUS_CRYSTAL_SHARD = rarityMat("luminous_crystal_shard", "rare");
      public static final Holder<Item> ARCANE_SANDS = rarityMat("arcane_sands", "epic");
      public static final Holder<Item> GODFORGED_PEARL = rarityMat("godforged_pearl", "mythic");
      public static final Holder<Item> GOD_FUSED_PEARL = AscEq.R.item("god_fused_pearl", GlowyItem::new, p -> p.rarity(Rarity.EPIC));
      public static final Holder<Item> GEM_DUST = AscEq.R.item("gem_dust", Item::new);
      public static final Holder<Item> GEM_FUSED_SLATE = AscEq.R.item("gem_fused_slate", Item::new);
      public static final Holder<Item> SIGIL_OF_SOCKETING = AscEq.R.item("sigil_of_socketing", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> SIGIL_OF_WITHDRAWAL = AscEq.R.item("sigil_of_withdrawal", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> SIGIL_OF_REBIRTH = AscEq.R.item("sigil_of_rebirth", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> SIGIL_OF_ENHANCEMENT = AscEq.R.item("sigil_of_enhancement", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> SIGIL_OF_UNNAMING = AscEq.R.item("sigil_of_unnaming", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> SIGIL_OF_MALICE = AscEq.R
         .item(
            "sigil_of_malice",
            TooltipItem::new,
            p -> p.component(DataComponents.ITEM_NAME, AscendantEquipment.lang("item", "sigil_of_malice").withStyle(ChatFormatting.RED))
         );
      public static final Holder<Item> SIGIL_OF_SUPREMACY = AscEq.R
         .item(
            "sigil_of_supremacy",
            TooltipItem::new,
            p -> p.component(DataComponents.ITEM_NAME, AscendantEquipment.lang("item", "sigil_of_supremacy").withStyle(ChatFormatting.GOLD))
         );
      public static final Holder<Item> BOSS_SUMMONER = AscEq.R.item("boss_summoner", BossSummonerItem::new);
      public static final Holder<Item> SIMPLE_REFORGING_TABLE = AscEq.R
         .blockItem("simple_reforging_table", AscEq.Blocks.SIMPLE_REFORGING_TABLE, ReforgingTableBlockItem::new, UnaryOperator.identity());
      public static final Holder<Item> REFORGING_TABLE = AscEq.R
         .blockItem("reforging_table", AscEq.Blocks.REFORGING_TABLE, ReforgingTableBlockItem::new, p -> p.rarity(Rarity.EPIC));
      public static final Holder<Item> SALVAGING_TABLE = AscEq.R
         .blockItem("salvaging_table", AscEq.Blocks.SALVAGING_TABLE, TooltipBlockItem::new, UnaryOperator.identity());
      public static final Holder<Item> GEM_CUTTING_TABLE = AscEq.R
         .blockItem("gem_cutting_table", AscEq.Blocks.GEM_CUTTING_TABLE, TooltipBlockItem::new, UnaryOperator.identity());
      public static final Holder<Item> AUGMENTING_TABLE = AscEq.R
         .blockItem("augmenting_table", AscEq.Blocks.AUGMENTING_TABLE, TooltipBlockItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> GEM_CASE = AscEq.R.blockItem("gem_case", AscEq.Blocks.GEM_CASE, GemCaseBlockItem::new, UnaryOperator.identity());
      public static final Holder<Item> ENDER_GEM_CASE = AscEq.R
         .blockItem("ender_gem_case", AscEq.Blocks.ENDER_GEM_CASE, GemCaseBlockItem::new, UnaryOperator.identity());
      public static final Holder<Item> GEM = AscEq.R.item("gem", GemItem::new);
      public static final Holder<Item> POTION_CHARM = AscEq.R.item("potion_charm", PotionCharmItem::new);
      public static final Holder<Item> IRON_UPGRADE_SMITHING_TEMPLATE = AscEq.R
         .item("iron_upgrade_smithing_template", p -> createVanillaUpgradeTemplate("iron", p));
      public static final Holder<Item> GOLD_UPGRADE_SMITHING_TEMPLATE = AscEq.R
         .item("gold_upgrade_smithing_template", p -> createVanillaUpgradeTemplate("gold", p));
      public static final Holder<Item> DIAMOND_UPGRADE_SMITHING_TEMPLATE = AscEq.R
         .item("diamond_upgrade_smithing_template", p -> createVanillaUpgradeTemplate("diamond", p));
      public static final Holder<Item> MUSIC_DISC_FLASH = AscEq.R
         .item("music_disc_flash", Item::new, p -> p.rarity(Rarity.RARE).stacksTo(1).jukeboxPlayable(AscEq.Songs.FLASH));
      public static final Holder<Item> MUSIC_DISC_GLIMMER = AscEq.R
         .item("music_disc_glimmer", Item::new, p -> p.rarity(Rarity.RARE).stacksTo(1).jukeboxPlayable(AscEq.Songs.GLIMMER));
      public static final Holder<Item> MUSIC_DISC_SHIMMER = AscEq.R
         .item("music_disc_shimmer", Item::new, p -> p.rarity(Rarity.RARE).stacksTo(1).jukeboxPlayable(AscEq.Songs.SHIMMER));
      public static final Holder<Item> SPAWNER_CHAIN = AscEq.R.item("spawner_chain", TooltipItem::new);
      public static final Holder<Item> SPAWNER_RUNE = AscEq.R.item("spawner_rune", Item::new);
      public static final Holder<Item> INFUSED_SPAWNER_RUNE = AscEq.R.item("infused_spawner_rune", GlowyItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> FRONTIER_SPAWNER_UPGRADE_RUNE = AscEq.R
         .item("frontier_spawner_upgrade_rune", TooltipItem::new, p -> p.component(AscEq.Components.RARITY, rarity("uncommon")));
      public static final Holder<Item> ASCENT_SPAWNER_UPGRADE_RUNE = AscEq.R
         .item("ascent_spawner_upgrade_rune", TooltipItem::new, p -> p.component(AscEq.Components.RARITY, rarity("rare")));
      public static final Holder<Item> SUMMIT_SPAWNER_UPGRADE_RUNE = AscEq.R
         .item("summit_spawner_upgrade_rune", TooltipItem.GlowyTooltipItem::new, p -> p.component(AscEq.Components.RARITY, rarity("epic")));
      public static final Holder<Item> PINNACLE_SPAWNER_UPGRADE_RUNE = AscEq.R
         .item("pinnacle_spawner_upgrade_rune", TooltipItem.GlowyTooltipItem::new, p -> p.component(AscEq.Components.RARITY, rarity("mythic")));
      public static final Holder<Item> SPAWN_RANGE_SPAWNER_RUNE = AscEq.R.item("spawn_range_spawner_rune", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> REDSTONE_CONTROL_SPAWNER_RUNE = AscEq.R
         .item("redstone_control_spawner_rune", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> IGNORE_LIGHT_SPAWNER_RUNE = AscEq.R.item("ignore_light_spawner_rune", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> INITIAL_HEALTH_SPAWNER_RUNE = AscEq.R
         .item("initial_health_spawner_rune", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> SILENT_SPAWNER_RUNE = AscEq.R.item("silent_spawner_rune", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> YOUTHFUL_SPAWNER_RUNE = AscEq.R.item("youthful_spawner_rune", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> BURNING_SPAWNER_RUNE = AscEq.R.item("burning_spawner_rune", TooltipItem::new, p -> p.rarity(Rarity.UNCOMMON));
      public static final Holder<Item> NO_AI_SPAWNER_RUNE = AscEq.R.item("no_ai_spawner_rune", TooltipItem.GlowyTooltipItem::new, p -> p.rarity(Rarity.EPIC));
      public static final Holder<Item> IGNORE_CONDITIONS_SPAWNER_RUNE = AscEq.R
         .item("ignore_conditions_spawner_rune", TooltipItem.GlowyTooltipItem::new, p -> p.rarity(Rarity.EPIC));
      public static final Holder<Item> IGNORE_PLAYERS_SPAWNER_RUNE = AscEq.R
         .item("ignore_players_spawner_rune", TooltipItem.GlowyTooltipItem::new, p -> p.rarity(Rarity.EPIC));
      public static final Holder<Item> ECHOING_SPAWNER_RUNE = AscEq.R
         .item("echoing_spawner_rune", TooltipItem.GlowyTooltipItem::new, p -> p.rarity(Rarity.EPIC));

      private static Holder<Item> rarityMat(String registryName, String rarityId) {
         return AscEq.R.item(registryName, p -> new SalvageItem(RarityRegistry.INSTANCE.holder(AscendantEquipment.loc(rarityId)), p));
      }

      private static SmithingTemplateItem createVanillaUpgradeTemplate(String type, Properties props) {
         String path = type + "_upgrade_smithing_template";
         return new SmithingTemplateItem(
            AscendantEquipment.lang("item", path + ".applies_to").withStyle(ChatFormatting.BLUE),
            AscendantEquipment.lang("item", path + ".ingredients").withStyle(ChatFormatting.BLUE),
            AscendantEquipment.lang("upgrade", type).withStyle(ChatFormatting.GRAY),
            AscendantEquipment.lang("item", path + ".base_slot_description"),
            AscendantEquipment.lang("item", path + ".additions_slot_description"),
            SmithingTemplateItem.createNetheriteUpgradeIconList(),
            SmithingTemplateItem.createNetheriteUpgradeMaterialList()
         );
      }

      private static DynamicHolder<LootRarity> rarity(String path) {
         return RarityRegistry.INSTANCE.holder(AscendantEquipment.loc("uncommon"));
      }

      private static void bootstrap() {
      }
   }

   public static final class LootCategories {
      public static final LootCategory BOW = register(
         "bow", s -> s.getItem() instanceof BowItem || s.getItem() instanceof CrossbowItem, EquipmentSlotGroups.HAND
      );
      public static final LootCategory BREAKER = register("breaker", s -> s.is(ItemTags.PICKAXES) || s.is(ItemTags.SHOVELS), EquipmentSlotGroups.MAINHAND);
      public static final LootCategory HELMET = register("helmet", armorSlot(EquipmentSlot.HEAD), EquipmentSlotGroups.HEAD);
      public static final LootCategory CHESTPLATE = register("chestplate", armorSlot(EquipmentSlot.CHEST), EquipmentSlotGroups.CHEST);
      public static final LootCategory LEGGINGS = register("leggings", armorSlot(EquipmentSlot.LEGS), EquipmentSlotGroups.LEGS);
      public static final LootCategory BOOTS = register("boots", armorSlot(EquipmentSlot.FEET), EquipmentSlotGroups.FEET);
      public static final LootCategory SHIELD = register("shield", s -> s.getItem() instanceof ShieldItem, EquipmentSlotGroups.HAND);
      public static final LootCategory TRIDENT = register("trident", s -> s.getItem() instanceof TridentItem, EquipmentSlotGroups.MAINHAND);
      public static final LootCategory MELEE_WEAPON = register(
         "melee_weapon",
         s -> s.is(ItemTags.SWORDS) || getDefaultModifiers(s).compute(1, EquipmentSlot.MAINHAND) > 1.0,
         EquipmentSlotGroups.MAINHAND,
         2000
      );
      public static final LootCategory SHEARS = register("shears", s -> s.canPerformAction(ItemAbilities.SHEARS_DIG), EquipmentSlotGroups.MAINHAND, 2500);
      public static final LootCategory NONE = register("none", Predicates.alwaysFalse(), EquipmentSlotGroups.ANY, Integer.MAX_VALUE);

      private static LootCategory register(String path, Predicate<ItemStack> filter, EntitySlotGroup slots, int priority) {
         return (LootCategory)AscEq.R.custom(path, AscEq.BuiltInRegs.LOOT_CATEGORY.key(), new LootCategory(filter, slots, priority));
      }

      private static LootCategory register(String path, Predicate<ItemStack> filter, EntitySlotGroup slots) {
         return register(path, filter, slots, 1000);
      }

      private static Predicate<ItemStack> armorSlot(EquipmentSlot slot) {
         return stack -> {
            if (!stack.is(net.minecraft.world.item.Items.CARVED_PUMPKIN)
               && !(stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof AbstractSkullBlock)) {
               EquipmentSlot itemSlot = stack.getEquipmentSlot();
               if (itemSlot == null) {
                  Equipable equipable = Equipable.get(stack);
                  if (equipable != null) {
                     itemSlot = equipable.getEquipmentSlot();
                  }
               }

               return itemSlot == slot;
            } else {
               return false;
            }
         };
      }

      private static ItemAttributeModifiers getDefaultModifiers(ItemStack stack) {
         return (ItemAttributeModifiers)stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, stack.getItem().getDefaultAttributeModifiers(stack));
      }

      private static void bootstrap() {
      }
   }

   public static final class LootConditions {
      public static final LootItemConditionType MATCHES_BLOCK = AscEq.R.lootCondition("matches_block", MatchesBlockCondition.CODEC);
      public static final LootItemConditionType KILLED_BY_REAL_PLAYER = AscEq.R.lootCondition("killed_by_real_player", KilledByRealPlayerCondition.CODEC);
      public static final LootItemConditionType HAS_WORLD_TIER = AscEq.R.lootCondition("has_world_tier", WorldTierCondition.CODEC);
      public static final LootItemConditionType LOOT_TABLE_PATTERN_MATCHER = AscEq.R.lootCondition("loot_table_pattern_matcher", LootPatternMatcher.CODEC);

      private static void bootstrap() {
      }
   }

   public static final class LootFunctions {
      public static final LootItemFunctionType<ReforgeItemFunction> REFORGE_ITEM = AscEq.R
         .custom("reforge_item", Registries.LOOT_FUNCTION_TYPE, new LootItemFunctionType<>(ReforgeItemFunction.CODEC));
      public static final LootItemFunctionType<AutomaticAffixTrade> AUTOMATIC_AFFIX_TRADE = AscEq.R
         .custom("automatic_affix_trade", Registries.LOOT_FUNCTION_TYPE, new LootItemFunctionType<>(AutomaticAffixTrade.CODEC));
      public static final LootItemFunctionType<TierGatedTrade> TIER_GATED_COMPONENTS = AscEq.R
         .custom("tier_gated_trade", Registries.LOOT_FUNCTION_TYPE, new LootItemFunctionType<>(TierGatedTrade.CODEC));

      private static void bootstrap() {
      }
   }

   public static final class LootModifiers {
      public static final MapCodec<GemLootModifier> GEMS = AscEq.R.lootModifier("gems", GemLootModifier.CODEC);
      public static final MapCodec<AffixLootModifier> AFFIX_LOOT = AscEq.R.lootModifier("affix_loot", AffixLootModifier.CODEC);
      public static final MapCodec<AffixConvertLootModifier> AFFIX_CONVERSION = AscEq.R.lootModifier("affix_conversion", AffixConvertLootModifier.CODEC);
      public static final MapCodec<AffixHookLootModifier> CODE_HOOK = AscEq.R.lootModifier("code_hook", AffixHookLootModifier.CODEC);

      private static void bootstrap() {
      }
   }

   public static final class LootPoolEntries {
      public static final LootPoolEntryType RANDOM_AFFIX_ITEM = AscEq.R.lootPoolEntry("random_affix_item", AffixLootPoolEntry.TYPE);
      public static final LootPoolEntryType RANDOM_GEM = AscEq.R.lootPoolEntry("random_gem", GemLootPoolEntry.TYPE);

      private static void bootstrap() {
      }
   }

   public static final class LootTables {
      public static final ResourceKey<LootTable> CHEST_VALUABLE = key("chests/chest_valuable");
      public static final ResourceKey<LootTable> SPAWNER_BRUTAL = key("chests/spawner_brutal");
      public static final ResourceKey<LootTable> SPAWNER_SWARM = key("chests/spawner_swarm");
      public static final ResourceKey<LootTable> TOME_TOWER = key("chests/tome_tower");
      public static final ResourceKey<LootTable> BONUS_BOSS_DROPS = key("entity/boss_drops");
      public static final ResourceKey<LootTable> BONUS_RARE_BOSS_DROPS = key("entity/rare_boss_drops");
      public static final ResourceKey<LootTable> TREASURE_GOBLIN = key("entity/treasure_goblin");

      private static ResourceKey<LootTable> key(String path) {
         return ResourceKey.create(Registries.LOOT_TABLE, AscendantEquipment.loc(path));
      }
   }

   public static final class Menus {
      public static final MenuType<ReforgingMenu> REFORGING = AscEq.R.menuWithPos("reforging", ReforgingMenu::new);
      public static final MenuType<SalvagingMenu> SALVAGE = AscEq.R.menuWithPos("salvage", SalvagingMenu::new);
      public static final MenuType<GemCuttingMenu> GEM_CUTTING = AscEq.R.menu("gem_cutting", GemCuttingMenu::new);
      public static final MenuType<AugmentingMenu> AUGMENTING = AscEq.R.menuWithPos("augmenting", AugmentingMenu::new);
      public static final MenuType<GemCaseMenu> GEM_CASE = AscEq.R.menuWithPos("gem_case", GemCaseMenu::new);

      private static void bootstrap() {
      }
   }

   public static final class Particles {
      public static final ParticleType<RarityParticleData> RARITY_GLOW = AscEq.R
         .particle("rarity_glow", false, type -> RarityParticleData.CODEC, type -> RarityParticleData.STREAM_CODEC);

      private static void bootstrap() {
      }
   }

   public static final class RecipeSerializers {
      public static final Holder<RecipeSerializer<?>> WITHDRAWAL = AscEq.R
         .recipeSerializer("withdrawal", () -> new SingletonRecipeSerializer<>(WithdrawalRecipe::new));
      public static final Holder<RecipeSerializer<?>> SOCKETING = AscEq.R
         .recipeSerializer("socketing", () -> new SingletonRecipeSerializer<>(SocketingRecipe::new));
      public static final Holder<RecipeSerializer<?>> SUPREMACY = AscEq.R
         .recipeSerializer("supremacy", () -> new SingletonRecipeSerializer<>(SupremacyRecipe::new));
      public static final Holder<RecipeSerializer<?>> UNNAMING = AscEq.R
         .recipeSerializer("unnaming", () -> new SingletonRecipeSerializer<>(UnnamingRecipe::new));
      public static final Holder<RecipeSerializer<?>> MALICE = AscEq.R.recipeSerializer("malice", () -> new SingletonRecipeSerializer<>(MaliceRecipe::new));
      public static final Holder<RecipeSerializer<?>> ADD_SOCKETS = AscEq.R.recipeSerializer("add_sockets", () -> AddSocketsRecipe.Serializer.INSTANCE);
      public static final Holder<RecipeSerializer<?>> SALVAGING = AscEq.R.recipeSerializer("salvaging", () -> SalvagingRecipe.Serializer.INSTANCE);
      public static final Holder<RecipeSerializer<?>> REFORGING = AscEq.R.recipeSerializer("reforging", () -> ReforgingRecipe.Serializer.INSTANCE);
      public static final Holder<RecipeSerializer<?>> PURITY_UPGRADE = AscEq.R.recipeSerializer("purity_upgrade", () -> PurityUpgradeRecipe.Serializer.INSTANCE);
      public static final Holder<RecipeSerializer<?>> BASIC_GEM_CUTTING = AscEq.R.recipeSerializer("basic_gem_cutting", () -> BasicGemCuttingRecipe.Serializer.INSTANCE);
      public static final Holder<RecipeSerializer<?>> POTION_CHARM_CRAFTING = AscEq.R
         .recipeSerializer("potion_charm_crafting", () -> PotionCharmRecipe.Serializer.INSTANCE);
      public static final Holder<RecipeSerializer<?>> POTION_CHARM_INFUSION = AscEq.R
         .recipeSerializer("potion_charm_infusion", () -> CharmInfusionRecipe.Serializer.INSTANCE);
      public static final Holder<RecipeSerializer<?>> SIZED_UPGRADE_RECIPE = AscEq.R
         .recipeSerializer("sized_upgrade_recipe", () -> SizedUpgradeRecipe.Serializer.INSTANCE);

      private static void bootstrap() {
      }
   }

   public static final class RecipeTypes {
      public static final RecipeType<SalvagingRecipe> SALVAGING = AscEq.R.recipe("salvaging");
      public static final RecipeType<ReforgingRecipe> REFORGING = AscEq.R.recipe("reforging");
      public static final RecipeType<GemCuttingRecipe> GEM_CUTTING = AscEq.R.recipe("gem_cutting");

      private static void bootstrap() {
      }
   }

    public static final class Songs {
      public static final ResourceKey<JukeboxSong> FLASH = key("flash");
      public static final ResourceKey<JukeboxSong> GLIMMER = key("glimmer");
      public static final ResourceKey<JukeboxSong> SHIMMER = key("shimmer");

      private static ResourceKey<JukeboxSong> key(String name) {
         return ResourceKey.create(Registries.JUKEBOX_SONG, AscendantEquipment.loc(name));
      }
   }

   public static class Sounds {
      public static final Holder<SoundEvent> REFORGE_ITEM_PLACED = AscEq.R.sound("reforge_item_placed");
      public static final Holder<SoundEvent> REFORGE_ITEM_REFORGED = AscEq.R.sound("reforge_item_reforged");
      public static final Holder<SoundEvent> MALICE = AscEq.R.sound("malice");
      public static final Holder<SoundEvent> MUSIC_DISC_FLASH = AscEq.R.sound("music_disc_flash");
      public static final Holder<SoundEvent> MUSIC_DISC_GLIMMER = AscEq.R.sound("music_disc_glimmer");
      public static final Holder<SoundEvent> MUSIC_DISC_SHIMMER = AscEq.R.sound("music_disc_shimmer");
      public static final Holder<SoundEvent> INVADER_UNCOMMON = AscEq.R.sound("invader_uncommon");
      public static final Holder<SoundEvent> INVADER_RARE = AscEq.R.sound("invader_rare");
      public static final Holder<SoundEvent> INVADER_EPIC = AscEq.R.sound("invader_epic");
      public static final Holder<SoundEvent> INVADER_MYTHIC = AscEq.R.sound("invader_mythic");

      private static void bootstrap() {
      }
   }

   public static final class Stats {
      public static final ResourceLocation WORLD_TIERS_ACTIVATED = AscEq.R.customStat("world_tiers_activated", StatFormatter.DEFAULT);

      private static void bootstrap() {
      }
   }

   public static class Tabs {
      public static final Holder<CreativeModeTab> ADVENTURE = AscEq.R
         .creativeTab(
            "adventure",
            b -> b.title(Component.translatable("itemGroup.ascendant_equipment.adventure")).icon(() -> ((Item)AscEq.Items.GEM.value()).getDefaultInstance())
         );

      private static void bootstrap() {
      }
   }

   public static final class Tags {
      public static final TagKey<Block> ROGUE_SPAWNER_COVERS = BlockTags.create(AscendantEquipment.loc("rogue_spawner_covers"));
      public static final TagKey<Block> STONEFORMING_CANDIDATES = BlockTags.create(AscendantEquipment.loc("stoneforming_candidates"));
      public static final TagKey<Block> SANDFORMING_CANDIDATES = BlockTags.create(AscendantEquipment.loc("sandforming_candidates"));
      public static final TagKey<Block> LEAFFORMING_CANDIDATES = BlockTags.create(AscendantEquipment.loc("leafforming_candidates"));
      public static final TagKey<Block> GARDENING_CANDIDATES = BlockTags.create(AscendantEquipment.loc("gardening_candidates"));
      public static final TagKey<Item> BOSS_MUSIC_DISCS = ItemTags.create(AscendantEquipment.loc("boss_music_discs"));
      public static final TagKey<Potion> POTION_CHARM_BLACKLIST = TagKey.create(Registries.POTION, AscendantEquipment.loc("potion_charm_blacklist"));
      public static final TagKey<MobEffect> EXTENDED_CHARM_DURATION = TagKey.create(Registries.MOB_EFFECT, AscendantEquipment.loc("extended_charm_duration"));
   }

   public static final class Tiles {
      public static final BlockEntityType<BossSpawnerBlock.BossSpawnerTile> BOSS_SPAWNER = AscEq.R
         .tickingBlockEntity("boss_spawner", BossSpawnerBlock.BossSpawnerTile::new, TickSide.SERVER, new Holder[]{AscEq.Blocks.BOSS_SPAWNER});
      public static final BlockEntityType<ReforgingTableTile> REFORGING_TABLE = AscEq.R
         .tickingBlockEntity(
            "reforging_table", ReforgingTableTile::new, TickSide.CLIENT, new Holder[]{AscEq.Blocks.REFORGING_TABLE, AscEq.Blocks.SIMPLE_REFORGING_TABLE}
         );
      public static final BlockEntityType<SalvagingTableTile> SALVAGING_TABLE = AscEq.R
         .blockEntity("salvaging_table", SalvagingTableTile::new, new Holder[]{AscEq.Blocks.SALVAGING_TABLE});
      public static final BlockEntityType<AugmentingTableTile> AUGMENTING_TABLE = AscEq.R
         .tickingBlockEntity("augmenting_table", AugmentingTableTile::new, TickSide.CLIENT, new Holder[]{AscEq.Blocks.AUGMENTING_TABLE});
      public static final BlockEntityType<GemCaseTile> GEM_CASE = AscEq.R
         .tickingBlockEntity("gem_case", GemCaseTile.BasicGemCaseTile::new, TickSide.CLIENT, new Holder[]{AscEq.Blocks.GEM_CASE});
      public static final BlockEntityType<GemCaseTile> ENDER_GEM_CASE = AscEq.R
         .tickingBlockEntity("ender_gem_case", GemCaseTile.EnderGemCaseTile::new, TickSide.CLIENT, new Holder[]{AscEq.Blocks.ENDER_GEM_CASE});

      private static void bootstrap() {
      }
   }

   public static final class Triggers {
      public static final GemCutTrigger GEM_CUTTING = (GemCutTrigger)AscEq.R.criteriaTrigger("gem_cutting", new GemCutTrigger());
      public static final EquippedItemTrigger EQUIPPED_ITEM = (EquippedItemTrigger)AscEq.R.criteriaTrigger("equipped_item", new EquippedItemTrigger());

      private static void bootstrap() {
      }
   }
}
