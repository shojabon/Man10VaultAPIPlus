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
        vault = new Man10VaultAPI("Man10VaultAPIPlus");
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
            long a = System.currentTimeMillis();
            for(int i = 0;i < 100;i++){
                vault.takePlayerMoney(p.getUniqueId(), 1, null);
            }
            long b = System.currentTimeMillis();
            Bukkit.broadcastMessage(String.valueOf(b - a));
        }
        return true;
    }
}
