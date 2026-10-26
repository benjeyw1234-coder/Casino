package io.github.minevegas;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;

public final class CasinoListener implements Listener {
    private final MineVegas plugin;

    public CasinoListener(MineVegas plugin) { this.plugin = plugin; }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        GameType t = plugin.blocks().typeOf(e.getItemInHand());
        if (t == null) return;
        if (!e.getPlayer().hasPermission("minevegas.admin")) {
            e.setCancelled(true);
            return;
        }
        plugin.blocks().add(e.getBlockPlaced(), t);
        e.getPlayer().sendMessage(Items.text(plugin.prefix() + "§a" + t.display() + " §fplatziert."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        GameType t = plugin.blocks().at(e.getBlock());
        if (t == null) return;
        Player p = e.getPlayer();
        if (!p.hasPermission("minevegas.admin")) {
            e.setCancelled(true);
            return;
        }
        plugin.blocks().remove(e.getBlock());
        e.setDropItems(false);
        if (p.getGameMode() != GameMode.CREATIVE) {
            e.getBlock().getWorld().dropItemNaturally(
                    e.getBlock().getLocation().add(0.5, 0.5, 0.5), plugin.blocks().item(t));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND) return;
        Block b = e.getClickedBlock();
        if (b == null) return;
        GameType t = plugin.blocks().at(b);
        if (t == null) return;
        Player p = e.getPlayer();
        // Admins können mit Schleichen normal bauen
        if (p.isSneaking() && p.hasPermission("minevegas.admin")) return;
        e.setCancelled(true);
        if (!p.hasPermission("minevegas.play")) {
            p.sendMessage(Items.text(plugin.prefix() + "§cDu darfst das Casino nicht benutzen."));
            return;
        }
        if (p.getOpenInventory().getTopInventory().getHolder() instanceof CasinoGame) return;
        t.create(plugin, p).open();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(b -> plugin.blocks().at(b) != null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(b -> plugin.blocks().at(b) != null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (e.getBlocks().stream().anyMatch(b -> plugin.blocks().at(b) != null)) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (e.getBlocks().stream().anyMatch(b -> plugin.blocks().at(b) != null)) e.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (top.getHolder() instanceof CreativeMenu m) {
            e.setCancelled(true);
            Inventory c = e.getClickedInventory();
            if (c != null && c.equals(top)) m.click(e.getSlot(), e.getClick());
            return;
        }
        if (!(top.getHolder() instanceof CasinoGame g)) return;
        e.setCancelled(true);
        Inventory clicked = e.getClickedInventory();
        if (clicked == null || !clicked.equals(top)) return;
        g.click(e.getSlot(), e.getClick());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        var h = e.getView().getTopInventory().getHolder();
        if (h instanceof CasinoGame || h instanceof CreativeMenu) e.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof CasinoGame g) g.onClose();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof CasinoGame g) g.onClose();
    }
}
