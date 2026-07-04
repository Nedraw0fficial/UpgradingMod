package com.nedraw.upgrading.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class NecromisisParticle extends TextureSheetParticle {

    private final SpriteSet sprites;
    private final double originX;
    private final double originY;
    private final double originZ;
    private double angle;
    private final double orbitSpeed;
    private final double verticalSpeed;

    // Progress 0.0 -> 1.0 over lifetime (drives radius + noise expansion)
    private float progress;

    // Starting orbit radius (tight at bottom)
    private static final double BASE_RADIUS = 0.4;
    // Max orbit radius at the top of the tornado
    private static final double MAX_RADIUS = 1.8;

    // Noise accumulates over time
    private double noiseX;
    private double noiseZ;
    private double noiseVelX;
    private double noiseVelZ;

    protected NecromisisParticle(ClientLevel level, double x, double y, double z,
                                 double dx, double dy, double dz,
                                 SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = sprites;

        this.originX = x;
        this.originY = y;
        this.originZ = z;

        // Random starting angle so particles are spread around the circle
        this.angle = Math.random() * Math.PI * 2;

        // Consistent spin speed with tiny variation
        this.orbitSpeed = 0.20 + Math.random() * 0.03;

        // Upward rise speed
        this.verticalSpeed = 0.028 + Math.random() * 0.008;

        // No noise at start - it builds up over time
        this.noiseX = 0;
        this.noiseZ = 0;
        this.noiseVelX = (Math.random() - 0.5) * 0.002;
        this.noiseVelZ = (Math.random() - 0.5) * 0.002;

        this.lifetime = 75 + (int)(Math.random() * 20);
        this.hasPhysics = false;
        this.gravity = 0.0f;
        this.alpha = 1.0f;
        this.quadSize = 0.22f;
        this.progress = 0f;

        // Start on frame 0
        this.setSprite(sprites.get(0, 10));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // Progress from 0.0 at birth to 1.0 at death
        this.progress = (float) this.age / (float) this.lifetime;

        // Orbit radius EXPANDS with progress — tight at bottom, wide at top
        // Use a curve (squared) so it stays tight for longer then flares out
        double currentRadius = BASE_RADIUS + (MAX_RADIUS - BASE_RADIUS) * (progress * progress);

        // Advance orbit angle
        this.angle += this.orbitSpeed;

        // Noise GROWS with progress — almost zero at start, chaotic at top
        // Scale noise velocity injection by progress
        double noiseStrength = progress * progress * 0.025;
        this.noiseVelX += (Math.random() - 0.5) * noiseStrength;
        this.noiseVelZ += (Math.random() - 0.5) * noiseStrength;

        // Damping — less damping as progress increases so noise accumulates more
        double damping = 0.90 - progress * 0.15; // 0.90 at birth -> 0.75 at death
        this.noiseVelX *= damping;
        this.noiseVelZ *= damping;
        this.noiseX += this.noiseVelX;
        this.noiseZ += this.noiseVelZ;

        // Position: expanding orbit + growing noise
        this.x = originX + Math.cos(angle) * currentRadius + noiseX;
        this.z = originZ + Math.sin(angle) * currentRadius + noiseZ;
        this.y += verticalSpeed;

        // Animate through all 11 frames repeatedly
        int frameIndex = (int)(this.age * 0.7f) % 11;
        this.setSprite(sprites.get(frameIndex, 10));

        this.alpha = 1.0f;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public int getLightColor(float partialTick) {
        return 15728880;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new NecromisisParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}