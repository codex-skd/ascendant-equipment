package com.skd.ascendantequipment;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.affix.effect.FestiveAffix;
import com.skd.ascendantequipment.affix.effect.MagicalArrowAffix;
import com.skd.ascendantequipment.affix.effect.OmneticAffix;
import com.skd.ascendantequipment.affix.effect.RadialAffix;
import com.skd.ascendantequipment.affix.effect.TelepathicAffix;
import com.skd.ascendantequipment.commands.AffixCommand;
import com.skd.ascendantequipment.commands.BossCommand;
import com.skd.ascendantequipment.commands.CategoryCheckCommand;
import com.skd.ascendantequipment.commands.DebugWeightCommand;
import com.skd.ascendantequipment.commands.GemCommand;
import com.skd.ascendantequipment.commands.RarityCommand;
import com.skd.ascendantequipment.commands.ReforgeCommand;
import com.skd.ascendantequipment.commands.SocketCommand;
import com.skd.ascendantequipment.commands.WorldTierCommand;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.net.RadialStatePayload;
import com.skd.ascendantequipment.net.WorldTierPayload;
import com.skd.ascendantequipment.socket.SocketHelper;
import com.skd.ascendantequipment.socket.gem.bonus.special.OmneticBonus;
import com.skd.ascendantequipment.socket.gem.bonus.special.RadialBonus;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.ascendantequipment.tiers.augments.TierAugment;
import com.skd.ascendantequipment.tiers.augments.TierAugmentRegistry;
import com.skd.ascendantequipment.util.RadialUtil;
import com.skd.ascendantattributes.api.AscendantAttributesObjects;
import com.skd.ascendantattributes.event.AttributesCommandEvent;
import com.skd.ascendantattributes.modifiers.StackAttributeModifiersEvent;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.events.AnvilLandEvent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent.Result;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class EquipmentEvents {
    private static ThreadLocal<AtomicBoolean> reentrantLock = ThreadLocal.withInitial(() -> new AtomicBoolean(false));

    @SubscribeEvent
    public void cmds(AttributesCommandEvent e) {
        RarityCommand.register(e.getRoot());
        CategoryCheckCommand.register(e.getRoot());
        ReforgeCommand.register(e.getRoot());
        GemCommand.register(e.getRoot());
        SocketCommand.register(e.getRoot());
        BossCommand.register(e.getRoot());
        AffixCommand.register(e.getRoot());
        WorldTierCommand.register(e.getRoot());
        LiteralArgumentBuilder<CommandSourceStack> debug = Commands.literal("debug").requires(c -> c.hasPermission(4));
        DebugWeightCommand.register(debug, e.getContext());
        e.getRoot().then(debug);
    }

    @SubscribeEvent
    public void affixModifiers(StackAttributeModifiersEvent e) {
        ItemStack stack = e.getItemStack();
        SocketHelper.getGems(stack).addModifiers(e);
        AffixHelper.streamAffixes(stack).forEach(inst -> inst.addModifiers(e));
    }

    @SubscribeEvent
    public void preventBossSuffocate(EntityInvulnerabilityCheckEvent e) {
        if (e.getSource().is(DamageTypes.IN_WALL) && e.getEntity().getPersistentData().contains("apoth.boss")) {
            e.setInvulnerable(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void fireProjectile(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof Projectile proj && !proj.getPersistentData().getBoolean("apoth.generated") && proj.getOwner() instanceof LivingEntity user) {
            ItemStack weapon = user.getUseItem();
            if (weapon.isEmpty()) {
                weapon = user.getMainHandItem();
                if (weapon.isEmpty() || !LootCategory.forItem(weapon).isRanged()) {
                    weapon = user.getOffhandItem();
                }
            }

            if (weapon.isEmpty()) {
                return;
            }

            SocketHelper.getGems(weapon).onProjectileFired(user, proj);
            AffixHelper.streamAffixes(weapon).forEach(a -> a.onProjectileFired(user, proj));
            AffixHelper.copyToProjectile(weapon, proj);
        }
    }

    @SubscribeEvent
    public void impact(ProjectileImpactEvent e) {
        if (e.getProjectile() instanceof Projectile proj) {
            SocketHelper.getGemInstances(proj).forEach(inst -> inst.onProjectileImpact(proj, e.getRayTraceResult()));
            Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(proj);
            affixes.values().forEach(inst -> inst.onProjectileImpact(proj, e.getRayTraceResult(), e.getRayTraceResult().getType()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void modifyIncomingDamageTags(EntityInvulnerabilityCheckEvent e) {
        MagicalArrowAffix.modifyIncomingDamageTags(e);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onDamage(LivingIncomingDamageEvent e) {
        DamageSource src = e.getSource();
        LivingEntity ent = e.getEntity();
        float amount = e.getAmount();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack s = ent.getItemBySlot(slot);
            amount = SocketHelper.getGems(s).onHurt(src, ent, amount);
            Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(s);

            for (AffixInstance inst : affixes.values()) {
                amount = inst.onHurt(src, ent, amount);
            }
        }

        e.setAmount(amount);
    }

    @SubscribeEvent
    public void shieldBlock(LivingShieldBlockEvent e) {
        ItemStack stack = e.getEntity().getUseItem();
        Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        float blocked = e.getBlockedDamage();
        blocked = SocketHelper.getGems(stack).onShieldBlock(e.getEntity(), e.getDamageSource(), blocked);

        for (AffixInstance inst : affixes.values()) {
            blocked = inst.onShieldBlock(e.getEntity(), e.getDamageSource(), blocked);
        }

        if (blocked != e.getOriginalBlockedDamage()) {
            e.setBlockedDamage(blocked);
        }
    }

    @SubscribeEvent
    public void blockBreak(BlockEvent.BreakEvent e) {
        ItemStack stack = e.getPlayer().getMainHandItem();
        SocketHelper.getGems(stack).onBlockBreak(e.getPlayer(), e.getLevel(), e.getPos(), e.getState());
        AffixHelper.streamAffixes(stack).forEach(inst -> inst.onBlockBreak(e.getPlayer(), e.getLevel(), e.getPos(), e.getState()));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void drops(LivingDropsEvent e) {
        if (e.getSource().getEntity() instanceof Player p) {
            AffixHelper.streamAffixes(p.getMainHandItem()).forEach(a -> a.modifyEntityLoot(e));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void deathMark(LivingDeathEvent e) {
        FestiveAffix.markEquipment(e);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void dropsLowest(LivingDropsEvent e) {
        TelepathicAffix.drops(e);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void dropsLowest(BlockDropsEvent e) {
        TelepathicAffix.drops(e);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void festive_removeMarker(LivingDropsEvent e) {
        FestiveAffix.removeMarker(e);
    }

    @SubscribeEvent
    public void harvest(HarvestCheck e) {
        OmneticAffix.harvest(e);
        OmneticBonus.harvest(e);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void speed(BreakSpeed e) {
        OmneticAffix.speed(e);
        OmneticBonus.speed(e);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBreak(BlockEvent.BreakEvent e) {
        RadialAffix.onBreak(e);
        RadialBonus.onBreak(e);
    }

    @SubscribeEvent
    public void gemSmashing(AnvilLandEvent e) {
        Level level = e.getLevel();
        BlockPos pos = e.getPos();

        for (ItemEntity ent : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos))) {
            ItemStack stack = ent.getItem();
            if (stack.is(AscEq.Items.GEM)) {
                ent.setItem(new ItemStack(AscEq.Items.GEM_DUST, stack.getCount()));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void enchLevels(GetEnchantmentLevelEvent e) {
        boolean isReentrant = reentrantLock.get().getAndSet(true);
        if (!isReentrant) {
            if (e.getStack() instanceof ItemStack stack) {
                SocketHelper.getGems(stack).getEnchantmentLevels(e);
                AffixHelper.streamAffixes(stack).forEach(inst -> inst.getEnchantmentLevels(e));
            }
            reentrantLock.get().set(false);
        }
    }

    @SubscribeEvent
    public void update(Post e) {
        Entity entity = e.getEntity();
        if (entity.getPersistentData().contains("apoth.burns_in_sun") && entity.level().isDay() && !entity.level().isClientSide()) {
            float f = entity.getLightLevelDependentMagicValue();
            BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
            boolean flag = entity.isInWaterRainOrBubble() || entity.isInPowderSnow || entity.wasInPowderSnow;
            if (f > 0.5F && entity.getRandom().nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !flag && entity.level().canSeeSky(blockpos)) {
                entity.setRemainingFireTicks(160);
            }
        }
    }

    @SubscribeEvent
    public void despawn(MobDespawnEvent e) {
        if (e.getEntity() instanceof AbstractGolem g && g.tickCount > 12000 && g.getPersistentData().getBoolean("apoth.boss")) {
            Entity player = g.level().getNearestPlayer(g, -1.0);
            if (player != null) {
                double dist = player.distanceToSqr(g);
                int despawnDist = g.getType().getCategory().getDespawnDistance();
                int dsDistSq = despawnDist * despawnDist;
                if (dist > dsDistSq) {
                    e.setResult(Result.ALLOW);
                }
            }
        }
    }

    @SubscribeEvent
    public void clone(Clone e) {
        int oldSeed = e.getOriginal().getPersistentData().getInt("apoth_reforge_seed");
        e.getEntity().getPersistentData().putInt("apoth_reforge_seed", oldSeed);
    }

    @SubscribeEvent
    public void equip(LivingEquipmentChangeEvent e) {
        if (e.getEntity() instanceof ServerPlayer player) {
            AscEq.Triggers.EQUIPPED_ITEM.trigger(player, e.getSlot(), e.getTo());
        }
    }

    @SubscribeEvent
    public void sendWorldTierDataOnJoin(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            PacketDistributor.sendToPlayer(p, new WorldTierPayload(WorldTier.getTier(p)), new CustomPacketPayload[0]);
        }
    }

    @SubscribeEvent
    public void applyMissedTierAugments(EntityJoinLevelEvent e) {
        if (e.getLevel() instanceof ServerLevelAccessor) {
            Entity entity = e.getEntity();
            if (!entity.getData(AscEq.Attachments.TIER_AUGMENTS_APPLIED)) {
                if (entity instanceof Player player) {
                    WorldTier tier = player.getData(AscEq.Attachments.WORLD_TIER);

                    for (TierAugment aug : TierAugmentRegistry.getAugments(tier, TierAugment.Target.PLAYERS)) {
                        aug.apply((ServerLevelAccessor) e.getLevel(), player);
                    }

                    entity.setData(AscEq.Attachments.TIER_AUGMENTS_APPLIED, true);
                }
                else if (entity instanceof Mob mob) {
                    float healthPct = mob.getHealth() / mob.getMaxHealth();
                    Player player = e.getLevel().getNearestPlayer(mob, -1.0);
                    if (player != null) {
                        WorldTier tier = player.getData(AscEq.Attachments.WORLD_TIER);

                        for (TierAugment aug : TierAugmentRegistry.getAugments(tier, TierAugment.Target.MONSTERS)) {
                            aug.apply((ServerLevelAccessor) e.getLevel(), mob);
                        }

                        entity.setData(AscEq.Attachments.TIER_AUGMENTS_APPLIED, true);
                    }

                    mob.setHealth(healthPct * mob.getMaxHealth());
                }
            }
        }
    }

    @SubscribeEvent
    public void sync(OnDatapackSyncEvent e) {
        EquipmentConfig.ConfigPayload payload = new EquipmentConfig.ConfigPayload();
        e.getRelevantPlayers().forEach(p -> PacketDistributor.sendToPlayer(p, payload));
    }

    @SubscribeEvent
    public void recordColdDamage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e) {
        if (e.getSource().is(AscendantAttributesObjects.DamageTypes.COLD_DAMAGE)) {
            LivingEntity entity = e.getEntity();
            entity.setData(AscEq.Attachments.COLD_DAMAGE_TAKEN, entity.getData(AscEq.Attachments.COLD_DAMAGE_TAKEN) + e.getNewDamage());
        }
    }

    @SubscribeEvent
    public void stackedOnOther(ItemStackedOnOtherEvent e) {
        Slot slot = e.getSlot();
        SlotAccess access = e.getCarriedSlotAccess();
        if (e.getClickAction() == ClickAction.SECONDARY && e.getCarriedItem().is(AscEq.Items.GEM) && slot.allowModification(e.getPlayer())) {
            ItemStack stack = e.getStackedOnItem();
            ItemStack gemStack = e.getCarriedItem();
            ItemStack socketed = SocketHelper.socketGemInItem(stack, gemStack);
            if (!socketed.isEmpty()) {
                slot.set(socketed);
                access.set(gemStack.copyWithCount(gemStack.getCount() - 1));
                e.setCanceled(true);
                e.getPlayer().playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 1.0F, 1.5F + 0.35F * (1.0F - 2.0F * e.getPlayer().getRandom().nextFloat()));
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void removeCloudsOnDeath(LivingDeathEvent e) {
        if (e.getEntity().getPersistentData().getBoolean("apoth.miniboss")) {
            for (Entity passenger : e.getEntity().getPassengers()) {
                if (passenger instanceof AreaEffectCloud cloud) {
                    cloud.discard();
                }
            }
        }
    }

    @SubscribeEvent
    public void syncRadialState(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new RadialStatePayload(RadialUtil.RadialState.getState(player)), new CustomPacketPayload[0]);
        }
    }
}
