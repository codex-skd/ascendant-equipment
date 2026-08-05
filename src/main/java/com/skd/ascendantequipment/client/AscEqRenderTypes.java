package com.skd.ascendantequipment.client;

import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class AscEqRenderTypes {
   public static final BiFunction<Identifier, Boolean, RenderType> BEAM = Util.memoize((texture, translucency) -> RenderTypes.eyes(texture));
   public static final Function<Identifier, RenderType> SHADOW = Util.memoize(RenderTypes::entityShadow);

   public static RenderType affixBeam(Identifier location, boolean colorFlag) {
      return BEAM.apply(location, colorFlag);
   }

   public static RenderType affixShadow(Identifier location) {
      return SHADOW.apply(location);
   }

   private AscEqRenderTypes() {
   }
}
