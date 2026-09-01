package com.skd.ascendantequipment.socket.gem.bonus;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.bonus.special.AllStatsBonus;
import com.skd.ascendantequipment.socket.gem.bonus.special.BloodyArrowBonus;
import com.skd.ascendantequipment.socket.gem.bonus.special.DropTransformBonus;
import com.skd.ascendantequipment.socket.gem.bonus.special.FrozenDropsBonus;
import com.skd.ascendantequipment.socket.gem.bonus.special.LeechBlockBonus;
import com.skd.ascendantequipment.socket.gem.bonus.special.MageSlayerBonus;
import com.skd.ascendantequipment.socket.gem.bonus.special.OmneticBonus;
import com.skd.ascendantequipment.socket.gem.bonus.special.RadialBonus;
import com.skd.ascendantattributes.modifiers.StackAttributeModifiersEvent;
import com.skd.commontoolkit.codec.CodecMap;
import com.skd.commontoolkit.codec.CodecProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

public abstract class GemBonus implements CodecProvider<GemBonus> {
   public static final CodecMap<GemBonus> CODEC = new CodecMap("Gem Bonus");
   protected final GemClass gemClass;

   public GemBonus(GemClass gemClass) {
      this.gemClass = gemClass;
   }

   public abstract boolean supports(Purity var1);

   public abstract Component getSocketBonusTooltip(GemView var1, AttributeTooltipContext var2);

   public void addModifiers(GemInstance inst, StackAttributeModifiersEvent event) {
   }

   public float getDamageProtection(GemInstance inst, DamageSource source) {
      return 0.0F;
   }

   public float getDamageBonus(GemInstance inst, Entity target) {
      return 0.0F;
   }

   public void doPostAttack(GemInstance inst, LivingEntity user, @Nullable Entity target) {
   }

   public void doPostHurt(GemInstance inst, LivingEntity user, DamageSource source) {
   }

   public void onProjectileFired(GemInstance inst, LivingEntity user, Projectile proj) {
   }

   @Nullable
   public InteractionResult onItemUse(GemInstance inst, UseOnContext ctx) {
      return null;
   }

   public void onProjectileImpact(GemInstance inst, Projectile proj, HitResult res) {
   }

   public float onShieldBlock(GemInstance inst, LivingEntity entity, DamageSource source, float amount) {
      return amount;
   }

   public void onBlockBreak(GemInstance inst, Player player, LevelAccessor level, BlockPos pos, BlockState state) {
   }

   public float getDurabilityBonusPercentage(GemInstance inst) {
      return 0.0F;
   }

   public float onHurt(GemInstance inst, DamageSource src, LivingEntity user, float amount) {
      return amount;
   }

   public void getEnchantmentLevels(GemInstance inst, GetEnchantmentLevelEvent event) {
   }

   public void modifyLoot(GemInstance inst, ObjectArrayList<ItemStack> loot, LootContext ctx) {
   }

   public void skipModifierIds(GemInstance inst, Consumer<ResourceLocation> skip) {
   }

   public final ResourceLocation getTypeKey() {
      return CODEC.getKey(this.getCodec());
   }

   public final GemClass getGemClass() {
      return this.gemClass;
   }

   protected static ResourceLocation makeUniqueId(GemView view, String salt) {
      String path = view.gem().getId().getPath() + "_modifier_";
      if (view instanceof GemInstance inst) {
         path = path + inst.category().getSlots().id().toShortLanguageKey() + "_" + inst.slot();
      }

      return ResourceLocation.fromNamespaceAndPath(view.gem().getId().getNamespace(), path + salt);
   }

   protected static ResourceLocation makeUniqueId(GemView inst) {
      return makeUniqueId(inst, "");
   }

   public static void initCodecs() {
      register("attribute", AttributeBonus.CODEC);
      register("multi_attribute", MultiAttrBonus.CODEC);
      register("durability", DurabilityBonus.CODEC);
      register("damage_reduction", DamageReductionBonus.CODEC);
      register("enchantment", EnchantmentBonus.CODEC);
      register("bloody_arrow", BloodyArrowBonus.CODEC);
      register("leech_block", LeechBlockBonus.CODEC);
      register("all_stats", AllStatsBonus.CODEC);
      register("drop_transform", DropTransformBonus.CODEC);
      register("mageslayer", MageSlayerBonus.CODEC);
      register("mob_effect", MobEffectBonus.CODEC);
      register("frozen_drops", FrozenDropsBonus.CODEC);
      register("omnetic", OmneticBonus.CODEC);
      register("radial", RadialBonus.CODEC);
   }

   protected static <T extends GemBonus> App<Mu<T>, GemClass> gemClass() {
      return GemClass.CODEC.fieldOf("gem_class").forGetter(GemBonus::getGemClass);
   }

   private static void register(String id, Codec<? extends GemBonus> codec) {
      CODEC.register(AscendantEquipment.loc(id), codec);
   }

   public abstract static class Builder {
      public abstract GemBonus build(GemClass var1);
   }
}
