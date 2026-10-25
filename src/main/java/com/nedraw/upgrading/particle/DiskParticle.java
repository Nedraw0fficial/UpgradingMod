package com.nedraw.upgrading.particle;

import com.nedraw.upgrading.fx.DiskFxType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class DiskParticle extends TextureSheetParticle {

    private final SpriteSet sprites;
    private final DiskFxType.Motion motion;
    private final double originX;
    private final double originY;
    private final double originZ;
    private final double phase;
    private final double radius;
    private final double angleSpeed;
    private final double elevation;
    private double velX;
    private double velY;
    private double velZ;

    protected DiskParticle(ClientLevel level, double x, double y, double z,
                           DiskFxType.Motion motion, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = sprites;
        this.motion = motion;

        this.phase = Math.random() * Math.PI * 2;
        this.elevation = (Math.random() - 0.5) * Math.PI;
        this.radius = 0.5 + Math.random() * 0.3;
        this.angleSpeed = 0.25 + Math.random() * 0.1;

        double spreadX = 0;
        double spreadZ = 0;
        if (motion == DiskFxType.Motion.RISE || motion == DiskFxType.Motion.FALL) {
            spreadX = (Math.random() - 0.5) * 0.7;
            spreadZ = (Math.random() - 0.5) * 0.7;
        }

        this.originX = x + spreadX;
        this.originY = y;
        this.originZ = z + spreadZ;
        this.setPos(this.originX, this.originY, this.originZ);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.lifetime = 22 + (int) (Math.random() * 10);
        this.hasPhysics = false;
        this.gravity = 0.0f;
        this.quadSize = 0.12f + (float) Math.random() * 0.05f;

        if (motion == DiskFxType.Motion.BURST) {
            double speed = 0.12 + Math.random() * 0.10;
            this.velX = Math.cos(phase) * Math.cos(elevation) * speed;
            this.velY = Math.sin(elevation) * speed + 0.03;
            this.velZ = Math.sin(phase) * Math.cos(elevation) * speed;
        }

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = (float) this.age / (float) this.lifetime;
        double nx = this.x;
        double ny = this.y;
        double nz = this.z;

        switch (motion) {
            case BURST -> {
                velX *= 0.88;
                velY = velY * 0.88 - 0.002;
                velZ *= 0.88;
                nx += velX;
                ny += velY;
                nz += velZ;
            }
            case RISE -> {
                ny += 0.035;
                nx = originX + Math.sin(age * 0.3 + phase) * 0.08;
                nz = originZ + Math.cos(age * 0.3 + phase) * 0.08;
            }
            case SPIRAL -> {
                double angle = phase + age * angleSpeed;
                double r = radius * (1.0 - 0.3 * progress);
                nx = originX + Math.cos(angle) * r;
                nz = originZ + Math.sin(angle) * r;
                ny = originY + progress * 1.4;
            }
            case FALL -> {
                ny = originY + 1.2 - (progress * progress * 1.6 + progress * 0.4);
                nx = originX + Math.sin(age * 0.2 + phase) * 0.06;
                nz = originZ + Math.cos(age * 0.2 + phase) * 0.06;
            }
            case RING -> {
                double r = progress * 1.4;
                nx = originX + Math.cos(phase) * r;
                nz = originZ + Math.sin(phase) * r;
                ny = originY + 0.05;
            }
            case IMPLODE -> {
                double r = 1.1 * (1.0 - progress) * (1.0 - progress);
                nx = originX + Math.cos(phase) * Math.cos(elevation) * r;
                ny = originY + 0.9 + Math.sin(elevation) * r;
                nz = originZ + Math.sin(phase) * Math.cos(elevation) * r;
            }
            case ZIGZAG -> {
                if (age % 3 == 1) {
                    velX = (Math.random() - 0.5) * 0.2;
                    velY = (Math.random() - 0.5) * 0.2 + 0.02;
                    velZ = (Math.random() - 0.5) * 0.2;
                }
                nx += velX;
                ny += velY;
                nz += velZ;
            }
        }

        this.setPos(nx, ny, nz);
        this.alpha = 1.0f - progress * progress;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    public record Provider(SpriteSet sprites, DiskFxType.Motion motion)
            implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new DiskParticle(level, x, y, z, motion, sprites);
        }
    }
}