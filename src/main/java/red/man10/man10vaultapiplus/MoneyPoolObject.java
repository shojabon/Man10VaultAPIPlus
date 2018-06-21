package red.man10.man10vaultapiplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.man10vaultapiplus.enums.MoneyPoolTerm;
import red.man10.man10vaultapiplus.enums.MoneyPoolType;
import red.man10.man10vaultapiplus.enums.TransactionCategory;
import red.man10.man10vaultapiplus.enums.TransactionType;
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
    private Man10VaultAPI vault;

    public MoneyPoolObject(String pluginName, MoneyPoolTerm moneyPoolTerm, MoneyPoolType moneyPoolType, String memo){
        mysql = new  MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        vault = new Man10VaultAPI(pluginName);
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
        vault = new Man10VaultAPI(pluginName);
        long id = createNewMoneyPool(-1, null, null,false,null,null,pluginName,null);
        this.term = MoneyPoolTerm.UNKNOWN;
        this.value = 0;
        this.plugin = pluginName;
        this.id = id;
        frozen = false;
    }

    public MoneyPoolObject(String pluginName, String memo){
        mysql = new  MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        vault = new Man10VaultAPI(pluginName);
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

    public MoneyPoolObject(String pluginName, UUID wiredUuid, int pId, MoneyPoolTerm moneyPoolTerm, MoneyPoolType moneyPoolType, String memo) {
        boolean error = false;
        if(wiredUuid == null) {
            try {
                throw new Man10VaultExeption("Wired UUID Not Defined");
            } catch (Man10VaultExeption man10VaultExeption) {
                man10VaultExeption.printStackTrace();
                error = true;
                this.available = false;
            }
        }
        if(!error){
            vault = new Man10VaultAPI(pluginName);
            mysql = new MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
            ResultSet rs = mysql.query("SELECT count(*) FROM man10_moneypool WHERE wired_uuid ='" + wiredUuid + "' AND plugin_id ='" + pId + "' AND plugin = '" + pluginName + "'");
            try {
                int i = 0;
                while (rs.next()) {
                    i = rs.getInt("count(*)");
                }
                rs.close();
                mysql.close();
                Bukkit.broadcastMessage(String.valueOf(i));
                if (i <= 0) {
                    long res = createNewMoneyPool(pId, moneyPoolTerm, moneyPoolType, true, wiredUuid, Bukkit.getOfflinePlayer(wiredUuid).getName(), pluginName, memo);
                    getMoneyPoolObject(res);
                }else{
                    getMoneyPoolObject(getWiredMoneyPoolId(pluginName, wiredUuid, pId));
                }
            } catch (SQLException e) {
            }
        }
    }

    public MoneyPoolObject(String pluginName, long id) {
        mysql = new MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        vault = new Man10VaultAPI(pluginName);
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

    private long getWiredMoneyPoolId(String pluginName, UUID uuid, int pId){
        ResultSet count = mysql.query("SELECT id FROM man10_moneypool WHERE plugin='" + pluginName + "' and plugin_id ='" + pId + "' and wired_uuid ='" + uuid + "'");
        long id = -1;
        try {
            while(count.next()){
                id = count.getLong("id");
            }
            count.close();
            mysql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }


    private MoneyPoolObject getMoneyPoolObject(long id){
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
        return this;
    }

    public int dumpBalanceToWiredPlayer(TransactionCategory transactionCategory, TransactionType transactionType, String memo){
        if(!this.available){
            return -1;
        }
        if(!this.wired){
            return -2;
        }
        if(this.frozen){
            return -3;
        }
        return vault.transferMoneyPoolToPlayer(this.id, this.wiredUuid, this.value, transactionCategory, transactionType, memo);
    }

    public int transferBalanceToPlayer(UUID toUUID, double value, TransactionCategory transactionCategory, TransactionType transactionType, String memo){
        if(!this.available){
            return -1;
        }
        if(this.frozen){
            return -3;
        }
        return vault.transferMoneyPoolToPlayer(this.id, toUUID, value, transactionCategory, transactionType, memo);
    }

    public int transferBalanceToPool(long toPoolId, double value, TransactionCategory transactionCategory, TransactionType transactionType, String memo){
        if(!this.available){
            return -1;
        }
        if(this.frozen){
            return -3;
        }
        return vault.transferMoneyPoolToPool(this.id, toPoolId, value, transactionCategory, transactionType, memo);
    }

    public int transferBalanceToCountry(double value, TransactionCategory transactionCategory, TransactionType transactionType, String memo){
        if(!this.available){
            return -1;
        }
        if(this.frozen){
            return -3;
        }
        return vault.transferMoneyPoolToCountry(this.id, value, transactionCategory, transactionType, memo);
    }

    public int transferMoneyPlayerToPool(UUID uuidFrom,  double value, TransactionCategory transactionCategory,TransactionType transactionType, String memo){
        if(!frozen){
            return -1;
        }
        int a = vault.transferMoneyPlayerToPool(uuidFrom, getId(), value, transactionCategory, transactionType, memo);
        getCurrentBalance();
        return a;
    }


    public double getCurrentBalance(){
        if(!isAvailable()){
            return -1;
        }
        ResultSet rs = mysql.query("SELECT balance FROM man10_moneypool WHERE id='" + getId() + "' LIMIT 1");
        double balance = 0;
        try {
            while (rs.next()){
                balance = rs.getDouble("balance");
            }
            rs.close();
            mysql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.value = balance;
        return balance;
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
