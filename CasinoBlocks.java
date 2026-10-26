package io.github.minevegas;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Merkt sich, welche platzierten Blöcke Casino-Automaten sind (blocks.yml). */
public final class CasinoBlocks {
    private final MineVegas plugin;
    private final NamespacedKey key;
    private final File file;
    private final Map<String, GameType> blocks = new HashMap<>();

    public CasinoBlocks(MineVegas plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "casino_game");
        this.file = new File(plugin.getDataFolder(), "blocks.yml");
        load();
    }

    private static String k(Block b) {
        return b.getWorld().getName() + ";" + b.getX() + ";" + b.getY() + ";" + b.getZ();
    }

    public void load() {
        blocks.clear();
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        for (String s : y.getStringList("blocks")) {
            String[] p = s.split(";");
            if (p.length != 5) continue;
            try {
                blocks.put(p[0] + ";" + p[1] + ";" + p[2] + ";" + p[3], GameType.valueOf(p[4]));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void save() {
        YamlConfiguration y = new YamlConfiguration();
        List<String> l = new ArrayList<>();
        blocks.forEach((k, v) -> l.add(k + ";" + v.name()));
        y.set("blocks", l);
        try {
            plugin.getDataFolder().mkdirs();
            y.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("blocks.yml konnte nicht gespeichert werden: " + e.getMessage());
        }
    }

    public GameType at(Block b) { return blocks.get(k(b)); }

    public void add(Block b, GameType t) {
        blocks.put(k(b), t);
        save();
    }

    public void remove(Block b) {
        if (blocks.remove(k(b)) != null) save();
    }

    public ItemStack item(GameType t) {
        ItemStack it = Items.named(t.material(), "§6§l" + t.display(),
                "§7Casino-Automat", "§7Platzieren und per Rechtsklick spielen.");
        ItemMeta m = it.getItemMeta();
        m.getPersistentDataContainer().set(key, PersistentDataType.STRING, t.name());
        m.setEnchantmentGlintOverride(true);
        it.setItemMeta(m);
        return it;
    }

    public GameType typeOf(ItemStack it) {
        if (it == null || !it.hasItemMeta()) return null;
        String s = it.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (s == null) return null;
        try {
            return GameType.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
