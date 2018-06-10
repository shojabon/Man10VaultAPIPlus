package red.man10.man10vaultapiplus;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.man10vaultapiplus.utils.MySQLAPI;
import red.man10.man10vaultapiplus.utils.VaultAPI;

import java.util.UUID;

public class Man10VaultAPI {

    MySQLAPI mysql;
    VaultAPI vault = null;
    String pluginName;

    public Man10VaultAPI(String pluginName){
        mysql = new  MySQLAPI((JavaPlugin) Bukkit.getPluginManager().getPlugin("Man10VaultAPIPlus"), "Man10VaultAPI");
        vault = new VaultAPI();
        this.pluginName = pluginName;
    }

    private int  createTransactionLog(TransactionCategory category, TransactionType type, String pluginName, double value,
                                    String fromName, UUID fromUUID,
                                    String toName, UUID toUUID,
                                    double fromOldBalance, double fromNewBalance,
                                    double toOldBalance, double toNewBalance,
                                    long poolId, TransactionLogType transactionLogType,
                                    String memo) {
        if (!mysql.connectable()) {
            return -999;
        }
        String toUuidPut = "";
        String fromUuidPut = "";
        if(fromUUID == null){
            fromUuidPut = "";
        }else{
            fromUuidPut = fromUUID.toString();
        }
        if(toUUID == null){
            toUuidPut = "";
        }else{
            toUuidPut = toUUID.toString();
        }
        if(fromName == null){
            fromName = "";
        }
        if(toName == null){
            toName = "";
        }
        if (category == null) {
            category = TransactionCategory.UNKNOWN;
        }
        if (type == null) {
            type = TransactionType.UNKNOWN;
        }
        if (transactionLogType == null) {
            transactionLogType = TransactionLogType.RAW;
        }
        boolean res = mysql.execute("INSERT INTO man10_transaction (`id`,`category`,`type`,`plugin`,`balance`,`from_name`,`to_name`,`from_uuid`,`to_uuid`,`from_old_balance`,`from_new_balance`,`to_old_balance`,`to_new_balance`,`pool_id`,`log_type`,`memo`) VALUES " +
                "('0','" + category + "','" + type + "','" + pluginName + "','" + value + "','" + fromName + "','" + toName + "','" + fromUuidPut + "','" + toUuidPut + "','" + fromOldBalance + "','" + fromNewBalance + "','" + toOldBalance + "','" + toNewBalance + "','" + poolId + "','" + transactionLogType + "','" + memo + "');");
        if(res){
            return 0;
        }
        return -998;
    }

    public int transferMoneyPlayerToPlayer(UUID uuidFrom, UUID uuidTo, double value, TransactionCategory transactionCategory,TransactionType transactionType, String memo){
        OfflinePlayer from = Bukkit.getOfflinePlayer(uuidFrom);
        OfflinePlayer to = Bukkit.getOfflinePlayer(uuidTo);
        if(transactionCategory == null){
            transactionCategory = TransactionCategory.UNKNOWN;
        }
        if(transactionType == null){
            transactionType = TransactionType.UNKNOWN;
        }
        if(memo == null){
            memo = "";
        }
        if (from == null){
            return -1;
        }
        if(to == null){
            return -2;
        }
        double fromOldBalance = vault.getBalance(from);
        double toOldBalance = vault.getBalance(to);
        if(fromOldBalance < value){
            return -3;
        }
        boolean fromS = vault.silentWithdraw(from, value);
        boolean toS = vault.silentDeposit(to,value);
        if(!fromS || !toS){
            if(fromS){
                vault.silentDeposit(from, value);
            }
            if(toS){
                vault.silentWithdraw(to, value);
            }
            return -4;
        }
        double fromNewBalance = fromOldBalance - value;
        double toNewBalance = toOldBalance + value;
        int a = createTransactionLog(transactionCategory, transactionType, this.pluginName, value, from.getName(), uuidFrom, to.getName(), uuidTo, fromOldBalance, fromNewBalance, toOldBalance, toNewBalance, -1, TransactionLogType.BOTH, memo);
        return a;
    }

    public int transferMoneyPlayerToPool(UUID uuidFrom, long poolId, double value, TransactionCategory transactionCategory,TransactionType transactionType, String memo){
        OfflinePlayer from = Bukkit.getOfflinePlayer(uuidFrom);
        MoneyPoolObject pool = new MoneyPoolObject(poolId);
        if(transactionCategory == null){
            transactionCategory = TransactionCategory.UNKNOWN;
        }
        if(transactionType == null){
            transactionType = TransactionType.UNKNOWN;
        }
        if(memo == null){
            memo = "";
        }
        if (from == null){
            return -1;
        }
        if(!pool.isAvailable()){
            return -2;
        }
        double fromOldBalance = vault.getBalance(from);
        double toOldBalance = pool.getBalance();
        if(fromOldBalance < value){
            return -3;
        }
        boolean fromS = vault.silentWithdraw(from, value);
        boolean toS = mysql.execute("UPDATE man10_moneypool SET balance = balance +'" + value + "' WHERE id = '" + pool.getId() + "'");
        if(!fromS || !toS){
            if(fromS){
                vault.silentDeposit(from, value);
            }
            if(toS){
                mysql.execute("UPDATE man10_moneypool SET balance = balance - '" + value + "' WHERE id = '" + pool.getId() + "'");
            }
            return -4;
        }
        double fromNewBalance = fromOldBalance - value;
        double toNewBalance = toOldBalance + value;
        int a = createTransactionLog(transactionCategory, transactionType, this.pluginName, value, from.getName(), uuidFrom, "POOL:" + pool.getId(),  null, fromOldBalance, fromNewBalance, toOldBalance, toNewBalance, pool.getId(), TransactionLogType.BOTH, memo);
        return a;
    }

    public int transferMoneyPlayerToCountry(UUID uuidFrom, double value, String memo){
        OfflinePlayer from = Bukkit.getOfflinePlayer(uuidFrom);
        MoneyPoolObject pool = new MoneyPoolObject(1);
        double fromOldBalance = vault.getBalance(from);
        double toOldBalance = pool.getBalance();
        if(fromOldBalance < value){
            return -3;
        }
        boolean fromS = vault.silentWithdraw(from, value);
        boolean toS = mysql.execute("UPDATE man10_moneypool SET balance = balance +'" + value + "' WHERE id = '" + pool.getId() + "'");
        if(!fromS || !toS){
            if(fromS){
                vault.silentDeposit(from, value);
            }
            if(toS){
                mysql.execute("UPDATE man10_moneypool SET balance = balance - '" + value + "' WHERE id = '" + pool.getId() + "'");
            }
            return -4;
        }
        double fromNewBalance = fromOldBalance - value;
        double toNewBalance = toOldBalance + value;
        int a = createTransactionLog(TransactionCategory.TAX, TransactionType.PAY, this.pluginName, value, from.getName(), uuidFrom, "CTS POOL:" + pool.getId(),  null, fromOldBalance, fromNewBalance, toOldBalance, toNewBalance, pool.getId(), TransactionLogType.BOTH, memo);
        return a;
    }

    public int transferMoneyPoolToPlayer(long poolId, UUID uuidTo, double value, TransactionCategory transactionCategory,TransactionType transactionType, String memo){
        OfflinePlayer to = Bukkit.getOfflinePlayer(uuidTo);
        MoneyPoolObject pool = new MoneyPoolObject(poolId);
        if(transactionCategory == null){
            transactionCategory = TransactionCategory.UNKNOWN;
        }
        if(transactionType == null){
            transactionType = TransactionType.UNKNOWN;
        }
        if(memo == null){
            memo = "";
        }
    }





}
