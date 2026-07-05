package com.nedraw.upgrading.advancement;

import com.nedraw.upgrading.UpgradingMod;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ModAdvancementTriggers {

    public static void grant(ServerPlayer player, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, path);
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        if (holder == null) return;

        var progress = player.getAdvancements().getOrStartProgress(holder);
        if (progress.isDone()) return;

        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(holder, criterion);
        }
    }

    // Always grants root first so children can be granted
    private static void grantWithRoot(ServerPlayer player, String path) {
        grant(player, "root"); // ensure root is always granted first
        grant(player, path);
    }

    // BASIC
    public static void AERIAL_DASH(ServerPlayer p)              { grantWithRoot(p, "basic/swift_feet"); }
    public static void UNDERWATER_30S(ServerPlayer p)           { grantWithRoot(p, "basic/sea_fish"); }
    public static void MAGNET_PICKUP_20(ServerPlayer p)         { grantWithRoot(p, "basic/magnet"); }
    public static void BONUS_ORE_FOUND(ServerPlayer p)          { grantWithRoot(p, "basic/mighty_miner"); }
    public static void SEE_INVISIBLE(ServerPlayer p)            { grantWithRoot(p, "basic/night_vision"); }
    public static void LETHAL_FALL_SURVIVED(ServerPlayer p)     { grantWithRoot(p, "basic/feather_fall"); }
    public static void HUNGER_DRAIN_PREVENTED_5(ServerPlayer p) { grantWithRoot(p, "basic/lightweight"); }
    public static void DURABILITY_SAVED_5(ServerPlayer p)       { grantWithRoot(p, "basic/efficient"); }
    public static void TANKY_30_PERCENT(ServerPlayer p)         { grantWithRoot(p, "basic/tanky"); }

    // RARE
    public static void WALK_ON_LAVA(ServerPlayer p)             { grantWithRoot(p, "rare/flame_walker"); }
    public static void STEP_UP_FULL_BLOCK(ServerPlayer p)       { grantWithRoot(p, "rare/step_assist"); }
    public static void GOLDEN_CROP_FOUND(ServerPlayer p)        { grantWithRoot(p, "rare/harvester"); }
    public static void TWIN_BORN(ServerPlayer p)                { grantWithRoot(p, "rare/beast_whisperer"); }
    public static void EDGE_KNOCKBACK_RESISTED(ServerPlayer p)  { grantWithRoot(p, "rare/iron_grip"); }
    public static void CHEST_DUPLICATED(ServerPlayer p)         { grantWithRoot(p, "rare/treasure_sense"); }
    public static void EMERALDS_KEPT(ServerPlayer p)            { grantWithRoot(p, "rare/pawnbroker"); }

    // EPIC
    public static void ALL_8_EFFECTS(ServerPlayer p)            { grantWithRoot(p, "epic/warchemist"); }
    public static void SHIELD_BASH_LAUNCHED(ServerPlayer p)     { grantWithRoot(p, "epic/basher"); }
    public static void ABSORPTION_OVERFLOW(ServerPlayer p)      { grantWithRoot(p, "epic/glutton"); }
    public static void FULL_DISARM(ServerPlayer p)              { grantWithRoot(p, "epic/soapy_hands"); }
    public static void WALL_JUMP(ServerPlayer p)                { grantWithRoot(p, "epic/mountain_goat"); }

    // LEGENDARY
    public static void BERSERKER_RAGE_KILL(ServerPlayer p)      { grantWithRoot(p, "legendary/berserker"); }
    public static void FIRE_SHIELD_EXPLODED(ServerPlayer p)     { grantWithRoot(p, "legendary/pyroclasm"); }
    public static void MAX_REACH_PLACEMENT(ServerPlayer p)      { grantWithRoot(p, "legendary/architects_mind"); }

    // MYTHIC
    public static void NECROMISIS_DETONATED_5(ServerPlayer p)   { grantWithRoot(p, "mythic/necro_archer"); }

    // TIER UNLOCKS — also grant root first, then tier, then check if it's first of that tier
    public static void UNLOCK_BASIC_DISK(ServerPlayer p)        { grant(p, "root"); grant(p, "tier_basic"); }
    public static void UNLOCK_RARE_DISK(ServerPlayer p)         { grant(p, "root"); grant(p, "tier_rare"); }
    public static void UNLOCK_EPIC_DISK(ServerPlayer p)         { grant(p, "root"); grant(p, "tier_epic"); }
    public static void UNLOCK_LEGENDARY_DISK(ServerPlayer p)    { grant(p, "root"); grant(p, "tier_legendary"); }
    public static void UNLOCK_MYTHIC_DISK(ServerPlayer p)       { grant(p, "root"); grant(p, "tier_mythic"); }

    // ROOT + HARDCODED — for encrypted floppy pickup
    public static void ROOT(ServerPlayer p)                     { grant(p, "root"); }
    public static void HARDCODED(ServerPlayer p)                { grant(p, "root"); grant(p, "hardcoded"); }
}