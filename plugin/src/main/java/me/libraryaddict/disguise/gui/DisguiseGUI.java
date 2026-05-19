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
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class DisguiseGUI {

    // ── Mapas de estado ────────────────────────────────────────────────────
    public static final Map<UUID, String>  OPEN_MENUS   = new HashMap<>();
    public static final Map<UUID, Integer> MENU_PAGES   = new HashMap<>();
    /** UUIDs en transición entre menús — evita que InventoryCloseEvent limpie el mapa */
    public static final Set<UUID>          TRANSITIONING = Collections.synchronizedSet(new HashSet<>());
    /** UUIDs esperando escribir un nombre de jugador en el chat */
    public static final Map<UUID, Long>    AWAITING_NAME = new HashMap<>();

    // ── Referencia al plugin (necesaria para el scheduler) ─────────────────
    public static Plugin PLUGIN = null;

    public static void setPlugin(Plugin p) {
        PLUGIN = p;
    }

    // ── Claves de menú ─────────────────────────────────────────────────────
    static final String MENU_MAIN        = "main";
    static final String MENU_MOBS        = "mobs";
    static final String MENU_MISC        = "misc";
    static final String MENU_PLAYERS     = "players";
    static final String MENU_MY_DISGUISE = "mydisguise";

    private static final int ITEMS_PER_PAGE  = 45;
    private static final long NAME_TIMEOUT_MS = 30_000L;   // 30 segundos

    // ── Caché de tipos ────────────────────────────────────────────────────
    private static List<DisguiseType> mobTypesCache  = null;
    private static List<DisguiseType> miscTypesCache = null;

    static List<DisguiseType> getMobTypes() {
        if (mobTypesCache == null) {
            mobTypesCache = Arrays.stream(DisguiseType.values()).filter(t -> {
                try { return t.isMob() && t != DisguiseType.MODDED_LIVING; }
                catch (Exception e) { return false; }
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
                } catch (Exception e) { return false; }
            }).sorted(Comparator.comparing(Enum::name)).collect(Collectors.toList());
        }
        return miscTypesCache;
    }

    // ── Icono por tipo de disfraz ─────────────────────────────────────────
    static Material getIcon(DisguiseType type) {
        Material egg = Material.matchMaterial(type.name() + "_SPAWN_EGG");
        if (egg != null) return egg;
        switch (type.name()) {
            case "PLAYER":            return Material.PLAYER_HEAD;
            case "ARMOR_STAND":       return Material.ARMOR_STAND;
            case "PAINTING":          return Material.PAINTING;
            case "ITEM_FRAME":        return Material.ITEM_FRAME;
            case "GLOW_ITEM_FRAME":   return Material.GLOW_ITEM_FRAME;
            case "DROPPED_ITEM":      return Material.DIAMOND;
            case "FALLING_BLOCK":     return Material.GRASS_BLOCK;
            case "AREA_EFFECT_CLOUD": return Material.GLASS_BOTTLE;
            case "ENDER_CRYSTAL":     return Material.END_CRYSTAL;
            case "TNT":               return Material.TNT;
            case "MINECART": case "CHEST_MINECART": case "FURNACE_MINECART":
                                      return Material.MINECART;
            case "FIREBALL":          return Material.FIRE_CHARGE;
            case "SNOWBALL":          return Material.SNOWBALL;
            case "EGG":               return Material.EGG;
            case "ENDER_PEARL":       return Material.ENDER_PEARL;
            case "EXPERIENCE_ORB":    return Material.EXPERIENCE_BOTTLE;
            case "WITHER_SKULL":      return Material.WITHER_SKELETON_SKULL;
            case "ARROW": case "SPECTRAL_ARROW":
                                      return Material.ARROW;
            case "SHULKER_BULLET":    return Material.MAGENTA_SHULKER_BOX;
            case "INTERACTION":       return Material.COMMAND_BLOCK;
            default:                  return Material.SPAWNER;
        }
    }

    static String formatName(DisguiseType type) {
        String raw = type.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String word : raw.split(" ")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    // ── Utilidades de ítems ───────────────────────────────────────────────
    static ItemStack buildItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack glass()      { return buildItem(Material.GRAY_STAINED_GLASS_PANE,  " "); }
    private static ItemStack glassBlack() { return buildItem(Material.BLACK_STAINED_GLASS_PANE, " "); }

    private static void fillBorder(Inventory inv) {
        int size = inv.getSize(), rows = size / 9;
        for (int i = 0; i < 9; i++) inv.setItem(i, glass());
        for (int i = size - 9; i < size; i++) inv.setItem(i, glass());
        for (int row = 1; row < rows - 1; row++) {
            inv.setItem(row * 9,     glass());
            inv.setItem(row * 9 + 8, glass());
        }
    }

    // ── Apertura segura (siempre en el siguiente tick) ────────────────────
    /**
     * Abre el inventario en el siguiente tick del servidor.
     * Esto es OBLIGATORIO en Paper 1.20 cuando se llama desde un InventoryClickEvent:
     * abrir un inventario directamente desde el manejador de clic provoca que el
     * servidor ignore la apertura. Programarlo en el siguiente tick evita ese bug.
     */
    private static void openMenu(Player player, Inventory inv, String menuKey, int page) {
        UUID id = player.getUniqueId();
        Runnable task = () -> {
            TRANSITIONING.add(id);
            player.openInventory(inv);
            TRANSITIONING.remove(id);
            OPEN_MENUS.put(id, menuKey);
            MENU_PAGES.put(id, page);
        };
        if (PLUGIN != null) {
            Bukkit.getScheduler().runTask(PLUGIN, task);
        } else {
            task.run();  // fallback (no debería ocurrir)
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  MENÚ PRINCIPAL  (54 slots)
    // ─────────────────────────────────────────────────────────────────────
    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8» §6LibsDisguises §7| §fMenu");
        fillBorder(inv);
        for (int i = 10; i <= 43; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glassBlack());
        }

        // Fila central — navegación principal
        inv.setItem(20, buildItem(Material.ZOMBIE_SPAWN_EGG,
            "§a§lMobs",
            "§7Disfrazarte como cualquier",
            "§7criatura del juego.",
            "", "§e> Clic para abrir"));

        inv.setItem(22, buildItem(Material.PLAYER_HEAD,
            "§b§lJugadores",
            "§7Jugadores online + offline.",
            "", "§e> Clic para abrir"));

        inv.setItem(24, buildItem(Material.ITEM_FRAME,
            "§d§lObjetos y Misc",
            "§7Entidades especiales,",
            "§7proyectiles, bloques cayendo...",
            "", "§e> Clic para abrir"));

        boolean isDisguised = DisguiseAPI.isDisguised(player);

        // Mi Disfraz — solo visible si está disfrazado
        if (isDisguised) {
            inv.setItem(31, buildItem(Material.COMPASS,
                "§6§lMi Disfraz",
                "§7Ver y gestionar tu disfraz activo.",
                "", "§e> Clic para abrir"));
        }

        // Quitar Disfraz — SIEMPRE visible
        if (isDisguised) {
            inv.setItem(49, buildItem(Material.BARRIER,
                "§c§lQuitar Disfraz",
                "§7Elimina tu disfraz actual.",
                "", "§e> Clic para quitarlo"));
        } else {
            inv.setItem(49, buildItem(Material.GRAY_DYE,
                "§7Sin disfraz activo",
                "§8No tienes ningun disfraz puesto."));
        }

        openMenu(player, inv, MENU_MAIN, 0);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  MENÚ MOBS
    // ─────────────────────────────────────────────────────────────────────
    public static void openMobMenu(Player player, int page) {
        List<DisguiseType> types = getMobTypes();
        int total = types.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, 54,
            "§8» §6Mobs §7(" + (page + 1) + "/" + totalPages + ")");

        int start = page * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, total);
        for (int i = start; i < end; i++) {
            DisguiseType t = types.get(i);
            inv.setItem(i - start, buildItem(getIcon(t),
                "§a" + formatName(t),
                "§7Tipo: §fMob", "", "§e> Clic para disfrazarte"));
        }

        buildNavRow(inv, page, totalPages, total);
        openMenu(player, inv, MENU_MOBS, page);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  MENÚ MISC
    // ─────────────────────────────────────────────────────────────────────
    public static void openMiscMenu(Player player, int page) {
        List<DisguiseType> types = getMiscTypes();
        int total = types.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, 54,
            "§8» §dObjetos y Misc §7(" + (page + 1) + "/" + totalPages + ")");

        int start = page * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, total);
        for (int i = start; i < end; i++) {
            DisguiseType t = types.get(i);
            inv.setItem(i - start, buildItem(getIcon(t),
                "§d" + formatName(t),
                "§7Tipo: §fMisc", "", "§e> Clic para disfrazarte"));
        }

        buildNavRow(inv, page, totalPages, total);
        openMenu(player, inv, MENU_MISC, page);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  MENÚ JUGADORES  (online + botón offline)
    // ─────────────────────────────────────────────────────────────────────
    public static void openPlayerMenu(Player player, int page) {
        List<Player> online = Bukkit.getOnlinePlayers().stream()
            .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
            .sorted(Comparator.comparing(Player::getName))
            .collect(Collectors.toList());

        int total      = online.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(null, 54,
            "§8» §bJugadores §7(" + (page + 1) + "/" + totalPages + ")");

        int start = page * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, total);
        for (int i = start; i < end; i++) {
            Player t = online.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(t);
                meta.setDisplayName("§b" + t.getName());
                meta.setLore(Arrays.asList("§7Disfrazarte como este jugador.", "", "§e> Clic para disfrazarte"));
                head.setItemMeta(meta);
            }
            inv.setItem(i - start, head);
        }

        buildNavRow(inv, page, totalPages, total);

        // Botón de jugador OFFLINE — siempre en slot 46
        inv.setItem(46, buildItem(Material.WRITABLE_BOOK,
            "§e§lJugador Offline",
            "§7Escribe el nombre de un jugador",
            "§7que este desconectado.",
            "", "§e> Clic para escribir el nombre"));

        openMenu(player, inv, MENU_PLAYERS, page);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  MENÚ MI DISFRAZ
    // ─────────────────────────────────────────────────────────────────────
    public static void openMyDisguiseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8» §6Mi Disfraz Actual");

        for (int i = 0; i < 27; i++) inv.setItem(i, glassBlack());
        for (int s : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,19,20,21,22,23,24,25,26})
            inv.setItem(s, glass());

        boolean isDisguised = DisguiseAPI.isDisguised(player);

        inv.setItem(11, buildItem(Material.COMPASS,
            "§6§lEstado del Disfraz",
            "§7Estado: " + (isDisguised ? "§aActivo" : "§cNinguno")));

        inv.setItem(13, buildItem(Material.ENDER_EYE,
            "§e§lVer Mi Disfraz",
            "§7Activa o desactiva ver",
            "§7tu propio disfraz.",
            "", "§e> Clic para alternar"));

        if (isDisguised) {
            inv.setItem(15, buildItem(Material.BARRIER,
                "§c§lQuitar Disfraz",
                "§7Elimina tu disfraz activo.",
                "", "§e> Clic para quitarlo"));
        }

        inv.setItem(18, buildItem(Material.ARROW,
            "§7§l< Volver al Menu",
            "§7Regresar al menu principal."));

        openMenu(player, inv, MENU_MY_DISGUISE, 0);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  MANEJADOR DE CLICS
    // ─────────────────────────────────────────────────────────────────────
    public static void handleClick(Player player, int slot) {
        String menu = OPEN_MENUS.get(player.getUniqueId());
        int    page = MENU_PAGES.getOrDefault(player.getUniqueId(), 0);
        if (menu == null) return;

        switch (menu) {
            case MENU_MAIN:        handleMainClick(player, slot);                    break;
            case MENU_MOBS:        handleListClick(player, slot, page, MENU_MOBS);  break;
            case MENU_MISC:        handleListClick(player, slot, page, MENU_MISC);  break;
            case MENU_PLAYERS:     handlePlayerClick(player, slot, page);           break;
            case MENU_MY_DISGUISE: handleMyDisguiseClick(player, slot);             break;
        }
    }

    private static void handleMainClick(Player player, int slot) {
        switch (slot) {
            case 20: openMobMenu(player, 0);    break;
            case 22: openPlayerMenu(player, 0); break;
            case 24: openMiscMenu(player, 0);   break;
            case 31:
                if (DisguiseAPI.isDisguised(player)) openMyDisguiseMenu(player);
                break;
            case 49:
                if (DisguiseAPI.isDisguised(player)) {
                    player.closeInventory();
                    OPEN_MENUS.remove(player.getUniqueId());
                    MENU_PAGES.remove(player.getUniqueId());
                    player.performCommand("undisguise");
                }
                break;
        }
    }

    private static void handleListClick(Player player, int slot, int page, String menuType) {
        List<DisguiseType> types = MENU_MOBS.equals(menuType) ? getMobTypes() : getMiscTypes();
        int totalPages = Math.max(1, (int) Math.ceil((double) types.size() / ITEMS_PER_PAGE));

        if (slot == 45 && page > 0) {
            if (MENU_MOBS.equals(menuType)) openMobMenu(player, page - 1); else openMiscMenu(player, page - 1);
            return;
        }
        if (slot == 48) { openMainMenu(player); return; }
        if (slot == 53 && page < totalPages - 1) {
            if (MENU_MOBS.equals(menuType)) openMobMenu(player, page + 1); else openMiscMenu(player, page + 1);
            return;
        }
        if (slot >= 45) return;

        int index = page * ITEMS_PER_PAGE + slot;
        if (index >= types.size()) return;

        DisguiseType type = types.get(index);
        player.closeInventory();
        OPEN_MENUS.remove(player.getUniqueId());
        MENU_PAGES.remove(player.getUniqueId());
        player.performCommand("disguise " + type.name().toLowerCase(Locale.ENGLISH));
    }

    private static void handlePlayerClick(Player player, int slot, int page) {
        // Botón de jugador offline
        if (slot == 46) {
            player.closeInventory();
            OPEN_MENUS.remove(player.getUniqueId());
            MENU_PAGES.remove(player.getUniqueId());
            AWAITING_NAME.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage("§8[§6ZerDisguise§8] §eEscribe en el chat el nombre del jugador:");
            player.sendMessage("§8[§6ZerDisguise§8] §7(tienes 30 segundos, escribe §ccancelar §7para salir)");
            return;
        }

        List<Player> online = Bukkit.getOnlinePlayers().stream()
            .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
            .sorted(Comparator.comparing(Player::getName))
            .collect(Collectors.toList());
        int totalPages = Math.max(1, (int) Math.ceil((double) online.size() / ITEMS_PER_PAGE));

        if (slot == 45 && page > 0)              { openPlayerMenu(player, page - 1); return; }
        if (slot == 48)                           { openMainMenu(player); return; }
        if (slot == 53 && page < totalPages - 1)  { openPlayerMenu(player, page + 1); return; }
        if (slot >= 45) return;

        int index = page * ITEMS_PER_PAGE + slot;
        if (index >= online.size()) return;

        Player target = online.get(index);
        player.closeInventory();
        OPEN_MENUS.remove(player.getUniqueId());
        MENU_PAGES.remove(player.getUniqueId());
        player.performCommand("disguise player " + target.getName());
    }

    private static void handleMyDisguiseClick(Player player, int slot) {
        switch (slot) {
            case 13:
                player.closeInventory();
                OPEN_MENUS.remove(player.getUniqueId());
                MENU_PAGES.remove(player.getUniqueId());
                player.performCommand("disguiseviewself");
                break;
            case 15:
                if (DisguiseAPI.isDisguised(player)) {
                    player.closeInventory();
                    OPEN_MENUS.remove(player.getUniqueId());
                    MENU_PAGES.remove(player.getUniqueId());
                    player.performCommand("undisguise");
                }
                break;
            case 18:
                openMainMenu(player);
                break;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  INPUT DE NOMBRE (jugador offline) — llamado desde MenuListener
    // ─────────────────────────────────────────────────────────────────────
    public static boolean handleNameInput(Player player, String name) {
        UUID id = player.getUniqueId();
        Long since = AWAITING_NAME.get(id);
        if (since == null) return false;

        AWAITING_NAME.remove(id);

        // Timeout de 30 segundos
        if (System.currentTimeMillis() - since > NAME_TIMEOUT_MS) {
            player.sendMessage("§8[§6ZerDisguise§8] §cTiempo agotado. Intenta de nuevo.");
            return true;
        }

        if (name.equalsIgnoreCase("cancelar") || name.equalsIgnoreCase("cancel")) {
            player.sendMessage("§8[§6ZerDisguise§8] §7Cancelado.");
            return true;
        }

        // Nombre válido — ejecutar disfraz
        player.sendMessage("§8[§6ZerDisguise§8] §aDisfrazandote como §f" + name + "§a...");
        player.performCommand("disguise player " + name);
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  FILA DE NAVEGACIÓN
    // ─────────────────────────────────────────────────────────────────────
    private static void buildNavRow(Inventory inv, int page, int totalPages, int total) {
        for (int i = 45; i < 54; i++) inv.setItem(i, glass());
        if (page > 0)
            inv.setItem(45, buildItem(Material.ARROW, "§f§l< Pagina anterior", "§7Pagina " + page));
        inv.setItem(48, buildItem(Material.BARRIER, "§c§lX Volver al menu", "§7Menu principal"));
        inv.setItem(49, buildItem(Material.PAPER,
            "§fPagina §e" + (page + 1) + " §fde §e" + totalPages, "§7Total: §f" + total));
        if (page < totalPages - 1)
            inv.setItem(53, buildItem(Material.ARROW, "§f§lPagina siguiente >", "§7Pagina " + (page + 2)));
    }
}
