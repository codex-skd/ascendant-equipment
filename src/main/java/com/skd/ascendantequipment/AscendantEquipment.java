package com.skd.ascendantequipment;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.skd.ascendantequipment.compat.VellumliCompat;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
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
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AscendantEquipment(IEventBus modEventBus, ModContainer modContainer) {
        // Wire the ported AscEq content (registry stages, biome modifier serializers, datamaps,
        // etc.). Without this the whole ported module is never registered (the class above is the
        // scaffolded shell) and any data referencing its registries fails to load.
        AscEq.bootstrap(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (AscendantEquipment) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));

        VellumliCompat.register();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
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
