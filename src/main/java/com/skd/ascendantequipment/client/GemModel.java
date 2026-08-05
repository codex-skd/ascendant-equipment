package com.skd.ascendantequipment.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemModel.BakingContext;
import net.minecraft.client.resources.model.ResolvableModel.Resolver;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public class GemModel implements ItemModel {
   public static final Map<DynamicHolder<Gem>, StandaloneModelKey<ItemModel>> GEM_MODEL_KEYS = new ConcurrentHashMap<>();
   private final ItemModel fallback;

   public GemModel(ItemModel fallback) {
      this.fallback = fallback;
   }

   public void update(
      ItemStackRenderState output,
      ItemStack stack,
      ItemModelResolver resolver,
      ItemDisplayContext displayContext,
      @Nullable ClientLevel level,
      @Nullable ItemOwner owner,
      int seed
   ) {
      DynamicHolder<Gem> holder = (DynamicHolder<Gem>)stack.get(AscEq.Components.GEM);
      if (holder != null && holder.isBound()) {
         StandaloneModelKey<ItemModel> key = GEM_MODEL_KEYS.get(holder);
         if (key != null) {
            ItemModel model = (ItemModel)Minecraft.getInstance().getModelManager().getStandaloneModel(key);
            if (model != null) {
               model.update(output, stack, resolver, displayContext, level, owner, seed);
               return;
            }
         }
      }

      this.fallback.update(output, stack, resolver, displayContext, level, owner, seed);
   }

   public record Unbaked(net.minecraft.client.renderer.item.ItemModel.Unbaked fallback) implements net.minecraft.client.renderer.item.ItemModel.Unbaked {
      public static final MapCodec<GemModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(ItemModels.CODEC.fieldOf("fallback").forGetter(GemModel.Unbaked::fallback)).apply(i, GemModel.Unbaked::new)
      );

      public MapCodec<GemModel.Unbaked> type() {
         return MAP_CODEC;
      }

      public ItemModel bake(BakingContext context, Matrix4fc transformation) {
         return new GemModel(this.fallback.bake(context, transformation));
      }

      public void resolveDependencies(Resolver resolver) {
         this.fallback.resolveDependencies(resolver);
      }
   }
}
