package com.skd.ascendantequipment.affix;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.ascendantattributes.modifiers.StackAttributeModifiersEvent;
import com.skd.commontoolkit.codec.CodecProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public abstract class Affix implements CodecProvider<Affix>, TieredWeights.Weighted {
   public static final float MAX_LEVEL = 2.0F;
   public static final float STANDARD_MAX_LEVEL = 1.0F;
   protected final AffixDefinition definition;

   protected Affix(AffixDefinition definition) {
      this.definition = definition;
   }

   public abstract boolean canApplyTo(ItemStack var1, LootCategory var2, LootRarity var3);

   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      return Component.translatable("affix." + this.id() + ".desc", new Object[]{fmt(inst.level())});
   }

   public Component getName(boolean prefix) {
      return prefix ? Component.translatable("affix." + this.id()) : Component.translatable("affix." + this.id() + ".suffix");
   }

   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      return this.getDescription(inst, ctx);
   }

   public boolean isLevelIndependent(AffixInstance inst) {
      return false;
   }

   public void addModifiers(AffixInstance inst, StackAttributeModifiersEvent event) {
   }

   public float getDamageProtection(AffixInstance inst, DamageSource source) {
      return 0.0F;
   }

   public float getDamageBonus(AffixInstance inst, Entity entity) {
      return 0.0F;
   }

   public void doPostAttack(AffixInstance inst, LivingEntity user, Entity target) {
   }

   public void doPostHurt(AffixInstance inst, LivingEntity user, DamageSource source) {
   }

   public void onProjectileFired(AffixInstance inst, LivingEntity user, Projectile projectile) {
   }

   @Nullable
   public InteractionResult onItemUse(AffixInstance inst, UseOnContext ctx) {
      return null;
   }

   public void onProjectileImpact(float level, LootRarity rarity, Projectile proj, HitResult res, Type type) {
   }

   public float onShieldBlock(AffixInstance inst, LivingEntity entity, DamageSource source, float amount) {
      return amount;
   }

   public void onBlockBreak(AffixInstance inst, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
   }

   public float getDurabilityBonusPercentage(AffixInstance inst) {
      return 0.0F;
   }

   public float onHurt(AffixInstance inst, DamageSource src, LivingEntity ent, float amount) {
      return amount;
   }

   public boolean enablesTelepathy() {
      return false;
   }

   public void getEnchantmentLevels(AffixInstance inst, GetEnchantmentLevelEvent event) {
   }

   public void modifyLoot(AffixInstance inst, ObjectArrayList<ItemStack> loot, LootContext ctx) {
   }

   public void modifyEntityLoot(AffixInstance inst, LivingDropsEvent event) {
   }

   public boolean isCompatibleWith(Affix affix) {
      return this != affix
         && !this.definition().exclusiveSet().contains(AffixRegistry.INSTANCE.holder(affix))
         && !affix.definition().exclusiveSet().contains(AffixRegistry.INSTANCE.holder(this));
   }

   public boolean isCompatibleWith(ItemAffixes affixes) {
      return affixes.liveAffixes().allMatch(this::isCompatibleWith);
   }

   @Override
   public String toString() {
      return String.format("Affix: %s", this.id());
   }

   public final AffixDefinition definition() {
      return this.definition;
   }

   @Override
   public final TieredWeights weights() {
      return this.definition.weights();
   }

   public final Identifier id() {
      return AffixRegistry.INSTANCE.getKey(this);
   }

   public static String fmt(float f) {
      return f == (float)((long)f) ? String.format("%d", (long)f) : IAttributeExtension.FORMAT.format(f);
   }

   public static MutableComponent valueBounds(Component min, Component max) {
      return CommonComponents.space()
         .append(Component.translatable("misc.ascendant_equipment.affix_bounds", new Object[]{min, max}).withStyle(ChatFormatting.DARK_GRAY));
   }

   static Identifier makeUniqueId(AffixInstance inst, String salt) {
      Identifier key = inst.affix().getId();
      LootCategory cat = LootCategory.forItem(inst.stack());
      return Identifier.fromNamespaceAndPath(key.getNamespace(), key.getPath() + "_modifier_" + cat.getSlots().id().toShortLanguageKey() + "_" + salt);
   }

   static Identifier makeUniqueId(AffixInstance inst) {
      return makeUniqueId(inst, "");
   }

   protected static <T extends Affix> App<Mu<T>, AffixDefinition> affixDef() {
      return AffixDefinition.CODEC.fieldOf("definition").forGetter(Affix::definition);
   }
}
