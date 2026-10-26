package io.github.minevegas;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineVegas extends JavaPlugin {
    private CasinoBlocks blocks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        blocks = new CasinoBlocks(this);
        getServer().getPluginManager().registerEvents(new CasinoListener(this), this);
        PluginCommand cmd = getCommand("casino");
        if (cmd != null) {
            CasinoCommand c = new CasinoCommand(this);
            cmd.setExecutor(c);
            cmd.setTabCompleter(c);
        }
    }

    @Override
    public void onDisable() {
        // laufende Runden sauber abschließen, damit niemand seinen Einsatz verliert
        for (Player p : getServer().getOnlinePlayers()) {
            if (p.getOpenInventory().getTopInventory().getHolder() instanceof CasinoGame g) {
                g.onClose();
                p.closeInventory();
            }
        }
    }

    public CasinoBlocks blocks() { return blocks; }

    public String prefix() { return "§6[§eCasino§6] §f"; }
}
