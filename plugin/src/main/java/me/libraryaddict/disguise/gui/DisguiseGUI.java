package me.libraryaddict.disguise.gui;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class DisguiseGUI {

    public static final Map<UUID, String> OPEN_MENUS = new HashMap<>();
    public static final Map<UUID, Integer> MENU_PAGES = new HashMap<>();

    static final String MENU_MAIN = "main";
    static final String MENU_MOBS = "mobs";
    static final String MENU_MISC = "misc";
    static final String MENU_PLAYERS = "players";
    static final String MENU_MY_DISGUISE = "mydisguise";

    private static final int ITEMS_PER_PAGE = 45;

    private static List<DisguiseType> mobTypesCache = null;
    private static List<DisguiseType> miscTypesCache = null;

    static List<DisguiseType> getMobTypes() {
        if (mobTypesCache == null) {
            mobTypesCache = Arrays.stream(DisguiseType.values()).filter(t -> {
                try {
                    return t.isMob() && t != DisguiseType.MODDED_LIVING;
                } catch (Exception e) {
                    return false;
                }
            }).sorted(Comparator.comparing(Enum::name)).collect(Collectors.toList());
        }
        return mobTypesCache;
    }

    static List<DisguiseType> getMiscTypes() {
        if (miscTypesCache == null) {
            miscTypesCache = Arrays.stream(DisguiseType.values()).filter(t -> {
                try {
                    return !t.isMob() && t != DisguiseType.PLAYER
                            && t != DisguiseType.UNKNOWN && t != DisguiseType.MODDED_MISC
                            && t.getEntityType() != null;
                } catch (Exception e) {
                    return false;
                }
            }).sorted(Comparator.comparing(Enum::name)).collect(Collectors.toList());
        }
        return miscTypesCache;
    }

    static Material getIcon(DisguiseType type) {
        Material egg = Material.matchMaterial(type.name() + "_SPAWN_EGG");
        if (egg != null) return egg;
        switch (type.name()) {
            case "PLAYER":           return Material.PLAYER_HEAD;
            case "ARMOR_STAND":      return Material.ARMOR_STAND;
            case "PAINTING":         return Material.PAINTING;
            case "ITEM_FRAME":       return Material.ITEM_FRAME;
            case "GLOW_ITEM_FRAME":  return Material.GLOW_ITEM_FRAME;
            case "DROPPED_ITEM":     return Material.DIAMOND;
            case "FALLING_BLOCK":    return Material.GRASS_BLOCK;
            case "AREA_EFFECT_CLOUD":return Material.GLASS_BOTTLE;
            case "ENDER_CRYSTAL":    return Material.END_CRYSTAL;
            case "TNT":              return Material.TNT;
            case "MINECART": case "CHEST_MINECART": case "HOPPER_MINECART":
            case "FURNACE_MINECART": case "COMMAND_BLOCK_MINECART":
                                     return Material.MINECART;
            case "FIREBALL": case "SMALL_FIREBALL": case "DRAGON_FIREBALL":
                                     return Material.FIRE_CHARGE;
            case "SNOWBALL":         return Material.SNOWBALL;
            case "EGG":              return Material.EGG;
            case "ENDER_PEARL":      return Material.ENDER_PEARL;
            case "EXPERIENCE_ORB":   return Material.EXPERIENCE_BOTTLE;
            case "WITHER_SKULL":     return Material.WITHER_SKELETON_SKULL;
            case "ARROW": case "SPECTRAL_ARROW": case "TIPPED_ARROW":
                                     return Material.ARROW;
            case "SHULKER_BULLET":   return Material.MAGENTA_SHULKER_BOX;
            case "BLOCK_DISPLAY": case "ITEM_DISPLAY": case "TEXT_DISPLAY":
            case "INTERACTION":      return Material.COMMAND_BLOCK;
            default:                 return Material.SPAWNER;
        }
    }

    static String formatName(DisguiseType type) {
        String raw = type.name().replace("_", " ").toLowerCase(Locale.ENGLISH);
        StringBuilder sb = new StringBuilder();
        for (String word : raw.split(" ")) {
            if (sb.length() > 0) sb.append(" ");
            if (!word.isEmpty()) sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    static ItemStack buildItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack glass() {
        return buildItem(Material.GRAY_STAINED_GLASS_PANE, " ");
    }

    private static ItemStack glassBlack() {
        return buildItem(Material.BLACK_STAINED_GLASS_PANE, " ");
    }

    private static void fillBorder(Inventory inv) {
        int size = inv.getSize();
        int rows = size / 9;
        for (int i = 0; i < 9; i++) inv.setItem(i, glass());
        for (int i = size - 9; i < size; i++) inv.setItem(i, glass());
        for (int row = 1; row < rows - 1; row++) {
            inv.setItem(row * 9, glass());
            inv.setItem(row * 9 + 8, glass());
        }
    }

    // ─────────────────────────────────────────
    //  MENÚ PRINCIPAL
    // ─────────────────────────────────────────

    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§l✦ §r§6LibsDisguises §7- §fMenú §8§l✦");
        fillBorder(inv);
        for (int i = 10; i <= 43; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glassBlack());
        }

        inv.setItem(20, buildItem(Material.ZOMBIE_SPAWN_EGG,
                "§a§l» Mobs",
                "§7Disfrazarte como cualquier",
                "§7criatura del juego.",
                "",
                "§e▶ Clic para abrir"));

        inv.setItem(22, buildItem(Material.PLAYER_HEAD,
                "§b§l» Jugadores",
                "§7Disfrazarte como un jugador",
                "§7online o por nombre.",
                "",
                "§e▶ Clic para abrir"));

        inv.setItem(24, buildItem(Material.ITEM_FRAME,
                "§d§l» Objetos y Misc",
                "§7Disfrazarte como objetos,",
                "§7proyectiles y entidades especiales.",
                "",
                "§e▶ Clic para abrir"));

        boolean isDisguised = DisguiseAPI.isDisguised(player);

        if (isDisguised) {
            inv.setItem(31, buildItem(Material.COMPASS,
                    "§6§l» Mi Disfraz",
                    "§7Gestionar tu disfraz activo.",
                    "",
                    "§e▶ Clic para abrir"));
        }

        inv.setItem(49, isDisguised
                ? buildItem(Material.BARRIER,
                        "§c§l» Quitar Disfraz",
                        "§7Elimina tu disfraz actual.",
                        "",
                        "§e▶ Clic para quitarlo")
                : buildItem(Material.GRAY_DYE,
                        "§7Sin disfraz activo",
                        "§8Equipa un disfraz primero."));

        player.openInventory(inv);
        OPEN_MENUS.put(player.getUniqueId(), MENU_MAIN);
        MENU_PAGES.put(player.getUniqueId(), 0);
    }

    // ─────────────────────────────────────────
    //  MENÚ MOBS
    // ─────────────────────────────────────────

    public static void openMobMenu(Player player, int page) {
        List<DisguiseType> types = getMobTypes();
        int totalPages = Math.max(1, (int) Math.ceil((double) types.size() / ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, 54,
                "§8§l✦ §r§6Mobs §8(§f" + (page + 1) + "§8/§f" + totalPages + "§8) §8§l✦");

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, types.size());
        for (int i = start; i < end; i++) {
            DisguiseType type = types.get(i);
            inv.setItem(i - start, buildItem(getIcon(type),
                    "§a" + formatName(type),
                    "§7Tipo: §fMob",
                    "",
                    "§e▶ Clic para disfrazarte"));
        }

        buildNavRow(inv, page, totalPages, types.size());
        player.openInventory(inv);
        OPEN_MENUS.put(player.getUniqueId(), MENU_MOBS);
        MENU_PAGES.put(player.getUniqueId(), page);
    }

    // ─────────────────────────────────────────
    //  MENÚ MISC
    // ─────────────────────────────────────────

    public static void openMiscMenu(Player player, int page) {
        List<DisguiseType> types = getMiscTypes();
        int totalPages = Math.max(1, (int) Math.ceil((double) types.size() / ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, 54,
                "§8§l✦ §r§6Objetos y Misc §8(§f" + (page + 1) + "§8/§f" + totalPages + "§8) §8§l✦");

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, types.size());
        for (int i = start; i < end; i++) {
            DisguiseType type = types.get(i);
            inv.setItem(i - start, buildItem(getIcon(type),
                    "§d" + formatName(type),
                    "§7Tipo: §fMisc",
                    "",
                    "§e▶ Clic para disfrazarte"));
        }

        buildNavRow(inv, page, totalPages, types.size());
        player.openInventory(inv);
        OPEN_MENUS.put(player.getUniqueId(), MENU_MISC);
        MENU_PAGES.put(player.getUniqueId(), page);
    }

    // ─────────────────────────────────────────
    //  MENÚ JUGADORES
    // ─────────────────────────────────────────

    public static void openPlayerMenu(Player player, int page) {
        List<Player> online = Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toList());

        int totalPages = Math.max(1, (int) Math.ceil((double) online.size() / ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, 54,
                "§8§l✦ §r§6Jugadores §8(§f" + (page + 1) + "§8/§f" + totalPages + "§8) §8§l✦");

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, online.size());
        for (int i = start; i < end; i++) {
            Player target = online.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.setDisplayName("§b" + target.getName());
                meta.setLore(Arrays.asList(
                        "§7Disfrazarte como este jugador.",
                        "",
                        "§e▶ Clic para disfrazarte"));
                head.setItemMeta(meta);
            }
            inv.setItem(i - start, head);
        }

        buildNavRow(inv, page, totalPages, online.size());
        player.openInventory(inv);
        OPEN_MENUS.put(player.getUniqueId(), MENU_PLAYERS);
        MENU_PAGES.put(player.getUniqueId(), page);
    }

    // ─────────────────────────────────────────
    //  MENÚ MI DISFRAZ
    // ─────────────────────────────────────────

    public static void openMyDisguiseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§l✦ §r§6Mi Disfraz Actual §8§l✦");

        for (int i = 0; i < 27; i++) inv.setItem(i, glassBlack());
        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,19,20,21,22,23,24,25,26};
        for (int s : border) inv.setItem(s, glass());

        boolean isDisguised = DisguiseAPI.isDisguised(player);

        inv.setItem(11, buildItem(Material.COMPASS,
                "§6§lDisfraz Actual",
                "§7Estado: " + (isDisguised ? "§aActivo" : "§cInactivo")));

        inv.setItem(13, buildItem(Material.EYE_OF_ENDER,
                "§e§lVer Mi Disfraz",
                "§7Activa o desactiva ver",
                "§7tu propio disfraz.",
                "",
                "§e▶ Clic para alternar"));

        if (isDisguised) {
            inv.setItem(15, buildItem(Material.BOOK,
                    "§b§lClonar Disfraz",
                    "§7Copia tu disfraz para",
                    "§7usarlo después.",
                    "",
                    "§e▶ Clic para clonar"));

            inv.setItem(22, buildItem(Material.BARRIER,
                    "§c§lQuitar Disfraz",
                    "§7Elimina tu disfraz activo.",
                    "",
                    "§e▶ Clic para quitarlo"));
        }

        inv.setItem(18, buildItem(Material.ARROW,
                "§7§l« Volver al Menú",
                "§7Regresar al menú principal."));

        player.openInventory(inv);
        OPEN_MENUS.put(player.getUniqueId(), MENU_MY_DISGUISE);
        MENU_PAGES.put(player.getUniqueId(), 0);
    }

    // ─────────────────────────────────────────
    //  MANEJADOR DE CLICS
    // ─────────────────────────────────────────

    public static void handleClick(Player player, int slot) {
        String menu = OPEN_MENUS.get(player.getUniqueId());
        int page = MENU_PAGES.getOrDefault(player.getUniqueId(), 0);
        if (menu == null) return;

        switch (menu) {
            case MENU_MAIN:        handleMainClick(player, slot);         break;
            case MENU_MOBS:        handleListClick(player, slot, page, MENU_MOBS);   break;
            case MENU_MISC:        handleListClick(player, slot, page, MENU_MISC);   break;
            case MENU_PLAYERS:     handlePlayerClick(player, slot, page); break;
            case MENU_MY_DISGUISE: handleMyDisguiseClick(player, slot);   break;
        }
    }

    private static void handleMainClick(Player player, int slot) {
        switch (slot) {
            case 20: openMobMenu(player, 0); break;
            case 22: openPlayerMenu(player, 0); break;
            case 24: openMiscMenu(player, 0); break;
            case 31:
                if (DisguiseAPI.isDisguised(player)) openMyDisguiseMenu(player);
                break;
            case 49:
                if (DisguiseAPI.isDisguised(player)) {
                    player.closeInventory();
                    player.performCommand("undisguise");
                }
                break;
        }
    }

    private static void handleListClick(Player player, int slot, int page, String menuType) {
        List<DisguiseType> types = menuType.equals(MENU_MOBS) ? getMobTypes() : getMiscTypes();
        int totalPages = Math.max(1, (int) Math.ceil((double) types.size() / ITEMS_PER_PAGE));

        if (slot == 45 && page > 0) {
            if (menuType.equals(MENU_MOBS)) openMobMenu(player, page - 1);
            else openMiscMenu(player, page - 1);
            return;
        }
        if (slot == 48) { openMainMenu(player); return; }
        if (slot == 53 && page < totalPages - 1) {
            if (menuType.equals(MENU_MOBS)) openMobMenu(player, page + 1);
            else openMiscMenu(player, page + 1);
            return;
        }
        if (slot >= 45) return;

        int index = page * ITEMS_PER_PAGE + slot;
        if (index >= types.size()) return;

        DisguiseType type = types.get(index);
        player.closeInventory();
        player.performCommand("disguise " + type.name().toLowerCase(Locale.ENGLISH));
    }

    private static void handlePlayerClick(Player player, int slot, int page) {
        List<Player> online = Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toList());
        int totalPages = Math.max(1, (int) Math.ceil((double) online.size() / ITEMS_PER_PAGE));

        if (slot == 45 && page > 0)             { openPlayerMenu(player, page - 1); return; }
        if (slot == 48)                          { openMainMenu(player); return; }
        if (slot == 53 && page < totalPages - 1) { openPlayerMenu(player, page + 1); return; }
        if (slot >= 45) return;

        int index = page * ITEMS_PER_PAGE + slot;
        if (index >= online.size()) return;

        Player target = online.get(index);
        player.closeInventory();
        player.performCommand("disguise player " + target.getName());
    }

    private static void handleMyDisguiseClick(Player player, int slot) {
        switch (slot) {
            case 13: player.closeInventory(); player.performCommand("disguiseviewself"); break;
            case 15: player.closeInventory(); player.performCommand("disguiseclone"); break;
            case 22: player.closeInventory(); player.performCommand("undisguise"); break;
            case 18: openMainMenu(player); break;
        }
    }

    // ─────────────────────────────────────────
    //  UTILIDADES COMPARTIDAS
    // ─────────────────────────────────────────

    private static void buildNavRow(Inventory inv, int page, int totalPages, int total) {
        for (int i = 45; i < 54; i++) inv.setItem(i, glass());
        if (page > 0) {
            inv.setItem(45, buildItem(Material.ARROW,
                    "§f§l◀ Página anterior", "§7Ir a la página " + page));
        }
        inv.setItem(48, buildItem(Material.BARRIER,
                "§c§l✖ Volver al menú", "§7Regresar al menú principal."));
        inv.setItem(49, buildItem(Material.PAPER,
                "§f§lPágina §e" + (page + 1) + " §fde §e" + totalPages,
                "§7Total: §f" + total + " elementos"));
        if (page < totalPages - 1) {
            inv.setItem(53, buildItem(Material.ARROW,
                    "§f§lPágina siguiente ▶", "§7Ir a la página " + (page + 2)));
        }
    }
}
