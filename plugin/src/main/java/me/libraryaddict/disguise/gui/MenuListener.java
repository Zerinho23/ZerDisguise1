package me.libraryaddict.disguise.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MenuListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UUID id = player.getUniqueId();

        if (!DisguiseGUI.OPEN_MENUS.containsKey(id)) return;

        event.setCancelled(true);

        // Usar getRawSlot() es más confiable que comparar objetos Inventory en Paper 1.20
        int rawSlot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize();
        if (rawSlot < 0 || rawSlot >= topSize) return;

        // Ignorar slots vacíos o de aire
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        DisguiseGUI.handleClick(player, rawSlot);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!DisguiseGUI.OPEN_MENUS.containsKey(player.getUniqueId())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID id = player.getUniqueId();
        // Si el jugador está transitando entre menús, no limpiar el mapa
        if (DisguiseGUI.TRANSITIONING.contains(id)) return;
        DisguiseGUI.OPEN_MENUS.remove(id);
        DisguiseGUI.MENU_PAGES.remove(id);
    }
}
