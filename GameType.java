package io.github.minevegas;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public enum GameType {
    SLOTS("slots", "Slot Machine", Material.DISPENSER, SlotsGame::new),
    ROULETTE("roulette", "Roulette", Material.RED_GLAZED_TERRACOTTA, RouletteGame::new),
    BLACKJACK("blackjack", "Blackjack", Material.GREEN_WOOL, BlackjackGame::new),
    DERBY("derby", "Duck Derby", Material.HAY_BLOCK, DerbyGame::new),
    WHEEL("wheel", "Glücksrad", Material.GOLD_BLOCK, WheelGame::new),
    PLINKO("plinko", "Plinko", Material.BLACK_CONCRETE, PlinkoGame::new);

    public interface Factory {
        CasinoGame create(MineVegas plugin, Player player);
    }

    private final String key;
    private final String display;
    private final Material material;
    private final Factory factory;

    GameType(String key, String display, Material material, Factory factory) {
        this.key = key;
        this.display = display;
        this.material = material;
        this.factory = factory;
    }

    public String key() { return key; }
    public String display() { return display; }
    public Material material() { return material; }
    public CasinoGame create(MineVegas plugin, Player player) { return factory.create(plugin, player); }

    public static GameType byKey(String s) {
        for (GameType t : values()) if (t.key.equalsIgnoreCase(s)) return t;
        return null;
    }
}
