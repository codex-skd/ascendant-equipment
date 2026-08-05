package com.skd.ascendantequipment.socket.gem;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.socket.SocketHelper;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.commontoolkit.codec.CodecProvider;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import org.jetbrains.annotations.Nullable;

public class Gem implements CodecProvider<Gem>, TieredWeights.Weighted, Constraints.Constrained {
   public static final Codec<Gem> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            TieredWeights.CODEC.fieldOf("weights").forGetter(TieredWeights.Weighted::weights),
            Constraints.CODEC.optionalFieldOf("constraints", Constraints.EMPTY).forGetter(Constraints.Constrained::constraints),
            Purity.CODEC.optionalFieldOf("min_purity", Purity.CRACKED).forGetter(Gem::getMinPurity),
            GemBonus.CODEC.listOf().fieldOf("bonuses").forGetter(Gem::getBonuses),
            Codec.BOOL.optionalFieldOf("unique", false).forGetter(Gem::isUnique)
         )
         .apply(inst, Gem::new)
   );
   protected final TieredWeights weights;
   protected final Constraints constraints;
   protected final Purity minPurity;
   protected final List<GemBonus> bonuses;
   protected final boolean unique;
   protected final transient Map<LootCategory, GemBonus> bonusMap = new IdentityHashMap<>();
   protected final transient List<GemBonus> extraBonuses = new ArrayList<>();

   public Gem(TieredWeights weights, Constraints constraints, Purity minPurity, List<GemBonus> bonuses, boolean unique) {
      this.weights = weights;
      this.constraints = constraints;
      this.minPurity = minPurity;
      this.bonuses = bonuses;
      this.unique = unique;
      Preconditions.checkArgument(!bonuses.isEmpty(), "No bonuses were provided.");

      for (GemBonus bonus : this.bonuses) {
         this.validateBonus(bonus);

         for (Holder<LootCategory> category : bonus.getGemClass().types()) {
            this.bonusMap.put((LootCategory)category.value(), bonus);
         }
      }
   }

   public void addInformation(GemView gem, Consumer<Component> list, AttributeTooltipContext ctx) {
      if (this.isUnique()) {
         list.accept(Component.translatable("text.ascendant_equipment.unique").withStyle(Style.EMPTY.withColor(13056274)));
         list.accept(CommonComponents.EMPTY);
      }

      Style style = Style.EMPTY.withColor(720650);
      list.accept(Component.translatable("text.ascendant_equipment.socketable_into").withStyle(style));
      addTypeInfo(list, this.bonusMap.keySet().toArray());
      list.accept(CommonComponents.EMPTY);
      list.accept(Component.translatable("text.ascendant_equipment.when_socketed_in").withStyle(ChatFormatting.GOLD));
      Consumer<GemBonus> appendBonusToTooltip = bonus -> {
         if (bonus.supports(gem.purity())) {
            Component modifComp = bonus.getSocketBonusTooltip(gem, ctx);
            Component sum = Component.translatable(
                  "text.ascendant_equipment.dot_prefix",
                  new Object[]{Component.translatable("%s: %s", new Object[]{Component.translatable("gem_class." + bonus.getGemClass().key()), modifComp})}
               )
               .withStyle(ChatFormatting.GOLD);
            list.accept(sum);
         }
      };
      this.bonuses.forEach(appendBonusToTooltip);
      this.extraBonuses.forEach(appendBonusToTooltip);
   }

   public boolean canApplyTo(ItemStack socketed, Purity purity) {
      if (this.isUnique()) {
         List<Gem> gems = SocketHelper.getGems(socketed).streamValidGems().map(GemInstance::gem).<Gem>map(DynamicHolder::get).toList();
         if (gems.contains(this)) {
            return false;
         }
      }

      return this.isValidIn(socketed, purity);
   }

   public boolean isValidIn(ItemStack socketed, Purity purity) {
      LootCategory cat = LootCategory.forItem(socketed);
      return !cat.isNone() && this.bonusMap.containsKey(cat) && this.bonusMap.get(cat).supports(purity);
   }

   public Optional<GemBonus> getBonus(LootCategory cat, Purity purity) {
      return Optional.ofNullable(this.bonusMap.get(cat)).filter(b -> b.supports(purity));
   }

   @Nullable
   public GemBonus getBonus(LootCategory cat) {
      return this.bonusMap.get(cat);
   }

   @Override
   public String toString() {
      return String.format("Gem: %s", this.getId());
   }

   @Override
   public TieredWeights weights() {
      return this.weights;
   }

   @Override
   public Constraints constraints() {
      return this.constraints;
   }

   public Purity getMinPurity() {
      return this.minPurity;
   }

   @Deprecated(forRemoval = true)
   public List<GemBonus> getBonuses() {
      return this.bonuses;
   }

   public boolean isUnique() {
      return this.unique;
   }

   public Codec<? extends Gem> getCodec() {
      return CODEC;
   }

   public final Identifier getId() {
      return GemRegistry.INSTANCE.getKey(this);
   }

   public ItemStack toStack(Purity purity) {
      ItemStack stack = new ItemStack(AscEq.Items.GEM);
      GemItem.setGem(stack, this);
      GemItem.setPurity(stack, Purity.max(purity, this.getMinPurity()));
      return stack;
   }

   public static String fmt(float f) {
      return Affix.fmt(f);
   }

   public static void addTypeInfo(Consumer<Component> list, Object... types) {
      Arrays.sort(types, (c1, c2) -> ((LootCategory)c1).getKey().compareTo(((LootCategory)c2).getKey()));
      Style style = Style.EMPTY.withColor(720650);
      if (types.length < AscEq.BuiltInRegs.LOOT_CATEGORY.size() - 1) {
         StringBuilder sb = new StringBuilder();
         int i = 0;

         while (i < types.length) {
            int rem = Math.min(3, types.length - i);
            Object[] args = new Object[rem];

            for (int r = 0; r < rem; r++) {
               sb.append("%s, ");
               args[r] = Component.translatable(((LootCategory)types[i + r]).getDescIdPlural());
            }

            list.accept(
               Component.translatable("text.ascendant_equipment.dot_prefix", new Object[]{Component.translatable(sb.substring(0, sb.length() - 2), args)})
                  .withStyle(style)
            );
            sb.setLength(0);
            i += rem;
         }
      } else {
         list.accept(Component.translatable("text.ascendant_equipment.dot_prefix", new Object[]{Component.translatable("text.ascendant_equipment.anything")}).withStyle(style));
      }
   }

   private void validateBonus(GemBonus bonus) {
      for (Holder<LootCategory> category : bonus.getGemClass().types()) {
         if (this.bonusMap.containsKey(category.value())) {
            GemBonus conflict = this.bonusMap.get(category.value());
            throw new IllegalArgumentException(
               "Gem Bonus for class %s conflicts with existing bonus for class %s (categories overlap)"
                  .formatted(bonus.getGemClass().key(), conflict.getGemClass().key())
            );
         }
      }
   }

   void appendExtraBonus(GemBonus bonus) {
      this.validateBonus(bonus);
      this.extraBonuses.add(bonus);

      for (Holder<LootCategory> category : bonus.getGemClass().types()) {
         this.bonusMap.put((LootCategory)category.value(), bonus);
      }
   }

   public static class Builder {
      protected final TieredWeights weights;
      protected Constraints constraints = Constraints.EMPTY;
      protected Purity minPurity = Purity.CRACKED;
      protected List<GemBonus> bonuses = new ArrayList<>();
      protected boolean unique = false;

      public Builder(TieredWeights weights) {
         this.weights = weights;
      }

      public Gem.Builder contstraints(Constraints constraints) {
         this.constraints = constraints;
         return this;
      }

      public Gem.Builder minPurity(Purity purity) {
         this.minPurity = purity;
         return this;
      }

      public Gem.Builder bonus(LootCategory cat, GemBonus.Builder builder) {
         return this.bonus(new GemClass(cat), builder);
      }

      public Gem.Builder bonus(GemClass gClass, GemBonus.Builder builder) {
         this.bonuses.add(builder.build(gClass));
         return this;
      }

      public Gem.Builder bonus(GemBonus bonus) {
         this.bonuses.add(bonus);
         return this;
      }

      public Gem.Builder unique() {
         this.unique = true;
         return this;
      }

      public Gem build() {
         return new Gem(this.weights, this.constraints, this.minPurity, this.bonuses, this.unique);
      }
   }
}
