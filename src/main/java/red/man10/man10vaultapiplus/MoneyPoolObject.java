package red.man10.man10vaultapiplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class MoneyPoolObject {
    long id;
    MoneyPoolTerm term;

    boolean wired;
    UUID wiredUuid;
    String name;

    double value;
    String plugin;
    long pId;
    String memo;
    boolean frozen;

    MySQLAPI mysql;

    public MoneyPoolObject(){
        mysql = new MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
    }

    public MoneyPoolObject(long id){
        mysql = new MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
    }
}
