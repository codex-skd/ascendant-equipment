package com.skd.ascendantequipment.util;

import com.google.common.base.Preconditions;
import com.skd.commontoolkit.config.Configuration;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class NameHelper {

    private static String[] names = new String[] {
            "Biscuit",
            "Elisande",
            "Willow",
            "Bippy",
            "Butto",
            "Prim",
            "Tyrael",
            "Bajorno",
            "Michael Morbius",
            "Morbius",
            "Arun",
            "Panez",
            "Doomsday",
            "Vanamar",
            "WhatTheDrunk",
            "Lothrazar",
            "Chelly",
            "Chelicia",
            "Darsh",
            "Dariush",
            "Cheese E Piloza",
            "Bing",
            "Royal",
            "NoWayHere",
            "SwankyStella",
            "Isosahedron",
            "Asfalis",
            "Biz",
            "Icicle",
            "Darko",
            "Shadows",
            "Katarina",
            "Faellynna",
            "Diliviel",
            "Jank",
            "Albert",
            "Andrew",
            "Anderson",
            "Andy",
            "Allan",
            "Arthur",
            "Aaron",
            "Allison",
            "Arielle",
            "Amanda",
            "Anne",
            "Annie",
            "Amy",
            "Alana",
            "Brandon",
            "Brady",
            "Bernard",
            "Ben",
            "Benjamin",
            "Bob",
            "Bobette",
            "Brooke",
            "Brandy",
            "Beatrice",
            "Bea",
            "Bella",
            "Becky",
            "Carlton",
            "Carl",
            "Calvin",
            "Cameron",
            "Carson",
            "Chase",
            "Cassandra",
            "Cassie",
            "Cas",
            "Carol",
            "Carly",
            "Cherise",
            "Charlotte",
            "Cheryl",
            "Chasity",
            "Danny",
            "Drake",
            "Daniel",
            "Derrel",
            "David",
            "Dave",
            "Donovan",
            "Don",
            "Donald",
            "Drew",
            "Derrick",
            "Darla",
            "Donna",
            "Dora",
            "Danielle",
            "Edward",
            "Elliot",
            "Ed",
            "Edson",
            "Elton",
            "Eddison",
            "Earl",
            "Eric",
            "Ericson",
            "Eddie",
            "Ediovany",
            "Emma",
            "Elizabeth",
            "Eliza",
            "Esperanza",
            "Esper",
            "Esmeralda",
            "Emi",
            "Emily",
            "Elaine",
            "Fernando",
            "Ferdinand",
            "Fred",
            "Feddie",
            "Fredward",
            "Frank",
            "Franklin",
            "Felix",
            "Felicia",
            "Fran",
            "Greg",
            "Gregory",
            "George",
            "Gerald",
            "Gina",
            "Geraldine",
            "Gabby",
            "Hendrix",
            "Henry",
            "Hobbes",
            "Herbert",
            "Heath",
            "Henderson",
            "Helga",
            "Hera",
            "Helen",
            "Helena",
            "Hannah",
            "Ike",
            "Issac",
            "Israel",
            "Ismael",
            "Irlanda",
            "Isabelle",
            "Irene",
            "Irenia",
            "Jimmy",
            "Jim",
            "Justin",
            "Jacob",
            "Jake",
            "Jon",
            "Johnson",
            "Jonny",
            "Jonathan",
            "Josh",
            "Joshua",
            "Julian",
            "Jesus",
            "Jericho",
            "Jeb",
            "Jess",
            "Joan",
            "Jill",
            "Jillian",
            "Jessica",
            "Jennifer",
            "Jenny",
            "Jen",
            "Judy",
            "Kenneth",
            "Kenny",
            "Ken",
            "Keith",
            "Kevin",
            "Karen",
            "Kassandra",
            "Kassie",
            "Leonard",
            "Leo",
            "Leroy",
            "Lee",
            "Lenny",
            "Luke",
            "Lucas",
            "Liam",
            "Lorraine",
            "Latasha",
            "Lauren",
            "Laquisha",
            "Livia",
            "Lydia",
            "Lila",
            "Lilly",
            "Lillian",
            "Lilith",
            "Lana",
            "Mason",
            "Mike",
            "Mickey",
            "Mario",
            "Manny",
            "Mark",
            "Marcus",
            "Martin",
            "Marty",
            "Matthew",
            "Matt",
            "Max",
            "Maximillian",
            "Marth",
            "Mia",
            "Marriah",
            "Maddison",
            "Maddie",
            "Marissa",
            "Miranda",
            "Mary",
            "Martha",
            "Melonie",
            "Melody",
            "Mel",
            "Minnie",
            "Nathan",
            "Nathaniel",
            "Nate",
            "Ned",
            "Nick",
            "Norman",
            "Nicholas",
            "Natasha",
            "Nicki",
            "Nora",
            "Nelly",
            "Nina",
            "Orville",
            "Oliver",
            "Orlando",
            "Owen",
            "Olsen",
            "Odin",
            "Olaf",
            "Ortega",
            "Olivia",
            "Patrick",
            "Pat",
            "Paul",
            "Perry",
            "Pinnochio",
            "Patrice",
            "Patricia",
            "Pennie",
            "Petunia",
            "Patti",
            "Pernelle",
            "Quade",
            "Quincy",
            "Quentin",
            "Quinn",
            "Roberto",
            "Robbie",
            "Rob",
            "Robert",
            "Roy",
            "Roland",
            "Ronald",
            "Richard",
            "Rick",
            "Ricky",
            "Rose",
            "Rosa",
            "Rhonda",
            "Rebecca",
            "Roberta",
            "Sparky",
            "Shiloh",
            "Stephen",
            "Steve",
            "Saul",
            "Sheen",
            "Shane",
            "Sean",
            "Sampson",
            "Samuel",
            "Sammy",
            "Stefan",
            "Sasha",
            "Sam",
            "Susan",
            "Suzy",
            "Shelby",
            "Samantha",
            "Sheila",
            "Sharon",
            "Sally",
            "Stephanie",
            "Sandra",
            "Sandy",
            "Sage",
            "Tim",
            "Thomas",
            "Thompson",
            "Tyson",
            "Tyler",
            "Tom",
            "Tyrone",
            "Timmothy",
            "Tamara",
            "Tabby",
            "Tabitha",
            "Tessa",
            "Tiara",
            "Tyra",
            "Uriel",
            "Ursala",
            "Uma",
            "Victor",
            "Vincent",
            "Vince",
            "Vance",
            "Vinny",
            "Velma",
            "Victoria",
            "Veronica",
            "Wilson",
            "Wally",
            "Wallace",
            "Will",
            "Wilard",
            "William",
            "Wilhelm",
            "Xavier",
            "Xandra",
            "Young",
            "Yvonne",
            "Yolanda",
            "Zach",
            "Zachary"
    };
    private static String[] nameParts = new String[] {
            "Prim",
            "Morb",
            "Ius",
            "Kat",
            "Chel",
            "Bing",
            "Darsh",
            "Jank",
            "Dark",
            "Osto",
            "Grab",
            "Thar",
            "Ger",
            "Ald",
            "Mas",
            "On",
            "O",
            "Din",
            "Thor",
            "Jon",
            "Ath",
            "Burb",
            "En",
            "A",
            "E",
            "I",
            "U",
            "Hab",
            "Bloo",
            "Ena",
            "Dit",
            "Aph",
            "Ern",
            "Bor",
            "Dav",
            "Id",
            "Toast",
            "Son",
            "For",
            "Wen",
            "Lob",
            "Van",
            "Zap",
            "Ear",
            "Ben",
            "Don",
            "Bran",
            "Gro",
            "Jen",
            "Bob",
            "Ette",
            "Ere",
            "Man",
            "Qua",
            "Bro",
            "Cree",
            "Per",
            "Skel",
            "Ton",
            "Zom",
            "Bie",
            "Wolf",
            "End",
            "Er",
            "Pig",
            "Sil",
            "Ver",
            "Fish",
            "Cow",
            "Chic",
            "Ken",
            "Sheep",
            "Squid",
            "Hell",
            "Dra",
            "Gor",
            "Nyx",
            "Fae",
            "Lux",
            "Vex",
            "Hex",
            "Rune",
            "Frost",
            "Flame",
            "Storm",
            "Shade",
            "Dawn",
            "Dusk",
            "Ash",
            "Mist",
            "Might",
            "Fury",
            "Rage",
            "Doom",
            "Grim",
            "Void",
            "Rend",
            "Slay",
            "Ar",
            "Or",
            "Ur",
            "El",
            "Al",
            "Im",
            "Un",
            "En",
            "Ix",
            "Ox",
            "Dire",
            "Dark",
            "Bright",
            "Swift",
            "Glow",
            "Shine",
            "Gleam",
            "Spark"
    };
    private static String[] prefixes = new String[] {
            "Dr. Michael",
            "Sir",
            "Mister",
            "Madam",
            "Doctor",
            "Father",
            "Mother",
            "Poppa",
            "Lord",
            "Lady",
            "Overseer",
            "Professor",
            "Mr.",
            "Mr. President",
            "Duke",
            "Duchess",
            "Dame",
            "The Honorable",
            "Chancellor",
            "Vice-Chancellor",
            "His Holiness",
            "Reverend",
            "Count",
            "Viscount",
            "Earl",
            "Captain",
            "Major",
            "General",
            "Senpai",
            "Discount"
    };
    private static String[] suffixes = new String[] {
            "Morbius",
            "Dragonborn",
            "Rejected",
            "Mighty",
            "Supreme",
            "Superior",
            "Ultimate",
            "Lame",
            "Wimpy",
            "Curious",
            "Sneaky",
            "Pathetic",
            "Crying",
            "Eagle",
            "Errant",
            "Unholy",
            "Questionable",
            "Mean",
            "Hungry",
            "Thirsty",
            "Feeble",
            "Wise",
            "Sage",
            "Magical",
            "Mythical",
            "Legendary",
            "Not Very Nice",
            "Jerk",
            "Doctor",
            "Misunderstood",
            "Angry",
            "Knight",
            "Bishop",
            "Godly",
            "Special",
            "Toasty",
            "Shiny",
            "Shimmering",
            "Light",
            "Dark",
            "Odd-Smelling",
            "Funky",
            "Rock Smasher",
            "Son of Herobrine",
            "Cracked",
            "Sticky",
            "§kAlien§r",
            "Baby",
            "Manly",
            "Rough",
            "Scary",
            "Undoubtable",
            "Honest",
            "Non-Suspicious",
            "Boring",
            "Odd",
            "Lazy",
            "Super",
            "Nifty",
            "Ogre Slayer",
            "Pig Thief",
            "Dirt Digger",
            "Really Cool",
            "Doominator",
            "... Something",
            "Extra-Fishy",
            "Gorilla Slaughterer",
            "Marbles Winner",
            "AC Rizzlord",
            "President",
            "Burger Chef",
            "Professional Animator",
            "Cheese Sprayer",
            "Happiness Advocate",
            "Ghost Hunter",
            "Head of Potatoes",
            "Ninja",
            "Warrior",
            "Pyromancer",
            "Trombone Player",
            "Airport Technician",
            "Grand Magistrix",
            "Starved",
            "Terrifying",
            "Expert Cloud Watcher",
            "Cookie Enthusiast",
            "Grass Toucher",
            "Coffee Addict",
            "Mildly Confused"
    };
    private static String[] helms = new String[] { "Helmet", "Cap", "Crown", "Great Helm", "Bassinet", "Sallet", "Close Helm", "Barbute" };
    private static String[] chestplates = new String[] { "Chestplate", "Tunic", "Brigandine", "Hauberk", "Cuirass" };
    private static String[] leggings = new String[] { "Leggings", "Pants", "Tassets", "Cuisses", "Schynbalds" };
    private static String[] boots = new String[] { "Boots", "Shoes", "Greaves", "Sabatons", "Sollerets" };
    private static String[] swords = new String[] {
            "Sword",
            "Cutter",
            "Slicer",
            "Dicer",
            "Knife",
            "Blade",
            "Machete",
            "Brand",
            "Claymore",
            "Cutlass",
            "Foil",
            "Dagger",
            "Glaive",
            "Rapier",
            "Saber",
            "Scimitar",
            "Shortsword",
            "Longsword",
            "Broadsword",
            "Calibur"
    };
    private static String[] axes = new String[] { "Axe", "Chopper", "Hatchet", "Tomahawk", "Cleaver", "Hacker", "Tree-Cutter", "Truncator" };
    private static String[] pickaxes = new String[] { "Pickaxe", "Pick", "Mattock", "Rock-Smasher", "Miner" };
    private static String[] shovels = new String[] { "Shovel", "Spade", "Digger", "Excavator", "Trowel", "Scoop" };
    private static String[] bows = new String[] {
            "Bow", "Shortbow", "Longbow", "Flatbow", "Recurve Bow", "Reflex Bow", "Self Bow", "Composite Bow", "Arrow-Flinger"
    };
    private static String[] shields = new String[] { "Shield", "Buckler", "Targe", "Greatshield", "Blockade", "Bulwark", "Tower Shield", "Protector", "Aegis" };

    private static Map<String, String[]> materialNames = new LinkedHashMap<>();

    public static String suffixFormat = "%s the %s";
    public static String ownershipFormat = "%s's";
    public static String chainFormat = "%s %s";

    public static String nameFromParts(RandomSource random) {
        String name = nameParts[random.nextInt(nameParts.length)] + nameParts[random.nextInt(nameParts.length)].toLowerCase();
        if (random.nextFloat() < 0.4F) {
            name = name + nameParts[random.nextInt(nameParts.length)].toLowerCase();
        }
        if (random.nextFloat() < 0.15F) {
            name = name + nameParts[random.nextInt(nameParts.length)].toLowerCase();
        }
        return name;
    }

    public static String setEntityName(RandomSource rand, Mob entity) {
        String root;
        if (names.length > 0 && nameParts.length > 0) {
            root = rand.nextFloat() < 0.45F ? names[rand.nextInt(names.length)] : nameFromParts(rand);
        }
        else if (names.length > 0) {
            root = names[rand.nextInt(names.length)];
        }
        else {
            root = nameFromParts(rand);
        }

        String name = root;
        if (rand.nextFloat() < 0.3F && prefixes.length > 0) {
            name = prefixes[rand.nextInt(prefixes.length)] + " " + name;
        }
        if (rand.nextFloat() < 0.8F && suffixes.length > 0) {
            name = String.format(suffixFormat, name, suffixes[rand.nextInt(suffixes.length)]);
        }

        entity.setCustomName(Component.literal(name));
        entity.setCustomNameVisible(true);
        return root;
    }

    public static Component setItemName(RandomSource random, ItemStack stack) {
        MutableComponent name = (MutableComponent) stack.getItem().getName(stack);
        String baseName = name.getString();
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        Tool tool = stack.get(DataComponents.TOOL);
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (tool != null) {
            name = buildMaterialPrefix(random, path, baseName);
            String[] type = new String[] { "Tool" };
            if (stack.is(ItemTags.SWORDS) || stack.canPerformAction(ItemAbilities.SWORD_SWEEP)) {
                type = swords;
            }
            else if (stack.is(ItemTags.AXES) || stack.canPerformAction(ItemAbilities.AXE_STRIP)) {
                type = axes;
            }
            else if (stack.is(ItemTags.PICKAXES)) {
                type = pickaxes;
            }
            else if (stack.is(ItemTags.SHOVELS) || stack.canPerformAction(ItemAbilities.SHOVEL_FLATTEN)) {
                type = shovels;
            }
            else if (stack.has(DataComponents.BLOCKS_ATTACKS)) {
                type = shields;
            }
            name.append(type[random.nextInt(type.length)]);
        }
        else if (stack.getItem() instanceof ProjectileWeaponItem) {
            name = Component.literal(bows[random.nextInt(bows.length)]);
        }
        else if (equippable != null && equippable.slot() != EquipmentSlot.BODY) {
            name = buildMaterialPrefix(random, path, baseName);
            String[] type = new String[] { "Armor" };
            EquipmentSlot slot = equippable.slot();
            if (slot == EquipmentSlot.HEAD) {
                type = helms;
            }
            else if (slot == EquipmentSlot.CHEST) {
                type = chestplates;
            }
            else if (slot == EquipmentSlot.LEGS) {
                type = leggings;
            }
            else if (slot == EquipmentSlot.FEET) {
                type = boots;
            }
            name.append(type[random.nextInt(type.length)]);
        }

        stack.set(DataComponents.CUSTOM_NAME, name.withStyle(name.getStyle().withItalic(false)));
        return name;
    }

    private static MutableComponent buildMaterialPrefix(RandomSource random, String path, String baseName) {
        String[] matNames = findMaterialNames(path);
        return matNames.length == 0 ? Component.literal(stripLastToken(baseName)) : Component.literal(matNames[random.nextInt(matNames.length)] + " ");
    }

    private static String[] findMaterialNames(String path) {
        for (Entry<String, String[]> entry : materialNames.entrySet()) {
            if (path.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new String[0];
    }

    private static String stripLastToken(String baseName) {
        String[] split = baseName.split(" ");
        if (split.length <= 1) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            sb.append(split[i]).append(' ');
        }
        return sb.toString();
    }

    public static void load(Configuration c) {
        names = c.getStringList("Names", "entity", names, "A list of full names, which are used in the generation of boss names. May be empty only if name parts is not empty.");
        nameParts = c.getStringList("Name Parts", "entity", nameParts, "A list of name pieces, which can be spliced together to create full names.  May be empty only if names is not empty.");
        Preconditions.checkArgument(names.length != 0 || nameParts.length != 0, "Both names and name parts are empty in ascendant_equipment/names.cfg, this is not allowed.");
        prefixes = c.getStringList("Prefixes", "entity", prefixes, "A list of prefixes, which are used in the generation of boss names. May be empty.");
        suffixes = c.getStringList("Suffixes", "entity", suffixes, "A list of suffixes, which are used in the generation of boss names. A suffix is always preceeded by \"The\". May be empty.");
        helms = c.getStringList("Helms", "items", helms, "A list of root names for helms, used in the generation of item names. May not be empty.");
        chestplates = c.getStringList("chestplates", "items", chestplates, "A list of root names for chestplates, used in the generation of item names. May not be empty.");
        leggings = c.getStringList("leggings", "items", leggings, "A list of root names for leggings, used in the generation of item names. May not be empty.");
        boots = c.getStringList("boots", "items", boots, "A list of root names for boots, used in the generation of item names. May not be empty.");
        Preconditions.checkArgument(helms.length > 0 && chestplates.length > 0 && leggings.length > 0 && boots.length > 0,
                "Detected empty lists for armor root names in ascendant_equipment/names.cfg, this is not allowed.");
        swords = c.getStringList("swords", "items", swords, "A list of root names for swords, used in the generation of item names. May not be empty.");
        axes = c.getStringList("axes", "items", axes, "A list of root names for axes, used in the generation of item names. May not be empty.");
        pickaxes = c.getStringList("pickaxes", "items", pickaxes, "A list of root names for pickaxes, used in the generation of item names. May not be empty.");
        shovels = c.getStringList("shovels", "items", shovels, "A list of root names for shovels, used in the generation of item names. May not be empty.");
        bows = c.getStringList("bows", "items", bows, "A list of root names for bows, used in the generation of item names. May not be empty.");
        shields = c.getStringList("shields", "items", shields, "A list of root names for shields, used in the generation of item names. May not be empty.");
        Preconditions.checkArgument(swords.length > 0 && axes.length > 0 && pickaxes.length > 0 && shovels.length > 0 && bows.length > 0,
                "Detected empty lists for weapon root names in ascendant_equipment/names.cfg, this is not allowed.");

        Map<String, List<Item>> byMaterial = new LinkedHashMap<>();
        for (String key : materialNames.keySet()) {
            byMaterial.put(key, new ArrayList<>());
        }

        for (Item i : BuiltInRegistries.ITEM) {
            String path = BuiltInRegistries.ITEM.getKey(i).getPath();
            for (String key : byMaterial.keySet()) {
                if (path.contains(key)) {
                    byMaterial.get(key).add(i);
                    break;
                }
            }
        }

        for (Entry<String, List<Item>> e : byMaterial.entrySet()) {
            String key = e.getKey();
            List<Item> items = e.getValue();
            String[] read = c.getStringList(key, "materials", materialNames.get(key), buildMaterialComment(items));
            if (read.length > 0) {
                materialNames.put(key, read);
            }
        }

        suffixFormat = c.getString("Suffix Format", "formatting", suffixFormat, "The format string that will be used when a suffix is applied.");
        ownershipFormat = c.getString("Ownership Format", "formatting", ownershipFormat, "The format string that will be used to indicate ownership.");
        if (c.hasChanged()) {
            c.save();
        }
    }

    private static String buildMaterialComment(List<Item> items) {
        String cmt = "A list of material-based prefix names for items whose registry path contains this fragment. May be empty.\n";
        if (items.isEmpty()) {
            return cmt + "No items currently match this fragment.\n";
        }

        cmt = cmt + "Matching items: ";
        cmt = cmt + items.stream().map(i -> BuiltInRegistries.ITEM.getKey(i).toString()).collect(Collectors.joining(", "));
        return cmt + "\n";
    }

    static {
        materialNames.put("netherite", new String[] { "Burnt", "Embered", "Fiery", "Hellborn", "Flameforged" });
        materialNames.put("diamond", new String[] { "Diamond", "Zircon", "Gemstone", "Jewel", "Crystal" });
        materialNames.put("chainmail", new String[] { "Chainmail", "Chain", "Chain Link", "Scale" });
        materialNames.put("ironwood", new String[] { "Ironwood", "Earthbound", "Oaken", "Ironcapped" });
        materialNames.put("knightmetal", new String[] { "Knightmetal", "Knightly", "Phantom-Forged" });
        materialNames.put("steeleaf", new String[] { "Steeleaf", "Organic", "Natural", "Cobaltstem", "Tungstenpetal" });
        materialNames.put("leather", new String[] { "Leather", "Rawhide", "Lamellar", "Cow Skin" });
        materialNames.put("golden", new String[] { "Golden", "Gold", "Gilt", "Auric", "Ornate" });
        materialNames.put("wooden", new String[] { "Wooden", "Wood", "Hardwood", "Balsa Wood", "Mahogany", "Plywood" });
        materialNames.put("turtle", new String[] { "Tortollan", "Very Tragic", "Environmental", "Organic" });
        materialNames.put("stone", new String[] { "Stone", "Rock", "Marble", "Cobblestone" });
        materialNames.put("fiery", new String[] { "Fiery", "Flaming", "Hydra-Infused", "Infernal" });
        materialNames.put("iron", new String[] { "Iron", "Steel", "Ferrous", "Rusty", "Wrought Iron" });
    }
}
