package io.github.minevegas;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Kleine Helfer für Texte, GUI-Items und Gegenstände im Spielerinventar. */
public final class Items {
    private Items() {}

    /** Text mit §-Farbcodes -> Component (ohne Kursiv). */
    public static Component text(String legacy) {
        return LegacyComponentSerializer.legacySection().deserialize(legacy)
                .decoration(TextDecoration.ITALIC, false);
    }

    public static ItemStack named(Material m, String name, String... lore) {
        return named(m, 1, name, lore);
    }

    public static ItemStack named(Material m, int amount, String name, String... lore) {
        ItemStack it = new ItemStack(m, Math.max(1, Math.min(64, amount)));
        ItemMeta meta = it.getItemMeta();
        meta.displayName(text(name));
        if (lore.length > 0) {
            List<Component> l = new ArrayList<>();
            for (String s : lore) l.add(text(s));
            meta.lore(l);
        }
        it.setItemMeta(meta);
        return it;
    }

    public static ItemStack glow(ItemStack it) {
        ItemMeta m = it.getItemMeta();
        m.setEnchantmentGlintOverride(true);
        it.setItemMeta(m);
        return it;
    }

    public static boolean has(Player p, Material m, int n) {
        return p.getInventory().containsAtLeast(new ItemStack(m), n);
    }

    /** Nimmt n Stück (nur unbenannte Standard-Items). false, wenn nicht genug da sind. */
    public static boolean take(Player p, Material m, int n) {
        if (!has(p, m, n)) return false;
        int left = n;
        while (left > 0) {
            int c = Math.min(64, left);
            left -= c;
            p.getInventory().removeItem(new ItemStack(m, c));
        }
        return true;
    }

    /** Gibt n Stück; was nicht ins Inventar passt, wird vor die Füße gedroppt. */
    public static void give(Player p, Material m, int n) {
        int left = n;
        while (left > 0) {
            int c = Math.min(64, left);
            left -= c;
            Map<Integer, ItemStack> over = p.getInventory().addItem(new ItemStack(m, c));
            for (ItemStack o : over.values()) p.getWorld().dropItem(p.getLocation(), o);
        }
    }
}
