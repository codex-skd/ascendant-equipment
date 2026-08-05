package com.skd.ascendantequipment.socket.gem;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import com.skd.ascendantattributes.modifiers.StackAttributeModifiersEvent;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

public record GemInstance(DynamicHolder<Gem> gem, LootCategory category, Purity purity, ItemStack gemStack, int slot) implements GemView {
   public static GemInstance EMPTY = new GemInstance(GemRegistry.INSTANCE.emptyHolder(), AscEq.LootCategories.NONE, Purity.CHIPPED, ItemStack.EMPTY, -1);

   public static GemInstance socketed(ItemStack socketed, ItemStack gemStack, int slot) {
      return socketed(LootCategory.forItem(socketed), gemStack, slot);
   }

   public static GemInstance socketed(LootCategory category, ItemStack gemStack, int slot) {
      DynamicHolder<Gem> gem = GemItem.getGem(gemStack);
      Purity purity = GemItem.getPurity(gemStack);
      if (gem.isBound()) {
         purity = Purity.max(((Gem)gem.get()).getMinPurity(), purity);
      }

      return new GemInstance(gem, category, purity, gemStack, slot);
   }

   public Gem getGem() {
      return (Gem)this.gem.get();
   }

   public boolean isValid() {
      return this.gem.isBound() && this.getGem().getBonus(this.category, this.purity).isPresent() && this.slot != -1;
   }

   public boolean isPerfect() {
      return this.purity == Purity.PERFECT;
   }

   public void addInformation(Consumer<Component> list, AttributeTooltipContext ctx) {
      this.getGem().addInformation(this, list, ctx);
   }

   public boolean canApplyTo(ItemStack stack) {
      return ((Gem)this.gem.get()).canApplyTo(stack, this.purity);
   }

   public void addModifiers(StackAttributeModifiersEvent event) {
      this.ifPresent(b -> b.addModifiers(this, event));
   }

   public Component getSocketBonusTooltip(AttributeTooltipContext ctx) {
      return this.<Component>map(b -> b.getSocketBonusTooltip(this, ctx)).orElse(Component.literal("Invalid Gem Category"));
   }

   public float getDamageProtection(DamageSource source) {
      return this.<Float>map(b -> b.getDamageProtection(this, source)).orElse(0.0F);
   }

   public float getDamageBonus(Entity target) {
      return this.<Float>map(b -> b.getDamageBonus(this, target)).orElse(0.0F);
   }

   public void doPostAttack(LivingEntity user, @Nullable Entity target) {
      this.ifPresent(b -> b.doPostAttack(this, user, target));
   }

   public void doPostHurt(LivingEntity user, DamageSource source) {
      this.ifPresent(b -> b.doPostHurt(this, user, source));
   }

   public void onProjectileFired(LivingEntity user, Projectile proj) {
      this.ifPresent(b -> b.onProjectileFired(this, user, proj));
   }

   @Nullable
   public InteractionResult onItemUse(UseOnContext ctx) {
      return this.<InteractionResult>map(b -> b.onItemUse(this, ctx)).orElse(null);
   }

   public void onProjectileImpact(Projectile proj, HitResult res) {
      this.ifPresent(b -> b.onProjectileImpact(this, proj, res));
   }

   public float onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
      return this.<Float>map(b -> b.onShieldBlock(this, entity, source, amount)).orElse(amount);
   }

   public void onBlockBreak(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
      this.ifPresent(b -> b.onBlockBreak(this, player, world, pos, state));
   }

   public float getDurabilityBonusPercentage() {
      return this.<Float>map(b -> b.getDurabilityBonusPercentage(this)).orElse(0.0F);
   }

   public float onHurt(DamageSource src, LivingEntity ent, float amount) {
      return this.<Float>map(b -> b.onHurt(this, src, ent, amount)).orElse(amount);
   }

   public void getEnchantmentLevels(GetEnchantmentLevelEvent event) {
      this.ifPresent(b -> b.getEnchantmentLevels(this, event));
   }

   public void modifyLoot(ObjectArrayList<ItemStack> loot, LootContext ctx) {
      this.ifPresent(b -> b.modifyLoot(this, loot, ctx));
   }

   public void skipModifierIds(Consumer<Identifier> skip) {
      this.ifPresent(b -> b.skipModifierIds(this, skip));
   }

   public Optional<GemBonus> getBonus() {
      return ((Gem)this.gem.get()).getBonus(this.category, this.purity);
   }

   private <T> Optional<T> map(Function<GemBonus, T> function) {
      return this.getBonus().map(function);
   }

   private void ifPresent(Consumer<GemBonus> function) {
      this.getBonus().ifPresent(function);
   }
}
