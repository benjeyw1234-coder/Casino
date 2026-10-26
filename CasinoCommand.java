package io.github.minevegas;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class CasinoCommand implements CommandExecutor, TabCompleter {
    private final MineVegas plugin;

    public CasinoCommand(MineVegas plugin) { this.plugin = plugin; }

    private void say(CommandSender s, String m) { s.sendMessage(Items.text(plugin.prefix() + m)); }

    private String games() {
        List<String> l = new ArrayList<>();
        for (GameType t : GameType.values()) l.add(t.key());
        return String.join(", ", l);
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String label, String[] a) {
        if (a.length == 0) {
            say(s, "§e/casino menu §7| §e/casino give <spiel> [anzahl] §7| §e/casino play <spiel> §7| §e/casino reload");
            say(s, "§7Spiele: §f" + games());
            return true;
        }
        switch (a[0].toLowerCase()) {
            case "give" -> {
                if (!s.hasPermission("minevegas.admin")) { say(s, "§cKeine Berechtigung."); return true; }
                if (!(s instanceof Player p)) { say(s, "§cNur für Spieler."); return true; }
                GameType t = a.length > 1 ? GameType.byKey(a[1]) : null;
                if (t == null) { say(s, "§cSpiel unbekannt. Verfügbar: §f" + games()); return true; }
                int n = 1;
                if (a.length > 2) {
                    try { n = Math.max(1, Math.min(64, Integer.parseInt(a[2]))); }
                    catch (NumberFormatException ignored) { }
                }
                var item = plugin.blocks().item(t);
                item.setAmount(n);
                p.getInventory().addItem(item).values()
                        .forEach(o -> p.getWorld().dropItem(p.getLocation(), o));
                say(s, "§a" + n + "× " + t.display() + " §ferhalten.");
            }
            case "menu" -> {
                if (!s.hasPermission("minevegas.admin") && !s.hasPermission("minevegas.menu")) {
                    say(s, "§cKeine Berechtigung.");
                    return true;
                }
                if (!(s instanceof Player p)) { say(s, "§cNur für Spieler."); return true; }
                new CreativeMenu(plugin, p).open();
            }
            case "play" -> {
                if (!(s instanceof Player p)) { say(s, "§cNur für Spieler."); return true; }
                if (!plugin.getConfig().getBoolean("allow-play-command", false)
                        && !s.hasPermission("minevegas.admin")) {
                    say(s, "§cBitte benutze einen Casino-Automaten.");
                    return true;
                }
                if (!s.hasPermission("minevegas.play")) { say(s, "§cKeine Berechtigung."); return true; }
                GameType t = a.length > 1 ? GameType.byKey(a[1]) : null;
                if (t == null) { say(s, "§cSpiel unbekannt. Verfügbar: §f" + games()); return true; }
                t.create(plugin, p).open();
            }
            case "reload" -> {
                if (!s.hasPermission("minevegas.admin")) { say(s, "§cKeine Berechtigung."); return true; }
                plugin.reloadConfig();
                plugin.blocks().load();
                say(s, "§aKonfiguration neu geladen.");
            }
            default -> say(s, "§cUnbekannter Befehl.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String label, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) {
            for (String x : List.of("menu", "give", "play", "reload")) if (x.startsWith(a[0].toLowerCase())) out.add(x);
        } else if (a.length == 2 && (a[0].equalsIgnoreCase("give") || a[0].equalsIgnoreCase("play"))) {
            for (GameType t : GameType.values()) if (t.key().startsWith(a[1].toLowerCase())) out.add(t.key());
        }
        return out;
    }
}
