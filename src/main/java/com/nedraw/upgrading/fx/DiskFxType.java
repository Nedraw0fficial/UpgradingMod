package com.nedraw.upgrading.fx;

public enum DiskFxType {

    // 2/32
    EFFICIENT("efficient", Motion.RISE, Audience.SELF, 8, 0.75f, 1.0, 8),
    FEATHER_FALL("feather_fall", Motion.RING, Audience.NEARBY, 10, 0.95f, 0.1, 20),
    LIGHTWEIGHT("lightweight", Motion.SPIRAL, Audience.SELF, 6, 0.5f, 0.2, 20);

    public enum Motion {
        BURST, RISE, SPIRAL, FALL, RING, IMPLODE, ZIGZAG
    }

    public enum Audience {
        SELF, NEARBY
    }

    private final String id;
    private final Motion motion;
    private final Audience audience;
    private final int particleCount;
    private final float volume;
    private final double height;
    private final int cooldownTicks;

    DiskFxType(String id, Motion motion, Audience audience, int particleCount,
               float volume, double height, int cooldownTicks) {
        this.id = id;
        this.motion = motion;
        this.audience = audience;
        this.particleCount = particleCount;
        this.volume = volume;
        this.height = height;
        this.cooldownTicks = cooldownTicks;
    }

    public String getId() {
        return id;
    }

    public Motion getMotion() {
        return motion;
    }

    public Audience getAudience() {
        return audience;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public float getVolume() {
        return volume;
    }

    public double getHeight() {
        return height;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }
}