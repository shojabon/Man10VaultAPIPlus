package red.man10.man10vaultapiplus;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.man10vaultapiplus.utils.MySQLAPI;
import red.man10.man10vaultapiplus.utils.VaultAPI;

public final class Man10VaultAPIPlus extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        mysql = new MySQLAPI(this, "Man10VaultAPIPlus");
        vault = new Man10VaultAPI("SampleVaultPlus");
        original = new VaultAPI();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static MySQLAPI mysql = null;
    public Man10VaultAPI vault = null;

    VaultAPI original = null;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("test")){
            Player p = (Player) sender;
            int res = vault.transferMoneyPlayerToCountry(p.getUniqueId(), 100, "Sample Pool Transaction");
        }
        return true;
    }
}
