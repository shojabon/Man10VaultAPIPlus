package red.man10.man10vaultapiplus;

import org.bukkit.plugin.java.JavaPlugin;

public final class Man10VaultAPIPlus extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        mysql = new MySQLAPI(this, "Man10VaultAPIPlus");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static MySQLAPI mysql = null;
}
