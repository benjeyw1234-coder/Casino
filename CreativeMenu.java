package io.github.minevegas;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Eigenes Kreativmenü: alle Casino-Gegenstände zum Herausnehmen.
 * (Ein Server-Plugin kann keine Einträge in die echten Kreativ-Tabs einfügen,
 * deshalb gibt es dieses Menü per /casino menu.)
 */
public final class CreativeMenu implements InventoryHolder {
    private static final int ROWS = 3;
    private static final int[] SLOTS = {10, 11, 12, 14, 15, 16};
    private static final int SLOT_ALL = 22;
    private static final int SLOT_CLOSE = 26;

    private final MineVegas plugin;
    private final Player player;
    private final Inventory inv;

    public CreativeMenu(MineVegas plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inv = Bukkit.createInventory(this, ROWS * 9, Items.text("§6§lCasino §8» §eKreativmenü"));
    }

    @Override
    public Inventory getInventory() { return inv; }

    public void open() {
        ItemStack bg = Items.named(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, bg.clone());
        GameType[] types = GameType.values();
        for (int i = 0; i < types.length && i < SLOTS.length; i++) {
            ItemStack it = plugin.blocks().item(types[i]);
            var meta = it.getItemMeta();
            var lore = meta.lore();
            if (lore != null) {
                lore.add(Items.text(""));
                lore.add(Items.text("§eLinksklick: §f1 Stück"));
                lore.add(Items.text("§eShift-Klick: §f64 Stück"));
                meta.lore(lore);
                it.setItemMeta(meta);
            }
            inv.setItem(SLOTS[i], it);
        }
        inv.setItem(SLOT_ALL, Items.glow(Items.named(Material.CHEST, "§aAlle Automaten holen",
                "§7Je 1 Stück jedes Spiels.")));
        inv.setItem(SLOT_CLOSE, Items.named(Material.BARRIER, "§cSchließen"));
        player.openInventory(inv);
    }

    public void click(int slot, ClickType type) {
        if (slot == SLOT_CLOSE) {
            player.closeInventory();
            return;
        }
        if (slot == SLOT_ALL) {
            for (GameType t : GameType.values()) give(plugin.blocks().item(t), 1);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1f);
            return;
        }
        GameType[] types = GameType.values();
        for (int i = 0; i < types.length && i < SLOTS.length; i++) {
            if (SLOTS[i] != slot) continue;
            give(plugin.blocks().item(types[i]), type.isShiftClick() ? 64 : 1);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.2f);
            return;
        }
    }

    private void give(ItemStack it, int amount) {
        it.setAmount(amount);
        Map<Integer, ItemStack> over = player.getInventory().addItem(it);
        for (ItemStack o : over.values()) player.getWorld().dropItem(player.getLocation(), o);
    }
}
