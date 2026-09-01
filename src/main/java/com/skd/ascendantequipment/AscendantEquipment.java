package com.skd.ascendantequipment;

import java.io.File;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.skd.ascendantequipment.affix.AffixRegistry;
import com.skd.ascendantequipment.compat.GuideBookGrantHandler;
import com.skd.ascendantequipment.compat.VellumliCompat;
import com.skd.ascendantequipment.compat.curios.CuriosCompat;
import com.skd.ascendantequipment.loot.AffixLootRegistry;
import com.skd.ascendantequipment.loot.LootRule;
import com.skd.ascendantequipment.loot.RarityOverrideRegistry;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.mobs.AscEqMobEvents;
import com.skd.ascendantequipment.mobs.registries.AugmentRegistry;
import com.skd.ascendantequipment.mobs.registries.EliteRegistry;
import com.skd.ascendantequipment.mobs.registries.InvaderRegistry;
import com.skd.ascendantequipment.mobs.util.EntityModifier;
import com.skd.ascendantequipment.mobs.util.SpawnCondition;
import com.skd.ascendantequipment.net.BossSpawnPayload;
import com.skd.ascendantequipment.net.GemCaseSelectPayload;
import com.skd.ascendantequipment.net.LinkItemToChatPayload;
import com.skd.ascendantequipment.net.RadialStatePayload;
import com.skd.ascendantequipment.net.RerollResultPayload;
import com.skd.ascendantequipment.net.WorldTierPayload;
import com.skd.ascendantequipment.socket.gem.ExtraGemBonusRegistry;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.PurityWeightsRegistry;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import com.skd.ascendantequipment.spawner.RogueSpawnerRegistry;
import com.skd.ascendantequipment.tiers.augments.TierAugmentRegistry;
import com.skd.commontoolkit.config.Configuration;
import com.skd.commontoolkit.network.PayloadHelper;
import com.skd.commontoolkit.tabs.TabFillingRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AscendantEquipment.MODID)
public class AscendantEquipment {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "ascendant_equipment";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean DEBUG_MOBS = "on".equalsIgnoreCase(System.getenv("APOTH_DEBUG_MOBS"));
    public static final boolean DEBUG_WORLDGEN = "on".equalsIgnoreCase(System.getenv("APOTH_DEBUG_WORLDGEN"));
    public static final boolean STAGES_LOADED = ModList.get().isLoaded("gamestages");
    // Store the mod event bus for use in commonSetup
    private static IEventBus modEventBus;
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AscendantEquipment(IEventBus modEventBus, ModContainer modContainer) {
        AscendantEquipment.modEventBus = modEventBus;
        // Wire the ported AscEq content (registry stages, biome modifier serializers, datamaps,
        // etc.). Without this the whole ported module is never registered (the class above is the
        // scaffolded shell) and any data referencing its registries fails to load.
        AscEq.bootstrap(modEventBus);

        // Initialize codec dispatchers (LootRule/SpawnCondition/EntityModifier/GemBonus types)
        // Must happen BEFORE any data loading otherwise dynamic registries fail to parse.
        LootRule.initCodecs();
        SpawnCondition.initCodecs();
        EntityModifier.initCodecs();
        GemBonus.initCodecs();

        // Fill the ADVENTURE creative tab with every AscEq item (all registered as simple fillers).
        ResourceKey<CreativeModeTab> adventureTab = AscEq.Tabs.ADVENTURE.getKey();
        TabFillingRegistry.register(
            adventureTab,
            AscEq.Items.MYSTERIOUS_SCRAP_METAL,
            AscEq.Items.TIMEWORN_FABRIC,
            AscEq.Items.LUMINOUS_CRYSTAL_SHARD,
            AscEq.Items.ARCANE_SANDS,
            AscEq.Items.GODFORGED_PEARL,
            AscEq.Items.GOD_FUSED_PEARL,
            AscEq.Items.GEM_DUST,
            AscEq.Items.GEM_FUSED_SLATE);
        TabFillingRegistry.register(
            adventureTab,
            AscEq.Items.SIGIL_OF_SOCKETING,
            AscEq.Items.SIGIL_OF_WITHDRAWAL,
            AscEq.Items.SIGIL_OF_REBIRTH,
            AscEq.Items.SIGIL_OF_ENHANCEMENT,
            AscEq.Items.SIGIL_OF_UNNAMING,
            AscEq.Items.SIGIL_OF_MALICE,
            AscEq.Items.SIGIL_OF_SUPREMACY);
        TabFillingRegistry.register(
            adventureTab,
            AscEq.Items.BOSS_SUMMONER,
            AscEq.Items.SIMPLE_REFORGING_TABLE,
            AscEq.Items.REFORGING_TABLE,
            AscEq.Items.SALVAGING_TABLE,
            AscEq.Items.GEM_CUTTING_TABLE,
            AscEq.Items.AUGMENTING_TABLE,
            AscEq.Items.GEM_CASE,
            AscEq.Items.ENDER_GEM_CASE,
            AscEq.Items.GEM,
            AscEq.Items.POTION_CHARM);
        TabFillingRegistry.register(
            adventureTab,
            AscEq.Items.IRON_UPGRADE_SMITHING_TEMPLATE,
            AscEq.Items.GOLD_UPGRADE_SMITHING_TEMPLATE,
            AscEq.Items.DIAMOND_UPGRADE_SMITHING_TEMPLATE);
        TabFillingRegistry.register(
            adventureTab,
            AscEq.Items.MUSIC_DISC_FLASH,
            AscEq.Items.MUSIC_DISC_GLIMMER,
            AscEq.Items.MUSIC_DISC_SHIMMER);
        TabFillingRegistry.register(
            adventureTab,
            AscEq.Items.SPAWNER_CHAIN,
            AscEq.Items.SPAWNER_RUNE,
            AscEq.Items.INFUSED_SPAWNER_RUNE,
            AscEq.Items.FRONTIER_SPAWNER_UPGRADE_RUNE,
            AscEq.Items.ASCENT_SPAWNER_UPGRADE_RUNE,
            AscEq.Items.SUMMIT_SPAWNER_UPGRADE_RUNE,
            AscEq.Items.PINNACLE_SPAWNER_UPGRADE_RUNE,
            AscEq.Items.SPAWN_RANGE_SPAWNER_RUNE,
            AscEq.Items.REDSTONE_CONTROL_SPAWNER_RUNE,
            AscEq.Items.IGNORE_LIGHT_SPAWNER_RUNE,
            AscEq.Items.INITIAL_HEALTH_SPAWNER_RUNE,
            AscEq.Items.SILENT_SPAWNER_RUNE,
            AscEq.Items.YOUTHFUL_SPAWNER_RUNE,
            AscEq.Items.BURNING_SPAWNER_RUNE,
            AscEq.Items.NO_AI_SPAWNER_RUNE,
            AscEq.Items.IGNORE_CONDITIONS_SPAWNER_RUNE,
            AscEq.Items.IGNORE_PLAYERS_SPAWNER_RUNE,
            AscEq.Items.ECHOING_SPAWNER_RUNE);

        // Explicitly add the Vellumli guide book to our own creative tab. Vellumli's own
        // BuildCreativeModeTabContentsEvent listener (which normally handles this via book.json's
        // "creative_tab" field, and separately adds every book to the vanilla search tab) does not
        // reliably pick this book up for reasons not fully root-caused — this direct registration
        // through our own TabFillingRegistry, the same mechanism every other item in this mod uses,
        // guarantees the book actually shows up regardless of that.
        if (ModList.get().isLoaded("vellumli")) {
            TabFillingRegistry.register(adventureTab,
                    (tab, event) -> event.accept(VellumliCompat.createGuideBookStack()));
        }

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register Curios compatibility
        CuriosCompat.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (AscendantEquipment) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Load the real Ascendant Equipment adventure module config (bosses, augmenting, spawners, etc).
        File configDir = new File(new File(FMLPaths.CONFIGDIR.get().toFile(), "ascendant"), "equipment");
        EquipmentConfig.load(new Configuration(new File(configDir, "ascendant_equipment.cfg")));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("HELLO FROM COMMON SETUP");

            if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
                LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
            }

            LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

            Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));

            VellumliCompat.register();
            CuriosCompat.register(modEventBus);
        });

        PayloadHelper.registerPayload(new BossSpawnPayload.Provider());
        PayloadHelper.registerPayload(new RerollResultPayload.Provider());
        PayloadHelper.registerPayload(new RadialStatePayload.Provider());
        PayloadHelper.registerPayload(new WorldTierPayload.Provider());
        PayloadHelper.registerPayload(new LinkItemToChatPayload.Provider());
        PayloadHelper.registerPayload(new GemCaseSelectPayload.Provider());
        PayloadHelper.registerPayload(new EquipmentConfig.ConfigPayload.Provider());

        NeoForge.EVENT_BUS.register(new EquipmentEvents());
        NeoForge.EVENT_BUS.register(new AscEqMobEvents());
        NeoForge.EVENT_BUS.register(new GuideBookGrantHandler());

        RarityRegistry.INSTANCE.registerToBus();
        RarityOverrideRegistry.INSTANCE.registerToBus();
        AffixRegistry.INSTANCE.registerToBus();
        ExtraGemBonusRegistry.INSTANCE.registerToBus();
        GemRegistry.INSTANCE.registerToBus();
        AffixLootRegistry.INSTANCE.registerToBus();
        InvaderRegistry.INSTANCE.registerToBus();
        RogueSpawnerRegistry.INSTANCE.registerToBus();
        EliteRegistry.INSTANCE.registerToBus();
        PurityWeightsRegistry.INSTANCE.registerToBus();
        AugmentRegistry.INSTANCE.registerToBus();
        TierAugmentRegistry.INSTANCE.registerToBus();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static MutableComponent lang(String type, String path, Object... args) {
        return Component.translatable(langKey(type, path), args);
    }

    public static String langKey(String type, String path) {
        return type + "." + MODID + "." + path;
    }

    public static MutableComponent sysMessageHeader() {
        return Component.translatable("[%s] ", Component.literal("AscEq").withStyle(ChatFormatting.GOLD));
    }

    public static void debugLog(BlockPos pos, String name) {
        if (DEBUG_WORLDGEN) {
            LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
