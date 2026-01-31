package com.agustinbenitez.addoncreator.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinecraftItems {
    private static final List<String> ITEMS = new ArrayList<>();

    static {
        // Building Blocks
        ITEMS.add("minecraft:stone");
        ITEMS.add("minecraft:cobblestone");
        ITEMS.add("minecraft:grass");
        ITEMS.add("minecraft:dirt");
        ITEMS.add("minecraft:planks");
        ITEMS.add("minecraft:log");
        ITEMS.add("minecraft:glass");
        ITEMS.add("minecraft:sand");
        ITEMS.add("minecraft:gravel");
        ITEMS.add("minecraft:gold_block");
        ITEMS.add("minecraft:iron_block");
        ITEMS.add("minecraft:coal_block");
        ITEMS.add("minecraft:brick_block");
        ITEMS.add("minecraft:obsidian");
        ITEMS.add("minecraft:diamond_block");
        ITEMS.add("minecraft:netherrack");
        ITEMS.add("minecraft:soul_sand");
        ITEMS.add("minecraft:quartz_block");

        // Tools & Weapons
        ITEMS.add("minecraft:wooden_sword");
        ITEMS.add("minecraft:wooden_pickaxe");
        ITEMS.add("minecraft:wooden_axe");
        ITEMS.add("minecraft:wooden_shovel");
        ITEMS.add("minecraft:wooden_hoe");
        ITEMS.add("minecraft:stone_sword");
        ITEMS.add("minecraft:stone_pickaxe");
        ITEMS.add("minecraft:stone_axe");
        ITEMS.add("minecraft:stone_shovel");
        ITEMS.add("minecraft:stone_hoe");
        ITEMS.add("minecraft:iron_sword");
        ITEMS.add("minecraft:iron_pickaxe");
        ITEMS.add("minecraft:iron_axe");
        ITEMS.add("minecraft:iron_shovel");
        ITEMS.add("minecraft:iron_hoe");
        ITEMS.add("minecraft:diamond_sword");
        ITEMS.add("minecraft:diamond_pickaxe");
        ITEMS.add("minecraft:diamond_axe");
        ITEMS.add("minecraft:diamond_shovel");
        ITEMS.add("minecraft:diamond_hoe");
        ITEMS.add("minecraft:netherite_sword");
        ITEMS.add("minecraft:netherite_pickaxe");
        ITEMS.add("minecraft:netherite_axe");
        ITEMS.add("minecraft:netherite_shovel");
        ITEMS.add("minecraft:netherite_hoe");
        ITEMS.add("minecraft:bow");
        ITEMS.add("minecraft:arrow");
        ITEMS.add("minecraft:shield");

        // Armor
        ITEMS.add("minecraft:leather_helmet");
        ITEMS.add("minecraft:leather_chestplate");
        ITEMS.add("minecraft:leather_leggings");
        ITEMS.add("minecraft:leather_boots");
        ITEMS.add("minecraft:iron_helmet");
        ITEMS.add("minecraft:iron_chestplate");
        ITEMS.add("minecraft:iron_leggings");
        ITEMS.add("minecraft:iron_boots");
        ITEMS.add("minecraft:diamond_helmet");
        ITEMS.add("minecraft:diamond_chestplate");
        ITEMS.add("minecraft:diamond_leggings");
        ITEMS.add("minecraft:diamond_boots");
        ITEMS.add("minecraft:netherite_helmet");
        ITEMS.add("minecraft:netherite_chestplate");
        ITEMS.add("minecraft:netherite_leggings");
        ITEMS.add("minecraft:netherite_boots");

        // Food
        ITEMS.add("minecraft:apple");
        ITEMS.add("minecraft:bread");
        ITEMS.add("minecraft:porkchop");
        ITEMS.add("minecraft:cooked_porkchop");
        ITEMS.add("minecraft:beef");
        ITEMS.add("minecraft:cooked_beef");
        ITEMS.add("minecraft:chicken");
        ITEMS.add("minecraft:cooked_chicken");
        ITEMS.add("minecraft:carrot");
        ITEMS.add("minecraft:potato");
        ITEMS.add("minecraft:baked_potato");
        ITEMS.add("minecraft:golden_apple");
        ITEMS.add("minecraft:enchanted_golden_apple");

        // Materials
        ITEMS.add("minecraft:stick");
        ITEMS.add("minecraft:coal");
        ITEMS.add("minecraft:charcoal");
        ITEMS.add("minecraft:diamond");
        ITEMS.add("minecraft:iron_ingot");
        ITEMS.add("minecraft:gold_ingot");
        ITEMS.add("minecraft:copper_ingot");
        ITEMS.add("minecraft:netherite_ingot");
        ITEMS.add("minecraft:emerald");
        ITEMS.add("minecraft:lapis_lazuli");
        ITEMS.add("minecraft:redstone");
        ITEMS.add("minecraft:glowstone_dust");
        ITEMS.add("minecraft:gunpowder");
        ITEMS.add("minecraft:string");
        ITEMS.add("minecraft:feather");
        ITEMS.add("minecraft:leather");
        ITEMS.add("minecraft:wheat");
        ITEMS.add("minecraft:sugar_cane");
        ITEMS.add("minecraft:paper");
        ITEMS.add("minecraft:book");
        ITEMS.add("minecraft:slime_ball");
        ITEMS.add("minecraft:egg");
        ITEMS.add("minecraft:bone");
        ITEMS.add("minecraft:bone_meal");

        // Redstone
        ITEMS.add("minecraft:redstone_torch");
        ITEMS.add("minecraft:repeater");
        ITEMS.add("minecraft:comparator");
        ITEMS.add("minecraft:piston");
        ITEMS.add("minecraft:sticky_piston");
        ITEMS.add("minecraft:dispenser");
        ITEMS.add("minecraft:dropper");
        ITEMS.add("minecraft:observer");
        ITEMS.add("minecraft:hopper");
        ITEMS.add("minecraft:tnt");

        Collections.sort(ITEMS);
    }

    public static List<String> getAllItems() {
        return ITEMS;
    }
}
