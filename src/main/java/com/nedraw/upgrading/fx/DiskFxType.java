package com.nedraw.upgrading.fx;

public enum DiskFxType {

    // 8/32 [8/9,0/9,0/7,0/6,0/1]
    EFFICIENT("efficient", Motion.RISE, Audience.SELF, 8, 0.75f, 1.0, 8, 27, 0, false, 1.0f, false),
    FEATHER_FALL("feather_fall", Motion.RING, Audience.NEARBY, 10, 0.95f, 0.1, 20, 27, 0, false, 1.0f, false),
    LIGHTWEIGHT("lightweight", Motion.SPIRAL, Audience.SELF, 6, 0.75f, 0.2, 20, 27, 0, false, 1.0f, false),
    MAGNET("magnet", Motion.IMPLODE, Audience.SELF, 6, 0.65f, 0.1, 30, 27, 0, false, 1.0f, false),
    MIGHTY_MINER("mighty_miner", Motion.BURST, Audience.NEARBY, 10, 0.8f, 0.5, 0, 27, 0, false, 1.0f, true),
    NIGHT_VISION("night_vision", Motion.RING, Audience.SELF, 8, 0.85f, 0.1, 60, 27, 0, false, 1.0f, true),
    SEA_FISH("sea_fish", Motion.SPIRAL, Audience.NEARBY, 10, 0.85f, 1.8, 100, 27, 0, false, 1.0f, false),
    SWIFT_FEET("swift_feet", Motion.BURST, Audience.NEARBY, 6, 0.7f, 0.1, 40, 18, 4, false, 1.3f, false);

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
    private final int displayDuration;
    private final int frameSpeed;
    private final boolean loops;
    private final float fadeOutSpeed;
    private final boolean glow;

    DiskFxType(String id, Motion motion, Audience audience, int particleCount, float volume,
               double height, int cooldownTicks, int displayDuration, int frameSpeed,
               boolean loops, float fadeOutSpeed, boolean glow) {
        this.id = id;
        this.motion = motion;
        this.audience = audience;
        this.particleCount = particleCount;
        this.volume = volume;
        this.height = height;
        this.cooldownTicks = cooldownTicks;
        this.displayDuration = displayDuration;
        this.frameSpeed = frameSpeed;
        this.loops = loops;
        this.fadeOutSpeed = fadeOutSpeed;
        this.glow = glow;
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

    public int getDisplayDuration() {
        return displayDuration;
    }

    public int getFrameSpeed() {
        return frameSpeed;
    }

    public boolean isLoops() {
        return loops;
    }

    public float getFadeOutSpeed() {
        return fadeOutSpeed;
    }

    public boolean isGlow() {
        return glow;
    }
}