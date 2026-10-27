package com.nedraw.upgrading.particle;

import com.nedraw.upgrading.fx.DiskFxType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;

public class DiskParticle extends TextureSheetParticle {

    private final SpriteSet sprites;
    private final DiskFxType type;
    private final DiskFxType.Motion motion;
    private final int frameCount;
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
                           DiskFxType type, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = sprites;
        this.type = type;
        this.motion = type.getMotion();
        this.frameCount = countFrames(sprites);

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

        this.lifetime = Math.max(2, Math.round(type.getDisplayDuration() * (0.9f + (float) Math.random() * 0.2f)));
        this.hasPhysics = false;
        this.gravity = 0.0f;
        this.quadSize = 0.12f + (float) Math.random() * 0.05f;

        if (motion == DiskFxType.Motion.BURST) {
            double speed = 0.12 + Math.random() * 0.10;
            this.velX = Math.cos(phase) * Math.cos(elevation) * speed;
            this.velY = Math.sin(elevation) * speed + 0.03;
            this.velZ = Math.sin(phase) * Math.cos(elevation) * speed;
        }

        this.updateSprite();
    }

    private static int countFrames(SpriteSet sprites) {
        TextureAtlasSprite previous = sprites.get(0, 64);
        int count = 1;
        for (int k = 1; k <= 64; k++) {
            TextureAtlasSprite current = sprites.get(k, 64);
            if (current != previous) {
                count++;
                previous = current;
            }
        }
        return count;
    }

    private void updateSprite() {
        int frame;
        if (type.getFrameSpeed() <= 0) {
            frame = Math.min(frameCount - 1, (int) ((float) age / (float) lifetime * frameCount));
        } else {
            frame = age / type.getFrameSpeed();
            if (frame >= frameCount) {
                frame = type.isLoops() ? frame % frameCount : frameCount - 1;
            }
        }
        this.setSprite(sprites.get(frame, Math.max(1, frameCount - 1)));
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
        this.alpha = Math.max(0.0f, 1.0f - type.getFadeOutSpeed() * progress * progress);
        this.updateSprite();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        if (type.isGlow()) return 15728880;
        return super.getLightColor(partialTick);
    }

    public record Provider(SpriteSet sprites, DiskFxType type)
            implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new DiskParticle(level, x, y, z, type, sprites);
        }
    }
}