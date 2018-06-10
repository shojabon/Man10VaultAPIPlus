package red.man10.man10vaultapiplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.man10vaultapiplus.utils.MySQLAPI;

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

    public MoneyPoolObject(String pluginName, MoneyPoolTerm moneyPoolTerm, MoneyPoolType moneyPoolType, String memo){
        mysql = new  MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        this.term = moneyPoolTerm;
        this.value = 0;
        this.plugin = pluginName;
        this.memo = memo;
        frozen = false;
    }

    private long createNewMoneyPool(int pId, MoneyPoolTerm moneyPoolTerm, MoneyPoolType moneyPoolType, boolean wired, UUID wiredUuid, String wiredName, String pluginName, String memo){
        if(moneyPoolTerm == null){
            moneyPoolTerm = MoneyPoolTerm.UNKNOWN;
        }
        if(moneyPoolType == null){
            moneyPoolType = MoneyPoolType.UNKNOWN;
        }
        if(!mysql.connectable()){
            return -999;
        }
        int idd = mysql.executeGetId("INSERT INTO man10_moneypool (`id`,`plugin_id`,`balance`,`pool_term`,`type`,`wired`,`wired_uuid`,`wired_name`,`plugin`,`memo`,`frozen`)" +
                " VALUE ('0','" + pId + "','" + 0 + "','" + moneyPoolTerm + "','" + moneyPoolType + "','" + mysql.convertBooleanToMysql(wired) + "','" + wiredUuid + "','" + wiredName + "','" + pluginName + "','" + memo + "','" + 0 + "');");
        return idd;
    }


    public MoneyPoolObject(long id){
        mysql = new MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        ResultSet rs = mysql.query("SELECT * FROM man10_moneypool WHERE id = '" + id  +"' ORDER BY DESC LIMIT 1");
        try {
            while(rs.next()){
                this.id = rs.getLong("id");
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
