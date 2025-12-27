package com.nedraw.upgrading.disk;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class DiskRegistry {
    private static final Map<String, UpgradeDisk> DISKS = new HashMap<>();

    // Register a disk
    public static void register(UpgradeDisk disk) {
        DISKS.put(disk.getId(), disk);
    }

    // Get a disk by ID
    public static UpgradeDisk getDisk(String id) {
        return DISKS.get(id);
    }

    // Get all registered disks
    public static Collection<UpgradeDisk> getAllDisks() {
        return DISKS.values();
    }

    // Check if a disk exists
    public static boolean diskExists(String id) {
        return DISKS.containsKey(id);
    }
}