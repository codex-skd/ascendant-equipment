package com.skd.ascendantequipment.client;

import com.skd.ascendantequipment.particle.RarityParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

public class RarityParticle extends TextureSheetParticle {
    public RarityParticle(
        RarityParticleData data, ClientLevel level, double x, double y, double z, double velX, double velY, double velZ, SpriteSet sprites
    ) {
        super(level, x, y, z, velX, velY, velZ);
        this.setSpriteFromAge(sprites);
        this.rCol = data.red();
        this.gCol = data.green();
        this.bCol = data.blue();
        this.lifetime = 80;
        this.xd = velX;
        this.yd = velY;
        this.zd = velZ;
        this.speedUpWhenYMotionIsBlocked = true;
        this.friction = 1.0F;
        this.quadSize = 0.05F + 0.03F * (float) level.getRandom().nextGaussian();
    }

    @Override
    protected int getLightColor(float partialTicks) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return 0.75F * this.quadSize * Mth.clamp((this.age + partialTick) / this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = 0.75F * (1.0F - (float) this.age / this.lifetime);
    }

    public static class Provider implements ParticleProvider<RarityParticleData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(
            RarityParticleData data, ClientLevel level, double x, double y, double z, double vx, double vy, double vz
        ) {
            return new RarityParticle(data, level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
