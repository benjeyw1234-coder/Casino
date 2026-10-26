package io.github.minevegas;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** Basisklasse aller Casino-Spiele. Jedes Spiel ist der "Holder" seines Inventars. */
public abstract class CasinoGame implements InventoryHolder {
    protected final MineVegas plugin;
    protected final Player player;
    protected final Stake stake = new Stake();
    protected Inventory inv;
    protected BukkitTask task;
    /** true, solange eine Runde läuft (Animation oder offene Blackjack-Hand). */
    protected boolean busy;

    /** Währung/Einsatz der laufenden Runde (damit Umstellen mitten im Spiel nichts kaputt macht). */
    protected Material roundCur = Material.GOLD_INGOT;
    protected int roundStake;

    protected CasinoGame(MineVegas plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    protected static Random rnd() { return ThreadLocalRandom.current(); }

    protected void create(int rows, String title) {
        inv = Bukkit.createInventory(this, rows * 9, Items.text(title));
    }

    @Override
    public Inventory getInventory() { return inv; }

    public void open() {
        build();
        player.openInventory(inv);
    }

    protected abstract void build();

    public abstract void click(int slot, ClickType type);

    /** Beendet eine laufende Runde sofort und zahlt aus (Fenster zu, Spieler weg, Server-Stopp). */
    public abstract void forceFinish();

    public void onClose() {
        if (busy) forceFinish();
        cancelTask();
    }

    protected void schedule(long delay, Runnable r) {
        task = Bukkit.getScheduler().runTaskLater(plugin, r, delay);
    }

    protected void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    protected ItemStack filler() {
        return Items.named(Material.BLACK_STAINED_GLASS_PANE, " ");
    }

    protected void fill(ItemStack it) {
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, it.clone());
    }

    protected void changeStake(ClickType t) {
        if (t.isRightClick()) stake.nextCurrency(); else stake.nextAmount();
    }

    /** Zieht den Einsatz ein. false, wenn der Spieler zu wenig hat. */
    protected boolean startRound() {
        int a = stake.amount();
        Material m = stake.currency();
        if (!Items.take(player, m, a)) {
            msg("§cDu hast nicht genug §f" + stake.label() + "§c.");
            sound(Sound.ENTITY_VILLAGER_NO, 1f);
            return false;
        }
        roundCur = m;
        roundStake = a;
        return true;
    }

    /** Zahlt die Gesamtauszahlung (inkl. Einsatz) aus und meldet das Ergebnis. */
    protected void result(int payout) {
        if (payout > 0) Items.give(player, roundCur, payout);
        String cur = Stake.name(roundCur);
        if (payout > roundStake) {
            msg("§aGewonnen! §f" + payout + "× " + cur + " §7(Gewinn: +" + (payout - roundStake) + ")");
            sound(Sound.ENTITY_PLAYER_LEVELUP, 1f);
            int bm = plugin.getConfig().getInt("broadcast-win-multiplier", 0);
            if (bm > 0 && payout >= (long) roundStake * bm) {
                Bukkit.broadcast(Items.text(plugin.prefix() + "§e" + player.getName()
                        + " §fgewinnt §a" + payout + "× " + cur + "§f im Casino!"));
            }
        } else if (payout == roundStake) {
            msg("§eEinsatz zurück: §f" + payout + "× " + cur);
            sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f);
        } else if (payout > 0) {
            msg("§6Teilgewinn: §f" + payout + "× " + cur + " §7(Verlust: −" + (roundStake - payout) + ")");
        } else {
            msg("§cVerloren. §7(−" + roundStake + "× " + cur + ")");
            sound(Sound.ENTITY_VILLAGER_NO, 1f);
        }
    }

    protected void msg(String s) {
        player.sendMessage(Items.text(plugin.prefix() + s));
    }

    protected void sound(Sound s, float pitch) {
        player.playSound(player.getLocation(), s, 0.6f, pitch);
    }

    protected static String fmt(double m) {
        return m == Math.floor(m) ? "x" + (int) m : "x" + m;
    }
}
