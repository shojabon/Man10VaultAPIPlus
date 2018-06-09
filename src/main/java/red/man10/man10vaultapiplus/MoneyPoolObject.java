package red.man10.man10vaultapiplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MoneyPoolObject {
    long id;
    MoneyPoolTerm term;

    boolean wired;
    UUID wiredUuid;
    String wiredName;

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
        ResultSet rs = mysql.query("SELECT * FROM man10_moneypool WHERE id = '" + id  +"' ORDER BY DESC LIMIT 1");
        try {
            while(rs.next()){
                id = rs.getLong("id");
                term = MoneyPoolTerm.valueOf(rs.getString("term"));
                wired = mysql.convertMysqlToBoolean(rs.getInt("wired"));
                wiredUuid = UUID.fromString(rs.getString("wired_uuid"));
                wiredName = rs.getString("wired_name");

                value = rs.getDouble("balance");
                plugin = rs.getString("plugin");
                pId = rs.getLong("plugin_id");
                memo = rs.getString("memo");
                frozen = mysql.convertMysqlToBoolean(rs.getInt("frozen"));
            }
            rs.close();
            mysql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
