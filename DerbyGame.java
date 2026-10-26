package io.github.minevegas;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Entenrennen: fünf Enten, tippe auf den Sieger. */
public final class DerbyGame extends CasinoGame {
    private static final Material[] DUCK = {Material.RED_DYE, Material.YELLOW_DYE, Material.LIME_DYE,
            Material.LIGHT_BLUE_DYE, Material.PINK_DYE};
    private static final String[] NAME = {"§cEnte 1", "§eEnte 2", "§aEnte 3", "§bEnte 4", "§dEnte 5"};
    private static final int STAKE = 45, FIRST_PICK = 47, START = 53;

    private int pick = 0;
    private final int[] pos = new int[5];
    private int winner = -1;

    public DerbyGame(MineVegas plugin, Player player) { super(plugin, player); }

    @Override
    protected void build() {
        create(6, "§e§lDuck Derby");
        fill(filler());
        drawLanes();
        drawControls();
    }

    private double payout() { return plugin.getConfig().getDouble("duck-derby.payout", 4.5); }

    private void drawControls() {
        inv.setItem(STAKE, stake.button());
        for (int i = 0; i < 5; i++) {
            ItemStack it = Items.named(DUCK[i], NAME[i], "§7Auszahlung: §e" + fmt(payout()),
                    i == pick ? "§a✔ dein Tipp" : "§7Klicken zum Tippen");
            inv.setItem(FIRST_PICK + i, i == pick ? Items.glow(it) : it);
        }
        inv.setItem(START, Items.named(Material.LIME_DYE, "§a§lRennen starten", "§7Tipp: " + NAME[pick]));
    }

    private void drawLanes() {
        for (int l = 0; l < 5; l++) {
            for (int c = 0; c < 9; c++) {
                ItemStack it = c == 8 ? Items.named(Material.LIME_STAINED_GLASS_PANE, "§aZiel")
                        : Items.named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ");
                if (c == pos[l]) it = Items.named(DUCK[l], NAME[l]);
                inv.setItem(l * 9 + c, it);
            }
        }
    }

    @Override
    public void click(int slot, ClickType t) {
        if (busy) return;
        if (slot == STAKE) {
            changeStake(t);
            drawControls();
        } else if (slot >= FIRST_PICK && slot < FIRST_PICK + 5) {
            pick = slot - FIRST_PICK;
            drawControls();
        } else if (slot == START) {
            if (!startRound()) return;
            busy = true;
            winner = -1;
            for (int i = 0; i < 5; i++) pos[i] = 0;
            drawLanes();
            step();
        }
    }

    /** Ein Rennschritt. true, wenn das Rennen entschieden ist. */
    private boolean advance() {
        List<Integer> arrived = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (rnd().nextBoolean()) pos[i]++;
            if (pos[i] >= 8) arrived.add(i);
        }
        if (arrived.isEmpty()) return false;
        winner = arrived.get(rnd().nextInt(arrived.size()));
        for (int i = 0; i < 5; i++) pos[i] = i == winner ? 8 : Math.min(pos[i], 7);
        return true;
    }

    private void step() {
        boolean done = advance();
        drawLanes();
        sound(Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f);
        if (done) finish();
        else schedule(3, this::step);
    }

    private void finish() {
        cancelTask();
        busy = false;
        drawLanes();
        msg("§7Es gewinnt §f" + NAME[winner] + "§7!");
        result(winner == pick ? (int) Math.floor(roundStake * payout()) : 0);
    }

    @Override
    public void forceFinish() {
        cancelTask();
        while (winner < 0) advance();
        finish();
    }
}
