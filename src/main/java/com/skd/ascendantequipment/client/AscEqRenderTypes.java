package com.skd.ascendantequipment.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

public class AscEqRenderTypes extends RenderType {

    static ShaderInstance grayShader;

    private static final ShaderStateShard RENDER_TYPE_GRAY = new ShaderStateShard(() -> grayShader);

    public static java.util.function.BiFunction<ResourceLocation, Boolean, RenderType> BEAM = Util.memoize(
        (texture, translucency) -> {
            RenderType.CompositeState builder = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_EYES_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(translucency ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
                .setWriteMaskState(translucency ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                .createCompositeState(false);
            return create("ascendant_equipment:beam", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 1536, false, true, builder);
        });

    public static final java.util.function.Function<ResourceLocation, RenderType> SHADOW = Util.memoize(
        texture -> {
            RenderType.CompositeState builder = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_EYES_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(false);
            return create("ascendant_equipment:shadow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, false, builder);
        });

    public static java.util.function.Function<ResourceLocation, RenderType> GRAY = Util.memoize(AscEqRenderTypes::gray);

    public static RenderType affixBeam(ResourceLocation location, boolean colorFlag) {
        return BEAM.apply(location, colorFlag);
    }

    public static RenderType affixShadow(ResourceLocation location) {
        return SHADOW.apply(location);
    }

    private static RenderType gray(ResourceLocation loc) {
        RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
            .setShaderState(RENDER_TYPE_GRAY)
            .setTextureState(new RenderStateShard.TextureStateShard(loc, false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setOutputState(ITEM_ENTITY_TARGET)
            .setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
            .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
            .createCompositeState(true);
        return create("ascendant_equipment:gray", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$state);
    }

    private AscEqRenderTypes(String name, VertexFormat format, Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

}
