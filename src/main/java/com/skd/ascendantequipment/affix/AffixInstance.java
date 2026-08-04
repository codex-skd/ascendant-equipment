package com.skd.ascendantequipment.affix;

import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantattributes.modifiers.StackAttributeModifiersEvent;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
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
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public record AffixInstance(DynamicHolder<Affix> affix, float level, DynamicHolder<LootRarity> rarity, ItemStack stack) {
   public LootCategory category() {
      return LootCategory.forItem(this.stack);
   }

   public boolean isValid() {
      return this.affix.isBound() && this.rarity.isBound();
   }

   public LootRarity getRarity() {
      return (LootRarity)this.rarity.get();
   }

   public Affix getAffix() {
      return (Affix)this.affix.get();
   }

   public void addModifiers(StackAttributeModifiersEvent event) {
      this.getAffix().addModifiers(this, event);
   }

   public MutableComponent getDescription(AttributeTooltipContext ctx) {
      return this.getAffix().getDescription(this, ctx);
   }

   public Component getAugmentingText(AttributeTooltipContext ctx) {
      return this.getAffix().getAugmentingText(this, ctx);
   }

   public Component getName(boolean prefix) {
      return this.getAffix().getName(prefix);
   }

   public float getDamageProtection(DamageSource source) {
      return this.getAffix().getDamageProtection(this, source);
   }

   public float getDamageBonus(Entity entity) {
      return this.getAffix().getDamageBonus(this, entity);
   }

   public void doPostAttack(LivingEntity user, Entity target) {
      this.getAffix().doPostAttack(this, user, target);
   }

   public void doPostHurt(LivingEntity user, DamageSource source) {
      this.getAffix().doPostHurt(this, user, source);
   }

   public void onProjectileFired(LivingEntity user, Projectile proj) {
      this.getAffix().onProjectileFired(this, user, proj);
   }

   @Nullable
   public InteractionResult onItemUse(UseOnContext ctx) {
      return this.getAffix().onItemUse(this, ctx);
   }

   public float onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
      return this.getAffix().onShieldBlock(this, entity, source, amount);
   }

   public void onBlockBreak(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
      this.getAffix().onBlockBreak(this, player, world, pos, state);
   }

   public float getDurabilityBonusPercentage() {
      return this.getAffix().getDurabilityBonusPercentage(this);
   }

   public void onProjectileImpact(Projectile proj, HitResult res, Type type) {
      this.getAffix().onProjectileImpact(this.level, this.getRarity(), proj, res, type);
   }

   public boolean enablesTelepathy() {
      return this.getAffix().enablesTelepathy();
   }

   public float onHurt(DamageSource src, LivingEntity ent, float amount) {
      return this.getAffix().onHurt(this, src, ent, amount);
   }

   public void getEnchantmentLevels(GetEnchantmentLevelEvent event) {
      this.getAffix().getEnchantmentLevels(this, event);
   }

   public void modifyLoot(ObjectArrayList<ItemStack> loot, LootContext ctx) {
      this.getAffix().modifyLoot(this, loot, ctx);
   }

   public void modifyEntityLoot(LivingDropsEvent e) {
      this.getAffix().modifyEntityLoot(this, e);
   }

   public boolean isLevelIndependent() {
      return this.getAffix().isLevelIndependent(this);
   }

   public AffixInstance withNewLevel(float level) {
      return new AffixInstance(this.affix, Mth.clamp(level, 0.0F, 2.0F), this.rarity, this.stack);
   }

   public Identifier makeUniqueId(String salt) {
      return Affix.makeUniqueId(this, salt);
   }

   public Identifier makeUniqueId() {
      return Affix.makeUniqueId(this);
   }
}
