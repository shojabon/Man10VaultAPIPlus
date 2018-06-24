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
    private MoneyPoolData data = new MoneyPoolData();

    private MySQLAPI mysql;
    private Man10VaultAPI vault;

    private MoneyPoolManager manager = new MoneyPoolManager();

    public MoneyPoolObject(String pluginName, MoneyPoolTerm moneyPoolTerm, MoneyPoolType moneyPoolType, String memo){
        mysql = new  MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        vault = new Man10VaultAPI(pluginName);
        long id = createNewMoneyPool(-1, moneyPoolTerm, moneyPoolType,false,null,null,pluginName,memo);
        data.term = moneyPoolTerm;
        data.value = 0;
        data.plugin = pluginName;
        data.memo = memo;
        data.id = id;
        data.frozen = false;
        manager.put(id, data);
    }

    public MoneyPoolObject(String pluginName){
        mysql = new  MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        vault = new Man10VaultAPI(pluginName);
        long id = createNewMoneyPool(-1, null, null,false,null,null,pluginName,null);
        data.term = MoneyPoolTerm.UNKNOWN;
        data.value = 0;
        data.plugin = pluginName;
        data.id = id;
        data.frozen = false;
        manager.put(id, data);

    }

    public MoneyPoolObject(String pluginName, String memo){
        mysql = new  MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        vault = new Man10VaultAPI(pluginName);
        long id = createNewMoneyPool(-1, null, null,false,null,null,pluginName,memo);
        data.term = MoneyPoolTerm.UNKNOWN;
        data.value = 0;
        data.plugin = pluginName;
        data.memo = memo;
        data.id = id;
        data.frozen = false;
        manager.put(id, data);
    }

    private long createNewMoneyPool(int pId, MoneyPoolTerm moneyPoolTerm, MoneyPoolType moneyPoolType, boolean wired, UUID wiredUuid, String wiredName, String pluginName, String memo){
        if(moneyPoolTerm == null){
            moneyPoolTerm = MoneyPoolTerm.UNKNOWN;
        }
        if(moneyPoolType == null){
            moneyPoolType = MoneyPoolType.UNKNOWN;
        }
        if(!mysql.connectable()){
            data.available = false;
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
                data.available = false;
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
        manager.put(data.id, this);
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
                data.available = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(data.available){
            ResultSet rs = mysql.query("SELECT * FROM man10_moneypool WHERE id = '" + id + "' LIMIT 1");
            try {
                if(rs == null){
                    data.available = false;
                    mysql.close();
                }else{
                    while (rs.next()) {
                        data.id = rs.getLong("id");
                        data.term = MoneyPoolTerm.valueOf(rs.getString("pool_term"));
                        data.wired = mysql.convertMysqlToBoolean(rs.getInt("wired"));
                        try{
                            data.wiredUuid = UUID.fromString(rs.getString("wired_uuid"));
                        }catch(IllegalArgumentException e){
                        }
                        data.wiredName = rs.getString("wired_name");

                        data.value = rs.getDouble("balance");
                        data.plugin = rs.getString("plugin");
                        data.pId = rs.getLong("plugin_id");
                        data.memo = rs.getString("memo");
                        data.frozen = mysql.convertMysqlToBoolean(rs.getInt("frozen"));
                    }
                    rs.close();
                    mysql.close();
                }
            } catch (SQLException | NullPointerException e) {
                data.available = false;
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
                data.available = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(data.available){
            ResultSet rs = mysql.query("SELECT * FROM man10_moneypool WHERE id = '" + id + "' LIMIT 1");
            try {
                if(rs == null){
                    data.available = false;
                    mysql.close();
                }else{
                    while (rs.next()) {
                        data.id = rs.getLong("id");
                        data.term = MoneyPoolTerm.valueOf(rs.getString("pool_term"));
                        data.wired = mysql.convertMysqlToBoolean(rs.getInt("wired"));
                        try{
                            data.wiredUuid = UUID.fromString(rs.getString("wired_uuid"));
                        }catch(IllegalArgumentException e){
                        }
                        data.wiredName = rs.getString("wired_name");

                        data.value = rs.getDouble("balance");
                        data.plugin = rs.getString("plugin");
                        data.pId = rs.getLong("plugin_id");
                        data.memo = rs.getString("memo");
                        data.frozen = mysql.convertMysqlToBoolean(rs.getInt("frozen"));
                    }
                    rs.close();
                    mysql.close();
                }
            } catch (SQLException | NullPointerException e) {
                data.available = false;
            }
        }
        return this;
    }

    public int dumpBalanceToWiredPlayer(TransactionCategory transactionCategory, TransactionType transactionType, String memo){
        if(!data.available){
            return -1;
        }
        if(!data.wired){
            return -2;
        }
        if(data.frozen){
            return -3;
        }
        return vault.transferMoneyPoolToPlayer(data.id, data.wiredUuid, data.value, transactionCategory, transactionType, memo);
    }

    public int transferBalanceToPlayer(UUID toUUID, double value, TransactionCategory transactionCategory, TransactionType transactionType, String memo){
        if(!data.available){
            return -1;
        }
        if(data.frozen){
            return -3;
        }
        return vault.transferMoneyPoolToPlayer(data.id, toUUID, value, transactionCategory, transactionType, memo);
    }

    public int transferBalanceToPool(long toPoolId, double value, TransactionCategory transactionCategory, TransactionType transactionType, String memo){
        if(!data.available){
            return -1;
        }
        if(data.frozen){
            return -3;
        }
        return vault.transferMoneyPoolToPool(data.id, toPoolId, value, transactionCategory, transactionType, memo);
    }

    public int transferBalanceToCountry(double value, TransactionCategory transactionCategory, TransactionType transactionType, String memo){
        if(!data.available){
            return -1;
        }
        if(data.frozen){
            return -3;
        }
        return vault.transferMoneyPoolToCountry(data.id, value, transactionCategory, transactionType, memo);
    }

    public int transferMoneyPlayerToPool(UUID uuidFrom,  double value, TransactionCategory transactionCategory,TransactionType transactionType, String memo){
        if(!data.frozen){
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
        data.value = balance;
        return balance;
    }

    public boolean isAvailable(){
        return data.available;
    }

    public double getBalance(){
        return data.value;
    }

    public long getId(){
        return data.id;
    }

    public String getName(){
        if(getId() == 1){
            return "CTS POOL:";
        }
        return "POOL:";
    }


}
