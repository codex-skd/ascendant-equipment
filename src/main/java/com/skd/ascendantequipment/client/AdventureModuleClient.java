package com.skd.ascendantequipment.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.affix.AffixRegistry;
import com.skd.ascendantequipment.affix.AttributeProvidingAffix;
import com.skd.ascendantequipment.affix.augmenting.AugmentingScreen;
import com.skd.ascendantequipment.affix.augmenting.AugmentingTableTileRenderer;
import com.skd.ascendantequipment.affix.effect.StoneformingAffix;
import com.skd.ascendantequipment.affix.reforging.ReforgingRecipeCache;
import com.skd.ascendantequipment.affix.reforging.ReforgingScreen;
import com.skd.ascendantequipment.affix.reforging.ReforgingTableTileRenderer;
import com.skd.ascendantequipment.affix.salvaging.SalvagingRecipeCache;
import com.skd.ascendantequipment.affix.salvaging.SalvagingScreen;
import com.skd.ascendantequipment.item.PotionCharmItem;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.net.BossSpawnPayload;
import com.skd.ascendantequipment.socket.SocketHelper;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingRecipeCache;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingScreen;
import com.skd.ascendantequipment.socket.gem.storage.GemCaseScreen;
import com.skd.ascendantequipment.socket.gem.storage.GemCaseTileRenderer;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.ascendantequipment.util.ApothMiscUtil;
import com.skd.ascendantequipment.util.EquipmentComparePositioner;
import com.skd.ascendantattributes.AscendantAttributes;
import com.skd.ascendantattributes.api.AscendantAttributesObjects.EquipmentSlots;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterStandalone;
import net.neoforged.neoforge.client.event.RenderTooltipEvent.GatherComponents;
import net.neoforged.neoforge.client.event.RenderTooltipEvent.Pre;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import net.neoforged.neoforge.client.pipeline.RegisterPipelineModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@EventBusSubscriber(modid = "ascendant_equipment", value = Dist.CLIENT)
public class AdventureModuleClient {
   public static final int COMPARE_PADDING = 18;
   public static final StandaloneModelKey<BlockStateModel> HAMMER_MODEL = new StandaloneModelKey(() -> "ascendant_equipment:hammer");
   public static final StandaloneModelKey<BlockStateModel> STAR_CUBE_MODEL = new StandaloneModelKey(() -> "ascendant_equipment:star_cube");
   private static final Identifier COMPARE_TOOLTIP_SPRITES = AscendantEquipment.loc("compare");
   public static final int[] GHOST_ALPHA_TIERS = new int[]{64, 72, 80, 88, 96, 104, 112, 120, 128, 136, 144, 152, 160, 168, 176, 184, 192, 200, 208, 216, 224};
   public static final ResourceKey<PipelineModifier>[] GHOST_ITEM_TIERS = new ResourceKey[GHOST_ALPHA_TIERS.length];
   public static final ResourceKey<PipelineModifier> GRAY_ITEM;
   private static final List<BossSpawnPayload.BossSpawnData> BOSS_SPAWNS;
   private static final Component GEM_SOCKET_MARKER;
   private static boolean inComparisonRender;

   @SubscribeEvent
   public static void setup(FMLClientSetupEvent e) {
      e.enqueueWork(() -> {
         BlockEntityRenderers.register(AscEq.Tiles.REFORGING_TABLE, ReforgingTableTileRenderer::new);
         BlockEntityRenderers.register(AscEq.Tiles.AUGMENTING_TABLE, AugmentingTableTileRenderer::new);
         BlockEntityRenderers.register(AscEq.Tiles.GEM_CASE, GemCaseTileRenderer::new);
         BlockEntityRenderers.register(AscEq.Tiles.ENDER_GEM_CASE, GemCaseTileRenderer::new);
      });
      NeoForge.EVENT_BUS.register(AdventureKeys.class);
      NeoForge.EVENT_BUS.register(RadialProgressTracker.class);
      TierAugmentModifierSource.bootstrap();
   }

   @SubscribeEvent
   public static void screens(RegisterMenuScreensEvent e) {
      e.register(AscEq.Menus.REFORGING, ReforgingScreen::new);
      e.register(AscEq.Menus.SALVAGE, SalvagingScreen::new);
      e.register(AscEq.Menus.GEM_CUTTING, GemCuttingScreen::new);
      e.register(AscEq.Menus.AUGMENTING, AugmentingScreen::new);
      e.register(AscEq.Menus.GEM_CASE, GemCaseScreen::new);
   }

   @SubscribeEvent
   public static void registerStandaloneModels(RegisterStandalone e) {
      e.register(HAMMER_MODEL, SimpleUnbakedStandaloneModel.blockStateModel(AscendantEquipment.loc("item/hammer")));
      e.register(STAR_CUBE_MODEL, SimpleUnbakedStandaloneModel.blockStateModel(AscendantEquipment.loc("item/star_cube")));
      addGemModels(e);
   }

   private static void addGemModels(RegisterStandalone e) {
      GemModel.GEM_MODEL_KEYS.clear();
      Minecraft.getInstance().getResourceManager().listResources("models/item/gems", loc -> loc.getPath().endsWith(".json")).keySet().forEach(loc -> {
         String fullPath = loc.getPath();
         String modelPath = fullPath.substring("models/".length(), fullPath.length() - ".json".length());
         Identifier cuboidModelId = Identifier.fromNamespaceAndPath(loc.getNamespace(), modelPath);
         String gemName = modelPath.substring(modelPath.indexOf("item/gems/") + "item/gems/".length());
         DynamicHolder<Gem> gemId = GemRegistry.INSTANCE.holder(Identifier.fromNamespaceAndPath(loc.getNamespace(), gemName));
         StandaloneModelKey<ItemModel> key = new StandaloneModelKey(cuboidModelId::toString);
         GemModel.GEM_MODEL_KEYS.put(gemId, key);
         e.register(key, new SimpleUnbakedStandaloneModel(cuboidModelId, (resolvedModel, baker, name) -> {
            TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
            QuadCollection quads = resolvedModel.bakeTopGeometry(textureSlots, baker, BlockModelRotation.IDENTITY);
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolvedModel, textureSlots);
            return new CuboidItemModelWrapper(List.of(), quads, properties, new Matrix4f());
         }));
      });
   }

   @SubscribeEvent
   public static void itemModels(RegisterItemModelsEvent e) {
      e.register(AscendantEquipment.loc("gem_dispatch"), GemModel.Unbaked.MAP_CODEC);
   }

   @SubscribeEvent
   public static void tooltipComps(RegisterClientTooltipComponentFactoriesEvent e) {
      e.register(SocketTooltipRenderer.SocketComponent.class, SocketTooltipRenderer::new);
      e.register(StoneformingTooltipRenderer.StoneformingComponent.class, StoneformingTooltipRenderer::new);
   }

   @SubscribeEvent
   public static void keys(RegisterKeyMappingsEvent e) {
      e.registerCategory(AdventureKeys.CATEGORY);
      e.register(AdventureKeys.TOGGLE_RADIAL);
      e.register(AdventureKeys.OPEN_WORLD_TIER_SELECT);
      e.register(AdventureKeys.LINK_ITEM_TO_CHAT);
      e.register(AdventureKeys.COMPARE_EQUIPMENT);
   }

   @SubscribeEvent
   public static void factories(RegisterParticleProvidersEvent e) {
      e.registerSpriteSet(AscEq.Particles.RARITY_GLOW, RarityParticle.Provider::new);
   }

   @SubscribeEvent
   public static void modifiers(RegisterPipelineModifiersEvent e) {
      for (int i = 0; i < GHOST_ALPHA_TIERS.length; i++) {
         float alpha = GHOST_ALPHA_TIERS[i] / 255.0F;
         e.register(
            GHOST_ITEM_TIERS[i],
            (pipeline, name) -> {
               Identifier loc = pipeline.getLocation();
               return !loc.equals(RenderPipelines.ITEM_CUTOUT.getLocation()) && !loc.equals(RenderPipelines.ITEM_TRANSLUCENT.getLocation())
                  ? pipeline
                  : pipeline.toBuilder()
                     .withLocation(name)
                     .withFragmentShader(AscendantEquipment.loc("core/ghost"))
                     .withShaderDefine("APOTH_GHOST_ALPHA", alpha)
                     .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                     .build();
            }
         );
      }

      e.register(
         GRAY_ITEM,
         (pipeline, name) -> {
            Identifier loc = pipeline.getLocation();
            return !loc.equals(RenderPipelines.ITEM_CUTOUT.getLocation()) && !loc.equals(RenderPipelines.ITEM_TRANSLUCENT.getLocation())
               ? pipeline
               : pipeline.toBuilder().withLocation(name).withFragmentShader(AscendantEquipment.loc("core/gray")).build();
         }
      );
   }

   public static void onBossSpawn(BlockPos pos, DynamicHolder<LootRarity> rarityHolder) {
      if (rarityHolder.isBound()) {
         LootRarity rarity = (LootRarity)rarityHolder.get();
         BOSS_SPAWNS.add(new BossSpawnPayload.BossSpawnData(pos, rarity, new MutableInt()));
         Minecraft.getInstance()
            .getSoundManager()
            .play(
               new SimpleSoundInstance(
                  rarity.invaderSound(), SoundSource.HOSTILE, EquipmentConfig.bossAnnounceRange / 16.0F, 1.0F, Minecraft.getInstance().player.getRandom(), pos
               )
            );
      }
   }

   public static void checkAffixLangKeys() {
      if (!DatagenModLoader.isRunningDataGen()) {
         StringBuilder sb = new StringBuilder("Missing Affix Lang Keys:\n");
         boolean any = false;
         String json = "\"%s\": \"\",";

         for (Affix a : AffixRegistry.INSTANCE.getValues()) {
            Identifier id = AffixRegistry.INSTANCE.getKey(a);
            if (!Language.getInstance().has("affix." + id)) {
               sb.append(json.formatted("affix." + id) + "\n");
               any = true;
            }

            if (!Language.getInstance().has("affix." + id + ".suffix")) {
               sb.append(json.formatted("affix." + id + ".suffix") + "\n");
               any = true;
            }
         }

         if (any) {
            AscendantEquipment.LOGGER.error(sb.toString());
         }
      }
   }

   public static AttributeTooltipContext tooltipCtx() {
      return AttributeTooltipContext.of(
         Minecraft.getInstance().player, TooltipContext.of(Minecraft.getInstance().level), TooltipDisplay.DEFAULT, AscendantAttributes.getTooltipFlag()
      );
   }

   @SubscribeEvent
   public static void renderBossBeams(SubmitCustomGeometryEvent e) {
      if (!BOSS_SPAWNS.isEmpty()) {
         Vec3 camPos = e.getLevelRenderState().cameraRenderState.pos;
         PoseStack poseStack = e.getPoseStack();
         SubmitNodeCollector collector = e.getSubmitNodeCollector();
         float animTime = (float)e.getLevelRenderState().gameTime;

         for (BossSpawnPayload.BossSpawnData data : BOSS_SPAWNS) {
            BlockPos pos = data.pos();
            int color = 0xFF000000 | data.rarity().color().getValue();
            poseStack.pushPose();
            poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
            BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1.0F, animTime, 0, 1024, color, 0.2F, 0.25F);
            poseStack.popPose();
         }
      }
   }

   @SubscribeEvent
   public static void login(LoggingIn e) {
      e.getConnection().send(new ServerboundClientCommandPacket(Action.REQUEST_STATS));
   }

   @SubscribeEvent
   public static void logout(LoggingOut e) {
      SalvagingRecipeCache.clear();
      GemCuttingRecipeCache.clear();
      ReforgingRecipeCache.clear();
   }

   @SubscribeEvent
   public static void recipesReceived(RecipesReceivedEvent e) {
      if (e.getRecipeTypes().contains(AscEq.RecipeTypes.SALVAGING)) {
         SalvagingRecipeCache.rebuildFromMap(e.getRecipeMap());
      }

      if (e.getRecipeTypes().contains(AscEq.RecipeTypes.GEM_CUTTING)) {
         GemCuttingRecipeCache.rebuildFromMap(e.getRecipeMap());
      }

      if (e.getRecipeTypes().contains(AscEq.RecipeTypes.REFORGING)) {
         ReforgingRecipeCache.rebuildFromMap(e.getRecipeMap());
      }
   }

   @SubscribeEvent
   public static void time(Post e) {
      for (int i = 0; i < BOSS_SPAWNS.size(); i++) {
         BossSpawnPayload.BossSpawnData data = BOSS_SPAWNS.get(i);
         if (data.ticks().getAndIncrement() > 400) {
            BOSS_SPAWNS.remove(i--);
         }
      }
   }

   @SubscribeEvent
   public static void tooltips(AddAttributeTooltipsEvent e) {
      ItemStack stack = e.getStack();
      int sockets = SocketHelper.getSockets(stack);
      if (sockets > 0 && !WorldTier.isTutorialActive(Minecraft.getInstance().player)) {
         e.addTooltipLines(new Component[]{GEM_SOCKET_MARKER.copy()});
      }
   }

   @SubscribeEvent
   public static void ignoreSocketUUIDS(GatherSkippedAttributeTooltipsEvent e) {
      ItemStack stack = e.getStack();

      for (GemInstance gem : SocketHelper.getGems(stack)) {
         if (gem.isValid()) {
            gem.skipModifierIds(e::skipId);
         }
      }

      AffixHelper.streamAffixes(stack).forEach(inst -> {
         if (inst.getAffix() instanceof AttributeProvidingAffix afx) {
            afx.skipModifierIds(inst, e.getContext(), e::skipId);
         }
      });
   }

   @SubscribeEvent
   public static void comps(GatherComponents e) {
      List<Either<FormattedText, TooltipComponent>> list = e.getTooltipElements();

      for (int i = 0; i < list.size(); i++) {
         Either<FormattedText, TooltipComponent> entry = list.get(i);
         if (containsMarker(entry, GEM_SOCKET_MARKER)) {
            list.remove(i);
            list.add(i, Either.right(new SocketTooltipRenderer.SocketComponent(e.getItemStack(), SocketHelper.getGems(e.getItemStack()))));
         } else if (containsMarker(entry, StoneformingAffix.TOOLTIP_MARKER)) {
            list.remove(i);
            AffixInstance inst = AffixHelper.streamAffixes(e.getItemStack()).filter(a -> a.getAffix() instanceof StoneformingAffix).findFirst().orElse(null);
            if (inst != null) {
               list.add(i, Either.right(new StoneformingTooltipRenderer.StoneformingComponent(inst)));
            }
         }
      }
   }

   private static boolean containsMarker(Either<FormattedText, TooltipComponent> entry, Component marker) {
      Optional<FormattedText> o = entry.left();
      return o.isPresent() && o.get() instanceof Component comp && comp.contains(marker);
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void affixTooltips(ItemTooltipEvent e) {
      ItemStack stack = e.getItemStack();
      List<Component> components = new ArrayList<>();
      AttributeTooltipContext ctx = AttributeTooltipContext.of(
         Minecraft.getInstance().player,
         e.getContext(),
         (TooltipDisplay)stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT),
         e.getFlags()
      );
      if (e.getEntity() != null && WorldTier.isTutorialActive(e.getEntity())) {
         if (stack.has(AscEq.Components.AFFIXES) || stack.has(AscEq.Components.SOCKETS) || stack.has(AscEq.Components.RARITY)) {
            e.getToolTip().add(1, AscendantEquipment.lang("text", "world_tier_tutorial").withStyle(ChatFormatting.YELLOW));
            e.getToolTip()
               .add(
                  2,
                  AscendantEquipment.lang("text", "world_tier_tutorial.2", AdventureKeys.OPEN_WORLD_TIER_SELECT.getTranslatedKeyMessage())
                     .withStyle(ChatFormatting.YELLOW)
               );
         }
      } else {
         if (stack.has(AscEq.Components.AFFIXES)) {
            AffixHelper.streamAffixes(stack).sorted(Comparator.comparingInt(a -> a.getAffix().definition().type().ordinal())).forEach(inst -> {
               Component desc = inst.getDescription(ctx);
               if (desc.getContents() != PlainTextContents.EMPTY) {
                  if (inst.level() > 1.0F) {
                     components.add(ApothMiscUtil.starPrefix(desc).withStyle(ChatFormatting.YELLOW));
                  } else {
                     components.add(ApothMiscUtil.dotPrefix(desc).withStyle(ChatFormatting.YELLOW));
                  }
               }
            });
         }

         if (stack.has(AscEq.Components.DURABILITY_BONUS) && !stack.has(DataComponents.UNBREAKABLE)) {
            Component desc = Component.translatable(
               "affix.ascendant_equipment:durable.desc", new Object[]{Math.round(100.0F * (Float)stack.get(AscEq.Components.DURABILITY_BONUS))}
            );
            components.add(ApothMiscUtil.dotPrefix(desc).withStyle(ChatFormatting.YELLOW));
         }

         if ((Boolean)stack.getOrDefault(AscEq.Components.MALICE_MARKER, false)) {
            Component desc = AscendantEquipment.lang("text", "malice_marker").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.UNDERLINE});
            components.add(desc);
         }

         if ((Boolean)stack.getOrDefault(AscEq.Components.TOUCHED_BY_MALICE, false)) {
            Component desc = AscendantEquipment.lang("text", "touched_by_malice");
            components.add(ApothMiscUtil.dotPrefix(desc).withStyle(ChatFormatting.RED));
         }

         if (!components.isEmpty()) {
            e.getToolTip().addAll(1, components);
         }

         Set<Component> special = new HashSet<>();
         AffixHelper.streamAffixes(stack)
            .filter(inst -> inst.level() > 1.0F)
            .filter(inst -> inst.getAffix() instanceof AttributeProvidingAffix)
            .forEach(inst -> ((AttributeProvidingAffix)inst.getAffix()).gatherModifierTooltips(inst, ctx, special::add));
         List<Component> tooltips = e.getToolTip();
         Component listHeader = Component.literal(" ┇ ").withStyle(ChatFormatting.GRAY);
         if (!special.isEmpty()) {
            for (int i = 0; i < tooltips.size(); i++) {
               Component comp = tooltips.get(i);
               if (special.contains(comp)) {
                  tooltips.remove(i);
                  tooltips.add(i, ApothMiscUtil.starPrefix(comp).withStyle(comp.getStyle()));
               } else if (comp.getContents().equals(listHeader.getContents()) && comp.getSiblings().size() == 1) {
                  Component child = (Component)comp.getSiblings().get(0);
                  if (special.contains(child)) {
                     tooltips.remove(i);
                     MutableComponent replacement = listHeader.copy();
                     replacement.append(ApothMiscUtil.starPrefix(child).withStyle(child.getStyle()));

                     for (int j = 1; j < comp.getSiblings().size(); j++) {
                        replacement.append((Component)comp.getSiblings().get(j));
                     }

                     tooltips.add(i, replacement);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void showBlacklistedPotions(ItemTooltipEvent e) {
      if (e.getItemStack().getItem() == Items.POTION) {
         Holder<Potion> potion = (Holder<Potion>)((PotionContents)e.getItemStack().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY))
            .potion()
            .orElse(Potions.WATER);
         if (!PotionCharmItem.isValidPotion(potion)) {
            e.getToolTip()
               .add(
                  Component.translatable("misc.ascendant_equipment.blacklisted_potion").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC})
               );
         }
      }
   }

   @SubscribeEvent
   public static void renderCanSocketTooltip(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post e) {
      if (e.getScreen() instanceof AbstractContainerScreen<?> screen) {
         ItemStack var9 = screen.getMenu().getCarried();
         Slot slot = screen.getHoveredSlot();
         if (slot != null) {
            ItemStack hover = slot.getItem();
            if (var9.is(AscEq.Items.GEM) && SocketHelper.canSocketGemInItem(hover, var9)) {
               Component itemName = Component.translatable("%s", new Object[]{hover.getHoverName()}).withStyle(ChatFormatting.WHITE);
               Component line = AscendantEquipment.lang("misc", "right_click_to_socket", var9.getHoverName(), itemName).withStyle(ChatFormatting.GRAY);
               Font font = Minecraft.getInstance().font;
               List<ClientTooltipComponent> comps = List.of(ClientTooltipComponent.create(line.getVisualOrderText()));
               e.getGuiGraphics().tooltip(font, comps, e.getMouseX(), e.getMouseY(), DefaultTooltipPositioner.INSTANCE, null);
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public static void compareItems(Pre e) {
      if (!inComparisonRender && EquipmentConfig.enableEquipmentCompare) {
         Minecraft mc = Minecraft.getInstance();
         if (ApothMiscUtil.ClientInternal.isKeyReallyDown(AdventureKeys.COMPARE_EQUIPMENT) && mc.gui.screen() instanceof AbstractContainerScreen<?> screen) {
            Slot slot = screen.getHoveredSlot();
            if (slot != null && slot.hasItem() && slot.getItem() == e.getItemStack()) {
               ItemStack stack = e.getItemStack();
               LootCategory cat = LootCategory.forItem(stack);
               if (!cat.isNone()) {
                  Player player = mc.player;
                  ItemStack equipped = ItemStack.EMPTY;
                  Equippable equip = (Equippable)stack.get(DataComponents.EQUIPPABLE);
                  if (equip != null) {
                     ItemStack candidate = player.getItemBySlot(equip.slot());
                     if (!candidate.isEmpty() && stack != candidate) {
                        equipped = candidate;
                     }
                  } else {
                     if (cat.getSlots().test(EquipmentSlots.MAINHAND)) {
                        ItemStack candidate = player.getMainHandItem();
                        if (LootCategory.forItem(candidate) == cat && stack != candidate) {
                           equipped = candidate;
                        }
                     }

                     if (equipped.isEmpty() && cat.getSlots().test(EquipmentSlots.OFFHAND)) {
                        ItemStack candidate = player.getOffhandItem();
                        if (LootCategory.forItem(candidate) == cat && stack != candidate) {
                           equipped = candidate;
                        }
                     }
                  }

                  if (!equipped.isEmpty()) {
                     tryRenderComparison(e, mc, equipped);
                  }
               }
            }
         }
      }
   }

   private static void tryRenderComparison(Pre e, Minecraft mc, ItemStack equipped) {
      Font font = e.getFont();
      GuiGraphicsExtractor gfx = e.getGraphics();
      ClientTooltipPositioner positioner = e.getTooltipPositioner();
      int scnWidth = e.getScreenWidth();
      int scnHeight = e.getScreenHeight();
      List<ClientTooltipComponent> compList = e.getComponents();
      int compWidth = -1;
      int compHeight = 0;

      for (ClientTooltipComponent comp : compList) {
         compWidth = Math.max(compWidth, comp.getWidth(font));
         compHeight += comp.getHeight(font);
      }

      Vector2ic compPos = positioner.positionTooltip(scnWidth, scnHeight, e.getX(), e.getY(), compWidth, compHeight);
      List<Component> equipLines = Screen.getTooltipFromItem(mc, equipped);
      List<ClientTooltipComponent> equipList = ClientHooks.gatherTooltipComponents(
         equipped, equipLines, equipped.getTooltipImage(), 0, gfx.guiWidth() - compWidth - 36, gfx.guiHeight(), font
      );
      int equipWidth = -1;
      int equipHeight = 0;

      for (ClientTooltipComponent comp : equipList) {
         equipWidth = Math.max(equipWidth, comp.getWidth(font));
         equipHeight += comp.getHeight(font);
      }

      Vector2ic equipPos = new Vector2i(compPos.x() - 18 - equipWidth, compPos.y());
      EquipmentComparePositioner realPositioner = new EquipmentComparePositioner(scnWidth, scnHeight);
      boolean canRender = realPositioner.position(equipPos, equipWidth + 6, equipHeight + 6, compPos, compWidth + 6, compHeight + 6);
      if (canRender) {
         e.setCanceled(true);
         inComparisonRender = true;

         try {
            Vector2ic finalCompPos = realPositioner.getComparePos();
            Vector2ic finalEquipPos = realPositioner.getEquippedPos();
            ClientTooltipPositioner compFixed = (sw, sh, mx, my, w, h) -> finalCompPos;
            ClientTooltipPositioner equipFixed = (sw, sh, mx, my, w, h) -> finalEquipPos;
            gfx.tooltip(font, compList, finalCompPos.x(), finalCompPos.y(), compFixed, null, e.getItemStack());
            gfx.tooltip(font, equipList, finalEquipPos.x(), finalEquipPos.y(), equipFixed, null, equipped);
            Component equippedTxt = AscendantEquipment.lang("text", "equipped");
            int txtWidth = font.width(equippedTxt);
            int txtX = finalEquipPos.x() + equipWidth / 2 - txtWidth / 2;
            int txtY = finalEquipPos.y() - 9 - 10;
            TooltipRenderUtil.extractTooltipBackground(gfx, txtX, txtY, txtWidth, 9, COMPARE_TOOLTIP_SPRITES);
            gfx.text(font, equippedTxt, txtX, txtY, -1);
         } finally {
            inComparisonRender = false;
         }
      }
   }

   static {
      for (int i = 0; i < GHOST_ALPHA_TIERS.length; i++) {
         GHOST_ITEM_TIERS[i] = ResourceKey.create(PipelineModifier.MODIFIERS_KEY, AscendantEquipment.loc("ghost_item_" + Integer.toHexString(GHOST_ALPHA_TIERS[i])));
      }

      GRAY_ITEM = ResourceKey.create(PipelineModifier.MODIFIERS_KEY, AscendantEquipment.loc("gray_item"));
      BOSS_SPAWNS = new ArrayList<>();
      GEM_SOCKET_MARKER = Component.literal("ASCENDANT_EQUIPMENT_SOCKET_MARKER");
      inComparisonRender = false;
   }
}
