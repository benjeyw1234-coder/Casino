package io.github.minevegas;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** Gewählter Einsatz (Währung + Menge) eines Spielers an einem Automaten. */
public final class Stake {
    private static final Material[] CUR = {
            Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.EMERALD,
            Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK};
    private static final String[] NAMES = {
            "Eisenbarren", "Goldbarren", "Diamanten", "Smaragde",
            "Eisenblöcke", "Goldblöcke", "Diamantblöcke", "Smaragdblöcke"};
    private static final int[] AMT = {1, 2, 4, 8, 16, 32, 64};

    private int c = 1;
    private int a = 0;

    public Material currency() { return CUR[c]; }
    public int amount() { return AMT[a]; }
    public String label() { return amount() + "× " + NAMES[c]; }
    public void nextAmount() { a = (a + 1) % AMT.length; }
    public void nextCurrency() { c = (c + 1) % CUR.length; }

    public static String name(Material m) {
        for (int i = 0; i < CUR.length; i++) if (CUR[i] == m) return NAMES[i];
        return m.name();
    }

    public ItemStack button() {
        return Items.named(currency(), amount(), "§eEinsatz: §f" + label(),
                "§7Linksklick: Menge ändern", "§7Rechtsklick: Währung wechseln");
    }
}
