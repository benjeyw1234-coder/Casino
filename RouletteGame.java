package io.github.minevegas;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Europäisches Roulette (37 Felder, ein Null-Feld). */
public final class RouletteGame extends CasinoGame {
    private static final Set<Integer> REDS = Set.of(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);
    /** Reihenfolge der Zahlen im echten Kessel. */
    private static final int[] WHEEL = {0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5,
            24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26};
    private static final int STAKE = 29, NUMBER = 31, SPIN = 33;

    private enum Bet {
        RED("Rot", Material.RED_CONCRETE, 2), BLACK("Schwarz", Material.BLACK_CONCRETE, 2),
        EVEN("Gerade", Material.WHITE_CONCRETE, 2), ODD("Ungerade", Material.GRAY_CONCRETE, 2),
        LOW("1–18", Material.LIGHT_BLUE_CONCRETE, 2), HIGH("19–36", Material.BLUE_CONCRETE, 2),
        DOZ1("1–12", Material.YELLOW_CONCRETE, 3), DOZ2("13–24", Material.ORANGE_CONCRETE, 3),
        DOZ3("25–36", Material.PURPLE_CONCRETE, 3), NUMBER("Einzelzahl", Material.LIME_CONCRETE, 36);

        final String label;
        final Material material;
        final int mult;

        Bet(String label, Material material, int mult) {
            this.label = label;
            this.material = material;
            this.mult = mult;
        }
    }

    private Bet bet = Bet.RED;
    private int number = 17;
    private int rolled;
    private int finalPos;

    public RouletteGame(MineVegas plugin, Player player) { super(plugin, player); }

    @Override
    protected void build() {
        create(5, "§c§lRoulette");
        fill(filler());
        inv.setItem(13, Items.named(Material.GOLD_NUGGET, "§e▲ Gewinnzahl"));
        drawStrip(rnd().nextInt(WHEEL.length));
        refreshBets();
        inv.setItem(STAKE, stake.button());
        inv.setItem(SPIN, Items.named(Material.LIME_DYE, "§a§lKugel werfen", "§7Klicken, um zu drehen"));
    }

    private ItemStack numItem(int n) {
        Material m = n == 0 ? Material.LIME_CONCRETE : REDS.contains(n) ? Material.RED_CONCRETE : Material.BLACK_CONCRETE;
        String c = n == 0 ? "§a" : REDS.contains(n) ? "§c" : "§f";
        return Items.named(m, c + "§l" + n);
    }

    private void drawStrip(int pos) {
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, numItem(WHEEL[Math.floorMod(pos + i - 4, WHEEL.length)]));
        }
    }

    private void refreshBets() {
        Bet[] bs = Bet.values();
        for (int i = 0; i < 9; i++) inv.setItem(18 + i, betItem(bs[i]));
        inv.setItem(NUMBER, betItem(Bet.NUMBER));
    }

    private ItemStack betItem(Bet b) {
        String name = b == Bet.NUMBER ? "Zahl " + number : b.label;
        Material mat = b == Bet.NUMBER ? numItem(number).getType() : b.material;
        List<String> lore = new ArrayList<>();
        lore.add("§7Auszahlung: §ex" + b.mult);
        if (b == Bet.NUMBER) lore.add("§7Links/Rechts: ±1 · Shift: ±10");
        lore.add(b == bet ? "§a✔ gewählt" : "§7Klicken zum Wählen");
        ItemStack it = Items.named(mat, "§f" + name, lore.toArray(new String[0]));
        return b == bet ? Items.glow(it) : it;
    }

    @Override
    public void click(int slot, ClickType t) {
        if (busy) return;
        if (slot == STAKE) {
            changeStake(t);
            inv.setItem(STAKE, stake.button());
        } else if (slot == SPIN) {
            spin();
        } else if (slot == NUMBER) {
            bet = Bet.NUMBER;
            int d = t.isShiftClick() ? 10 : 1;
            if (t.isRightClick()) d = -d;
            number = Math.floorMod(number + d, 37);
            refreshBets();
        } else if (slot >= 18 && slot <= 26) {
            bet = Bet.values()[slot - 18];
            refreshBets();
        }
    }

    private void spin() {
        if (!startRound()) return;
        busy = true;
        rolled = rnd().nextInt(37);
        for (int i = 0; i < WHEEL.length; i++) if (WHEEL[i] == rolled) finalPos = i;
        int start = rnd().nextInt(WHEEL.length);
        int steps = 20 + Math.floorMod(finalPos - start, WHEEL.length);
        step(start, 0, steps);
    }

    private void step(int start, int s, int steps) {
        drawStrip(start + s);
        sound(Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f + 0.5f * s / steps);
        if (s >= steps) {
            finish();
            return;
        }
        long delay = 1 + (6L * s * s) / ((long) steps * steps);
        schedule(delay, () -> step(start, s + 1, steps));
    }

    private boolean wins(int r) {
        return switch (bet) {
            case RED -> r > 0 && REDS.contains(r);
            case BLACK -> r > 0 && !REDS.contains(r);
            case EVEN -> r > 0 && r % 2 == 0;
            case ODD -> r % 2 == 1;
            case LOW -> r >= 1 && r <= 18;
            case HIGH -> r >= 19;
            case DOZ1 -> r >= 1 && r <= 12;
            case DOZ2 -> r >= 13 && r <= 24;
            case DOZ3 -> r >= 25;
            case NUMBER -> r == number;
        };
    }

    private void finish() {
        cancelTask();
        busy = false;
        drawStrip(finalPos);
        String color = rolled == 0 ? "§agrün" : REDS.contains(rolled) ? "§crot" : "§7schwarz";
        msg("§7Die Kugel landet auf §f§l" + rolled + " §7(" + color + "§7)");
        result(wins(rolled) ? roundStake * bet.mult : 0);
    }

    @Override
    public void forceFinish() { finish(); }
}
