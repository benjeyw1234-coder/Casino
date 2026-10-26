package io.github.minevegas;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Plinko: 8 Zufallsentscheidungen (links/rechts), jeweils zwei pro Reihe angezeigt.
 * Das Zielfach ist binomialverteilt (Mitte am wahrscheinlichsten).
 */
public final class PlinkoGame extends CasinoGame {
    private static final double[] DEFAULT = {10, 3, 1.5, 0.6, 0.2, 0.6, 1.5, 3, 10};
    private static final int STAKE = 46, DROP = 49, INFO = 52;

    private final double[] mult = new double[9];
    private final int[] rights = new int[5];

    public PlinkoGame(MineVegas plugin, Player player) {
        super(plugin, player);
        List<Double> cfg = plugin.getConfig().getDoubleList("plinko.multipliers");
        for (int i = 0; i < 9; i++) mult[i] = cfg.size() == 9 ? cfg.get(i) : DEFAULT[i];
    }

    @Override
    protected void build() {
        create(6, "§b§lPlinko");
        fill(filler());
        drawBoard(-1);
        inv.setItem(STAKE, stake.button());
        inv.setItem(DROP, Items.named(Material.LIME_DYE, "§a§lKugel fallen lassen", "§7Klicken zum Starten"));
        inv.setItem(INFO, Items.named(Material.PAPER, "§eHinweis",
                "§7Zahlt den Einsatz × Fachwert.", "§7Bei Bruchwerten wird abgerundet,", "§7nimm dann Einsätze ab 2."));
    }

    private ItemStack bucket(int i) {
        double m = mult[i];
        Material mat = m >= 5 ? Material.MAGENTA_STAINED_GLASS_PANE
                : m >= 2 ? Material.LIGHT_BLUE_STAINED_GLASS_PANE
                : m >= 1 ? Material.LIME_STAINED_GLASS_PANE
                : m >= 0.5 ? Material.YELLOW_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        return Items.named(mat, "§f" + fmt(m));
    }

    private int col(int row) { return 4 + rights[row] - row; }

    /** frame = Reihe, in der die Kugel gerade ist (-1 = keine Kugel). */
    private void drawBoard(int frame) {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 9; c++) {
                inv.setItem(r * 9 + c, (c + r) % 2 == 1
                        ? Items.named(Material.IRON_NUGGET, " ")
                        : Items.named(Material.GRAY_STAINED_GLASS_PANE, " "));
            }
        }
        for (int c = 0; c < 9; c++) inv.setItem(36 + c, bucket(c));
        if (frame >= 0) {
            int c = col(frame);
            String label = frame == 4 ? "§e" + fmt(mult[c]) : "§fKugel";
            inv.setItem(frame * 9 + c, Items.named(Material.ENDER_PEARL, label));
        }
    }

    @Override
    public void click(int slot, ClickType t) {
        if (busy) return;
        if (slot == STAKE) {
            changeStake(t);
            inv.setItem(STAKE, stake.button());
        } else if (slot == DROP) {
            if (!startRound()) return;
            busy = true;
            rights[0] = 0;
            for (int k = 1; k <= 4; k++) {
                rights[k] = rights[k - 1] + rnd().nextInt(2) + rnd().nextInt(2);
            }
            step(0);
        }
    }

    private void step(int frame) {
        drawBoard(frame);
        sound(Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f + 0.2f * frame);
        if (frame >= 4) {
            finish();
            return;
        }
        schedule(8, () -> step(frame + 1));
    }

    private void finish() {
        cancelTask();
        busy = false;
        drawBoard(4);
        result((int) Math.floor(roundStake * mult[col(4)]));
    }

    @Override
    public void forceFinish() { finish(); }
}
