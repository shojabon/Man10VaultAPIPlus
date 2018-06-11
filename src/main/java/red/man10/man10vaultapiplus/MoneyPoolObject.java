package red.man10.man10vaultapiplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.man10vaultapiplus.utils.Man10VaultExeption;
import red.man10.man10vaultapiplus.utils.MySQLAPI;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MoneyPoolObject {
    private long id;
    private MoneyPoolTerm term;

    private boolean wired;
    private UUID wiredUuid;
    private String wiredName;

    private double value;
    private String plugin;
    private long pId;
    private String memo;
    private boolean frozen;

    private boolean available = true;

    private MySQLAPI mysql;

    public MoneyPoolObject(String pluginName, MoneyPoolTerm moneyPoolTerm, MoneyPoolType moneyPoolType, String memo){
        mysql = new  MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        long id = createNewMoneyPool(-1, moneyPoolTerm, moneyPoolType,false,null,null,pluginName,memo);
        this.term = moneyPoolTerm;
        this.value = 0;
        this.plugin = pluginName;
        this.memo = memo;
        this.id = id;
        frozen = false;
    }

    public MoneyPoolObject(String pluginName){
        mysql = new  MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        long id = createNewMoneyPool(-1, null, null,false,null,null,pluginName,null);
        this.term = MoneyPoolTerm.UNKNOWN;
        this.value = 0;
        this.plugin = pluginName;
        this.id = id;
        frozen = false;
    }

    public MoneyPoolObject(String pluginName, String memo){
        mysql = new  MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        long id = createNewMoneyPool(-1, null, null,false,null,null,pluginName,memo);
        this.term = MoneyPoolTerm.UNKNOWN;
        this.value = 0;
        this.plugin = pluginName;
        this.memo = memo;
        this.id = id;
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
            this.available = false;
            return -999;
        }
        String uuidPut = "";
        if(memo == null){
            memo = "";
        }
        if(!wired){
            uuidPut = "";
            wiredName = "";
        }else{
            uuidPut = wiredUuid.toString();
        }
        int idd = mysql.executeGetId("INSERT INTO man10_moneypool (`id`,`plugin_id`,`balance`,`pool_term`,`type`,`wired`,`wired_uuid`,`wired_name`,`plugin`,`memo`,`frozen`)" +
                " VALUE ('0','" + pId + "','" + 0 + "','" + moneyPoolTerm + "','" + moneyPoolType + "','" + mysql.convertBooleanToMysql(wired) + "','" + uuidPut + "','" + wiredName + "','" + pluginName + "','" + memo + "','" + 0 + "');");
        return idd;
    }

    public MoneyPoolObject(String pluginName, UUID wiredUuid, int pid, MoneyPoolTerm moneyPoolTerm, MoneyPoolType moneyPoolType, String memo){
        if(wiredUuid == null){
            try {
                throw new Man10VaultExeption("Wired UUID Not Defined");
            } catch (Man10VaultExeption man10VaultExeption) {
                man10VaultExeption.printStackTrace();
            }
        }
        mysql = new MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        ResultSet rs = mysql.query("SELECT count(*) FROM man10_moneypool WHERE wired_uuid ='" + wiredUuid + "' AND plugin_id ='" + pid + "' AND plugin = '" + pluginName + "'");
        try{
            int i = 0;
            while(rs.next()){
                i = rs.getInt("count(*)");
            }
            rs.close();
            mysql.close();
            if(i <= 0){
                long res = createNewMoneyPool(pid, moneyPoolTerm, moneyPoolType, true, wiredUuid, Bukkit.getOfflinePlayer(wiredUuid).getName(), pluginName, memo);
            }
        }catch(SQLException e){

        }
    }


    public MoneyPoolObject(long id) {
        mysql = new MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        ResultSet count = mysql.query("SELECT count(*) FROM man10_moneypool WHERE id = '" + id + "' LIMIT 1");
        try {
            int i = 0;
            if(count.next()){
                i = count.getInt("count(*)");
            }
            count.close();
            mysql.close();
            if(i  <= 0){
                this.available = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(this.available){
            ResultSet rs = mysql.query("SELECT * FROM man10_moneypool WHERE id = '" + id + "' LIMIT 1");
            try {
                if(rs == null){
                    this.available = false;
                    mysql.close();
                }else{
                    while (rs.next()) {
                        this.id = rs.getLong("id");
                        this.term = MoneyPoolTerm.valueOf(rs.getString("pool_term"));
                        this.wired = mysql.convertMysqlToBoolean(rs.getInt("wired"));
                        try{
                            this.wiredUuid = UUID.fromString(rs.getString("wired_uuid"));
                        }catch(IllegalArgumentException e){
                        }
                        this.wiredName = rs.getString("wired_name");

                        this.value = rs.getDouble("balance");
                        this.plugin = rs.getString("plugin");
                        this.pId = rs.getLong("plugin_id");
                        this.memo = rs.getString("memo");
                        this.frozen = mysql.convertMysqlToBoolean(rs.getInt("frozen"));
                    }
                    rs.close();
                    mysql.close();
                }
            } catch (SQLException | NullPointerException e) {
                this.available = false;
            }
        }
    }


    public boolean isAvailable(){
        return this.available;
    }

    public double getBalance(){
        return this.value;
    }

    public long getId(){
        return this.id;
    }

    public String getName(){
        if(getId() == 1){
            return "CTS POOL:";
        }
        return "POOL:";
    }


}
