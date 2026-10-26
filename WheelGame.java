package io.github.minevegas;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/** Glücksrad mit 20 Feldern rund um das Inventar. */
public final class WheelGame extends CasinoGame {
    private static final int[] RING = {0, 1, 2, 3, 4, 5, 6, 7, 8, 17, 26, 25, 24, 23, 22, 21, 20, 19, 18, 9};
    private static final double[] DEFAULT = {0, 0.5, 0, 1, 0, 0, 1.5, 0, 2, 0, 0.5, 0, 1, 3, 0, 1, 0, 5, 0, 2};
    private static final int STAKE = 11, SPIN = 13, INFO = 15;

    private final double[] seg = new double[RING.length];
    private int finalIdx;

    public WheelGame(MineVegas plugin, Player player) {
        super(plugin, player);
        List<Double> cfg = plugin.getConfig().getDoubleList("wheel.segments");
        for (int i = 0; i < seg.length; i++) seg[i] = cfg.size() == seg.length ? cfg.get(i) : DEFAULT[i];
    }

    @Override
    protected void build() {
        create(3, "§d§lGlücksrad");
        fill(filler());
        drawRing(-1);
        inv.setItem(STAKE, stake.button());
        inv.setItem(SPIN, Items.named(Material.LIME_DYE, "§a§lRad drehen", "§7Klicken zum Drehen"));
        inv.setItem(INFO, Items.named(Material.PAPER, "§eHinweis",
                "§7Zahlt den Einsatz × Feldwert.", "§7Bei Bruchwerten wird abgerundet,", "§7nimm dann Einsätze ab 2."));
    }

    private ItemStack segItem(double m) {
        if (m <= 0) return Items.named(Material.RED_STAINED_GLASS_PANE, "§cNiete");
        if (m < 1) return Items.named(Material.ORANGE_STAINED_GLASS_PANE, "§6" + fmt(m));
        if (m == 1) return Items.named(Material.YELLOW_STAINED_GLASS_PANE, "§e" + fmt(m));
        if (m < 3) return Items.named(Material.LIME_STAINED_GLASS_PANE, "§a" + fmt(m));
        if (m < 10) return Items.named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "§b" + fmt(m));
        return Items.named(Material.MAGENTA_STAINED_GLASS_PANE, "§d§l" + fmt(m));
    }

    private void drawRing(int pointer) {
        for (int i = 0; i < RING.length; i++) {
            ItemStack it = i == pointer
                    ? Items.named(Material.NETHER_STAR, "§e▶ " + (seg[i] <= 0 ? "Niete" : fmt(seg[i])))
                    : segItem(seg[i]);
            inv.setItem(RING[i], it);
        }
    }

    @Override
    public void click(int slot, ClickType t) {
        if (busy) return;
        if (slot == STAKE) {
            changeStake(t);
            inv.setItem(STAKE, stake.button());
        } else if (slot == SPIN) {
            if (!startRound()) return;
            busy = true;
            finalIdx = rnd().nextInt(RING.length);
            int start = rnd().nextInt(RING.length);
            int steps = 20 + Math.floorMod(finalIdx - start, RING.length);
            step(start, 0, steps);
        }
    }

    private void step(int start, int s, int steps) {
        drawRing((start + s) % RING.length);
        sound(Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f + 0.8f * s / steps);
        if (s >= steps) {
            finish();
            return;
        }
        long delay = 1 + (6L * s * s) / ((long) steps * steps);
        schedule(delay, () -> step(start, s + 1, steps));
    }

    private void finish() {
        cancelTask();
        busy = false;
        drawRing(finalIdx);
        result((int) Math.floor(roundStake * seg[finalIdx]));
    }

    @Override
    public void forceFinish() { finish(); }
}
