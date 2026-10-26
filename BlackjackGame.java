package io.github.minevegas;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Blackjack: Dealer steht bei 17, Blackjack zahlt 3:2, Verdoppeln auf den ersten zwei Karten.
 * (Kein Splitten, keine Versicherung.)
 */
public final class BlackjackGame extends CasinoGame {
    private static final String[] RANKS = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};
    private static final int STAKE = 36, DEAL = 38, HIT = 40, STAND = 42, DOUBLE = 44;

    private final List<Integer> deck = new ArrayList<>();
    private final List<Integer> you = new ArrayList<>();
    private final List<Integer> dealer = new ArrayList<>();
    private boolean doubled;

    public BlackjackGame(MineVegas plugin, Player player) { super(plugin, player); }

    @Override
    protected void build() {
        create(5, "§2§lBlackjack");
        redraw();
    }

    private static int value(List<Integer> h) {
        int s = 0, aces = 0;
        for (int c : h) {
            int r = c % 13;
            if (r == 0) {
                aces++;
                s += 11;
            } else {
                s += Math.min(10, r + 1);
            }
        }
        while (s > 21 && aces > 0) {
            s -= 10;
            aces--;
        }
        return s;
    }

    private ItemStack card(int c) {
        int suit = c / 13;
        String color = (suit == 1 || suit == 2) ? "§c" : "§f";
        return Items.named(Material.PAPER, color + "§l" + RANKS[c % 13] + SUITS[suit]);
    }

    private int draw() { return deck.remove(deck.size() - 1); }

    private void redraw() {
        fill(filler());
        boolean hide = busy;
        String dealerLore = dealer.isEmpty() ? "§7–"
                : hide ? "§7Sichtbar: §f" + value(dealer.subList(0, 1))
                : "§7Summe: §f" + value(dealer);
        inv.setItem(0, Items.named(Material.BOOK, "§fDealer", dealerLore));
        for (int i = 0; i < Math.min(8, dealer.size()); i++) {
            inv.setItem(1 + i, (hide && i == 1) ? Items.named(Material.BOOK, "§7Verdeckte Karte") : card(dealer.get(i)));
        }
        inv.setItem(18, Items.named(Material.WRITABLE_BOOK, "§fDeine Hand",
                you.isEmpty() ? "§7–" : "§7Summe: §f" + value(you)));
        for (int i = 0; i < Math.min(8, you.size()); i++) inv.setItem(19 + i, card(you.get(i)));

        if (busy) {
            inv.setItem(HIT, Items.named(Material.LIME_DYE, "§aKarte", "§7Noch eine Karte ziehen"));
            inv.setItem(STAND, Items.named(Material.RED_DYE, "§cHalten", "§7Dealer ist an der Reihe"));
            if (you.size() == 2 && !doubled) {
                inv.setItem(DOUBLE, Items.named(Material.GOLD_INGOT, "§6Verdoppeln",
                        "§7Einsatz verdoppeln, genau eine Karte"));
            }
        } else {
            inv.setItem(STAKE, stake.button());
            inv.setItem(DEAL, Items.named(Material.EMERALD, "§a§lKarten geben", "§7Neue Runde starten"));
        }
    }

    @Override
    public void click(int slot, ClickType t) {
        if (!busy) {
            if (slot == STAKE) {
                changeStake(t);
                redraw();
            } else if (slot == DEAL) {
                deal();
            }
            return;
        }
        if (slot == HIT) hit();
        else if (slot == STAND) stand();
        else if (slot == DOUBLE) doubleDown();
    }

    private void deal() {
        if (!startRound()) return;
        deck.clear();
        for (int i = 0; i < 52; i++) deck.add(i);
        Collections.shuffle(deck, rnd());
        you.clear();
        dealer.clear();
        doubled = false;
        you.add(draw());
        dealer.add(draw());
        you.add(draw());
        dealer.add(draw());
        busy = true;
        sound(Sound.UI_BUTTON_CLICK, 1.4f);

        boolean pbj = value(you) == 21;
        boolean dbj = value(dealer) == 21;
        if (pbj || dbj) {
            finishRound(pbj && dbj ? roundStake : pbj ? (roundStake * 5) / 2 : 0);
            return;
        }
        redraw();
    }

    private void hit() {
        you.add(draw());
        sound(Sound.UI_BUTTON_CLICK, 1.2f);
        int v = value(you);
        if (v > 21) finishRound(0);
        else if (v == 21) stand();
        else redraw();
    }

    private void stand() {
        while (value(dealer) < 17) dealer.add(draw());
        int p = value(you), d = value(dealer);
        int pay;
        if (p > 21) pay = 0;
        else if (d > 21 || p > d) pay = roundStake * 2;
        else if (p == d) pay = roundStake;
        else pay = 0;
        finishRound(pay);
    }

    private void doubleDown() {
        if (you.size() != 2 || doubled) return;
        if (!Items.take(player, roundCur, roundStake)) {
            msg("§cDu hast nicht genug zum Verdoppeln.");
            return;
        }
        roundStake *= 2;
        doubled = true;
        you.add(draw());
        if (value(you) > 21) finishRound(0);
        else stand();
    }

    private void finishRound(int payout) {
        busy = false;
        redraw();
        msg("§7Du: §f" + value(you) + " §7– Dealer: §f" + value(dealer));
        result(payout);
    }

    @Override
    public void forceFinish() {
        if (busy) stand();
    }
}
