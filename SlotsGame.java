package io.github.minevegas;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class SlotsGame extends CasinoGame {
    private static final Material[] SYM = {Material.SWEET_BERRIES, Material.YELLOW_DYE, Material.BELL,
            Material.DIAMOND, Material.NETHER_STAR};
    private static final String[] NAME = {"§cKirsche", "§eZitrone", "§6Glocke", "§bDiamant", "§d§lSieben"};
    private static final int[] WEIGHT = {8, 6, 4, 2, 1};
    /** Auszahlung bei drei gleichen Symbolen (Gesamtauszahlung inkl. Einsatz). */
    private static final int[] TRIPLE = {3, 5, 10, 25, 75};
    private static final int[] REEL = {11, 13, 15};
    private static final int STAKE = 21, SPIN = 23, INFO = 25;

    private final int[] fin = new int[3];

    public SlotsGame(MineVegas plugin, Player player) { super(plugin, player); }

    @Override
    protected void build() {
        create(3, "§6§lSlot Machine");
        fill(filler());
        for (int i = 0; i < 3; i++) inv.setItem(REEL[i], symItem(rnd().nextInt(SYM.length)));
        inv.setItem(STAKE, stake.button());
        inv.setItem(SPIN, Items.named(Material.LIME_DYE, "§a§lDrehen", "§7Klicken, um den Hebel zu ziehen"));
        inv.setItem(INFO, Items.named(Material.PAPER, "§eGewinntabelle",
                "§c3× Kirsche §7→ x3", "§e3× Zitrone §7→ x5", "§63× Glocke §7→ x10",
                "§b3× Diamant §7→ x25", "§d3× Sieben §7→ x75", "§c2× Kirsche §7→ x2"));
    }

    private ItemStack symItem(int s) { return Items.named(SYM[s], NAME[s]); }

    private int pick() {
        int total = 0;
        for (int w : WEIGHT) total += w;
        int r = rnd().nextInt(total);
        for (int i = 0; i < WEIGHT.length; i++) {
            r -= WEIGHT[i];
            if (r < 0) return i;
        }
        return 0;
    }

    @Override
    public void click(int slot, ClickType type) {
        if (busy) return;
        if (slot == STAKE) {
            changeStake(type);
            inv.setItem(STAKE, stake.button());
        } else if (slot == SPIN) {
            if (!startRound()) return;
            busy = true;
            for (int i = 0; i < 3; i++) fin[i] = pick();
            step(0);
        }
    }

    private void step(int t) {
        for (int i = 0; i < 3; i++) {
            int stopAt = 10 + 8 * i;
            int sym = t >= stopAt ? fin[i] : rnd().nextInt(SYM.length);
            inv.setItem(REEL[i], symItem(sym));
        }
        sound(Sound.BLOCK_NOTE_BLOCK_HAT, 1.2f);
        if (t >= 10 + 8 * 2) {
            finish();
            return;
        }
        schedule(2, () -> step(t + 1));
    }

    private void finish() {
        cancelTask();
        busy = false;
        for (int i = 0; i < 3; i++) inv.setItem(REEL[i], symItem(fin[i]));
        int mult = 0;
        if (fin[0] == fin[1] && fin[1] == fin[2]) {
            mult = TRIPLE[fin[0]];
        } else {
            int cherries = 0;
            for (int s : fin) if (s == 0) cherries++;
            if (cherries == 2) mult = 2;
        }
        result(roundStake * mult);
    }

    @Override
    public void forceFinish() { finish(); }
}
