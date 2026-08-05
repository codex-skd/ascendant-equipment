package com.skd.ascendantequipment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRenderData;
import com.skd.ascendantequipment.particle.RarityParticleData;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;

@EventBusSubscriber(modid = "ascendant_equipment", value = Dist.CLIENT)
public class AffixItemEffectRenderer {
   private static final float GROW_IN_TICKS = 15.0F;
   private static final int ALPHA_ZERO = 0;
   private static final int ALPHA_LOW = 31;
   private static final int ALPHA_MAX = 159;

   @SubscribeEvent
   public static void submitBeams(SubmitCustomGeometryEvent e) {
      if (EquipmentConfig.enableAffixItemEffects) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            Vec3 camPos = e.getLevelRenderState().cameraRenderState.pos;
            PoseStack pose = e.getPoseStack();
            SubmitNodeCollector collector = e.getSubmitNodeCollector();
            float partials = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            long gameTime = mc.level.getGameTime();

            for (Entity ent : mc.level.entitiesForRendering()) {
               if (ent instanceof ItemEntity item) {
                  ItemStack stack = item.getItem();
                  DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(stack);
                  if (rarityHolder.isBound() && item.onGround()) {
                     if (!(Boolean)item.getData(AscEq.Attachments.AFFIX_EFFECT_RENDER_STARTED)) {
                        item.setData(AscEq.Attachments.AFFIX_EFFECT_RENDER_STARTED, true);
                        item.setData(AscEq.Attachments.AFFIX_EFFECT_START_TIME, item.tickCount);
                     }

                     LootRarity rarity = (LootRarity)rarityHolder.get();
                     RarityRenderData renderData = rarity.renderData();
                     int color = rarity.color().getValue();
                     float progress = Mth.clamp(item.tickCount - (Integer)item.getData(AscEq.Attachments.AFFIX_EFFECT_START_TIME) + partials, 0.0F, 15.0F)
                        / 15.0F;
                     double x = Mth.lerp(partials, item.xOld, item.getX());
                     double y = Mth.lerp(partials, item.yOld, item.getY());
                     double z = Mth.lerp(partials, item.zOld, item.getZ());
                     pose.pushPose();
                     pose.translate(x - camPos.x, y - camPos.y, z - camPos.z);
                     float beamHeight = renderData.beamHeight();
                     if (beamHeight > 0.0F) {
                        pose.pushPose();
                        pose.translate(-item.getBbWidth() * 2.0F, 0.0F, -item.getBbWidth() * 2.0F);
                        float beamRadius = renderData.beamRadius();
                        float glowRadius = renderData.glowRadius();
                        float height = beamHeight * progress;
                        BeamRenderer.renderBeaconBeam(
                           pose,
                           collector,
                           renderData.beamTexture(),
                           renderData.glowTexture(),
                           partials,
                           1.0F,
                           gameTime,
                           0.0F,
                           Math.min(height, 0.5F),
                           color(0, color),
                           color(31, color),
                           beamRadius,
                           glowRadius
                        );
                        height -= 0.5F;
                        BeamRenderer.renderBeaconBeam(
                           pose,
                           collector,
                           renderData.beamTexture(),
                           renderData.glowTexture(),
                           partials,
                           1.0F,
                           gameTime,
                           0.5F,
                           Mth.clamp(height, 0.0F, 0.5F),
                           color(31, color),
                           color(159, color),
                           beamRadius,
                           glowRadius
                        );
                        height -= 0.5F;
                        BeamRenderer.renderBeaconBeam(
                           pose,
                           collector,
                           renderData.beamTexture(),
                           renderData.glowTexture(),
                           partials,
                           1.0F,
                           gameTime,
                           1.0F,
                           Mth.clamp(height, 0.0F, 0.5F),
                           color(159, color),
                           color(159, color),
                           beamRadius,
                           glowRadius
                        );
                        height -= 0.5F;
                        BeamRenderer.renderBeaconBeam(
                           pose,
                           collector,
                           renderData.beamTexture(),
                           renderData.glowTexture(),
                           partials,
                           1.0F,
                           gameTime,
                           1.5F,
                           Mth.clamp(height, 0.0F, beamHeight),
                           color(159, color),
                           color(0, color),
                           beamRadius,
                           glowRadius
                        );
                        pose.popPose();
                     }

                     RarityRenderData.ShadowData shadow = renderData.shadow();
                     ShadowRenderer.renderShadow(pose, collector, item, partials, mc.level, shadow, ARGB.color(shadow.alpha(), color));
                     pose.popPose();
                  } else {
                     item.setData(AscEq.Attachments.AFFIX_EFFECT_RENDER_STARTED, false);
                  }
               }
            }
         }
      }
   }

   private static int color(int alpha, int color) {
      return ARGB.color(alpha, color);
   }

   @SubscribeEvent
   public static void spawnParticles(Post e) {
      if (EquipmentConfig.enableAffixItemEffects) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && !mc.isPaused()) {
            for (Entity ent : mc.level.entitiesForRendering()) {
               if (ent instanceof ItemEntity item) {
                  ItemStack stack = item.getItem();
                  DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(stack);
                  if (rarityHolder.isBound() && item.onGround()) {
                     LootRarity rarity = (LootRarity)rarityHolder.get();
                     if (rarity.renderData().particle().enabled()) {
                        int delay = (Integer)item.getData(AscEq.Attachments.AFFIX_EFFECT_NEXT_PARTICLE_TIME);
                        if (item.tickCount - delay > 0) {
                           int color = rarity.color().getValue();
                           RarityParticleData opt = new RarityParticleData(ARGB.red(color) / 255.0F, ARGB.green(color) / 255.0F, ARGB.blue(color) / 255.0F);
                           RandomSource rand = item.getRandom();
                           double spread = 0.1;
                           mc.level
                              .addParticle(
                                 opt,
                                 item.getX() - spread + rand.nextDouble() * 2.0 * spread,
                                 item.getY(),
                                 item.getZ() - spread + rand.nextDouble() * 2.0 * spread,
                                 0.0,
                                 0.03 + 0.005 * rand.nextGaussian(),
                                 0.0
                              );
                           item.setData(AscEq.Attachments.AFFIX_EFFECT_NEXT_PARTICLE_TIME, item.tickCount + 10 + rand.nextInt(15));
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
