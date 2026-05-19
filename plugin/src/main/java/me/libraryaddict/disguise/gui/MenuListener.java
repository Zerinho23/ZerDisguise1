package me.libraryaddict.disguise.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
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

        // En Paper 1.20.4, InventoryView fue cambiado a interfaz.
        // Evitamos event.getView() por completo y usamos comparación directa.
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;

        // Si el clic fue en el inventario del jugador (parte inferior), ignorar
        if (clicked == player.getInventory()) return;

        // Verificar que hay un ítem real en el slot
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        // event.getSlot() es correcto cuando se hace clic en el inventario superior
        DisguiseGUI.handleClick(player, event.getSlot());
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
        // No limpiar si el jugador está transitando entre menús del GUI
        if (DisguiseGUI.TRANSITIONING.contains(id)) return;
        DisguiseGUI.OPEN_MENUS.remove(id);
        DisguiseGUI.MENU_PAGES.remove(id);
    }

    /**
     * Captura el nombre escrito en chat para el modo "jugador offline".
     * Se cancela el mensaje para que no se vea en el chat público.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!DisguiseGUI.AWAITING_NAME.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);
        String name = event.getMessage().trim();

        // Procesar en el hilo principal (performCommand requiere hilo principal)
        if (DisguiseGUI.PLUGIN != null) {
            DisguiseGUI.PLUGIN.getServer().getScheduler().runTask(
                DisguiseGUI.PLUGIN,
                () -> DisguiseGUI.handleNameInput(player, name)
            );
        } else {
            DisguiseGUI.handleNameInput(player, name);
        }
    }
}
