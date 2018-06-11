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
        int a = createTransactionLog(transactionCategory, transactionType, this.pluginName, value, from.getName(), uuidFrom, pool.getName() + pool.getId(),  null, fromOldBalance, fromNewBalance, toOldBalance, toNewBalance, pool.getId(), TransactionLogType.BOTH, memo);
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
        int a = createTransactionLog(TransactionCategory.TAX, TransactionType.PAY, this.pluginName, value, from.getName(), uuidFrom, pool.getName() + pool.getId(),  null, fromOldBalance, fromNewBalance, toOldBalance, toNewBalance, pool.getId(), TransactionLogType.BOTH, memo);
        return a;
    }

    public int takePlayerMoney(UUID uuidFrom, double value, String memo){
        OfflinePlayer from = Bukkit.getOfflinePlayer(uuidFrom);
        if(memo == null){
            memo = "";
        }
        if (from == null){
            return -1;
        }
        double fromOldBalance = vault.getBalance(from);
        boolean fromS = vault.silentWithdraw(from, value);
        if(!fromS){
            vault.silentDeposit(from, value);
            return -4;
        }
        double fromNewBalance = fromOldBalance - value;
        int a = createTransactionLog(TransactionCategory.VOID, TransactionType.PAY, this.pluginName, value, from.getName(), uuidFrom, "||VOID||", null, fromOldBalance, fromNewBalance, 0, 0, -1, TransactionLogType.BOTH, memo);
        return a;
    }

    public int givePlayerMoney(UUID uuidTo, double value, String memo){
        OfflinePlayer to = Bukkit.getOfflinePlayer(uuidTo);
        if(memo == null){
            memo = "";
        }
        if (to == null){
            return -1;
        }
        double toOldBalance = vault.getBalance(to);
        boolean toS = vault.silentWithdraw(to, value);
        if(!toS){
            vault.silentDeposit(to, value);
            return -4;
        }
        double toNewBalance = toOldBalance - value;
        int a = createTransactionLog(TransactionCategory.VOID, TransactionType.RECIVE, this.pluginName, value, "||VOID||", null, to.getName(), to.getUniqueId(), 0, 0, toOldBalance, toNewBalance, -1, TransactionLogType.BOTH, memo);
        return a;
    }

    public int transferMoneyPoolToPlayer(long fromPoolId, UUID uuidTo, double value, TransactionCategory transactionCategory,TransactionType transactionType, String memo){
        OfflinePlayer to = Bukkit.getOfflinePlayer(uuidTo);
        MoneyPoolObject pool = new MoneyPoolObject(fromPoolId);
        if(transactionCategory == null){
            transactionCategory = TransactionCategory.UNKNOWN;
        }
        if(transactionType == null){
            transactionType = TransactionType.UNKNOWN;
        }
        if(memo == null){
            memo = "";
        }
        if (to == null){
            return -2;
        }
        if(!pool.isAvailable()){
            return -1;
        }
        double fromOldBalance = pool.getBalance();
        double toOldBalance = vault.getBalance(to);
        if(fromOldBalance < value){
            return -3;
        }
        boolean toS = vault.silentDeposit(to, value);
        boolean fromS = mysql.execute("UPDATE man10_moneypool SET balance = balance -'" + value + "' WHERE id = '" + pool.getId() + "'");
        if(!fromS || !toS){
            if(toS){
                vault.silentWithdraw(to, value);
            }
            if(fromS){
                mysql.execute("UPDATE man10_moneypool SET balance = balance + '" + value + "' WHERE id = '" + pool.getId() + "'");
            }
            return -4;
        }
        double fromNewBalance = fromOldBalance - value;
        double toNewBalance = toOldBalance + value;
        int a = createTransactionLog(transactionCategory, transactionType, this.pluginName, value, pool.getName() + pool.getId(), null, to.getName() , to.getUniqueId(), fromOldBalance, fromNewBalance, toOldBalance, toNewBalance, pool.getId(), TransactionLogType.BOTH, memo);
        return a;
    }

    public int transferMoneyPoolToCountry(long fromPoolId, double value, String memo){
        MoneyPoolObject to = new MoneyPoolObject(1);
        MoneyPoolObject pool = new MoneyPoolObject(fromPoolId);
        if(memo == null){
            memo = "";
        }
        if (!to.isAvailable()){
            return -2;
        }
        if(!pool.isAvailable()){
            return -1;
        }
        double fromOldBalance = pool.getBalance();
        double toOldBalance = to.getBalance();
        if(fromOldBalance < value){
            return -3;
        }
        boolean toS = mysql.execute("UPDATE man10_moneypool SET balance = balance +'" + value + "' WHERE id = '" + to.getId() + "'");
        boolean fromS = mysql.execute("UPDATE man10_moneypool SET balance = balance -'" + value + "' WHERE id = '" + pool.getId() + "'");
        if(!fromS || !toS){
            if(toS){
                mysql.execute("UPDATE man10_moneypool SET balance = balance - '" + value + "' WHERE id = '" + to.getId() + "'");
            }
            if(fromS){
                mysql.execute("UPDATE man10_moneypool SET balance = balance + '" + value + "' WHERE id = '" + pool.getId() + "'");
            }
            return -4;
        }
        double fromNewBalance = fromOldBalance - value;
        double toNewBalance = toOldBalance + value;
        int a = createTransactionLog(TransactionCategory.TAX, TransactionType.PAY, this.pluginName, value, pool.getName() + pool.getId(), null, to.getName() + to.getId() , null, fromOldBalance, fromNewBalance, toOldBalance, toNewBalance, pool.getId(), TransactionLogType.BOTH, memo);
        return a;
    }

    public int transferMoneyPoolToPool(long fromPoolId, long toPoolId, double value, TransactionCategory transactionCategory,TransactionType transactionType, String memo){
        MoneyPoolObject to = new MoneyPoolObject(toPoolId);
        MoneyPoolObject pool = new MoneyPoolObject(fromPoolId);
        if(transactionCategory == null){
            transactionCategory = TransactionCategory.UNKNOWN;
        }
        if(transactionType == null){
            transactionType = TransactionType.UNKNOWN;
        }
        if(memo == null){
            memo = "";
        }
        if (!to.isAvailable()){
            return -2;
        }
        if(!pool.isAvailable()){
            return -1;
        }
        double fromOldBalance = pool.getBalance();
        double toOldBalance = to.getBalance();
        if(fromOldBalance < value){
            return -3;
        }
        boolean toS = mysql.execute("UPDATE man10_moneypool SET balance = balance +'" + value + "' WHERE id = '" + to.getId() + "'");
        boolean fromS = mysql.execute("UPDATE man10_moneypool SET balance = balance -'" + value + "' WHERE id = '" + pool.getId() + "'");
        if(!fromS || !toS){
            if(toS){
                mysql.execute("UPDATE man10_moneypool SET balance = balance - '" + value + "' WHERE id = '" + to.getId() + "'");
            }
            if(fromS){
                mysql.execute("UPDATE man10_moneypool SET balance = balance + '" + value + "' WHERE id = '" + pool.getId() + "'");
            }
            return -4;
        }
        double fromNewBalance = fromOldBalance - value;
        double toNewBalance = toOldBalance + value;
        int a = createTransactionLog(transactionCategory, transactionType, this.pluginName, value, pool.getName() + pool.getId(), null, to.getName() + to.getId() , null, fromOldBalance, fromNewBalance, toOldBalance, toNewBalance, pool.getId(), TransactionLogType.BOTH, memo);
        return a;
    }

    public int takeMoneyPoolMoney(long fromPoolId, double value, String memo){
        MoneyPoolObject from = new MoneyPoolObject(fromPoolId);
        if(memo == null){
            memo = "";
        }
        if (!from.isAvailable()){
            return -1;
        }
        double fromOldBalance = from.getBalance();
        boolean fromS = mysql.execute("UPDATE man10_moneypool SET balance = balance -'" + value + "' WHERE id = '" + from.getId() + "'");
        if(!fromS){
            mysql.execute("UPDATE man10_moneypool SET balance = balance + '" + value + "' WHERE id = '" + from.getId() + "'");
            return -4;
        }
        double fromNewBalance = fromOldBalance - value;
        int a = createTransactionLog(TransactionCategory.VOID, TransactionType.PAY, this.pluginName, value, from.getName() + from.getId(), null, "||VOID||", null, fromOldBalance, fromNewBalance, 0, 0, from.getId(), TransactionLogType.BOTH, memo);
        return a;
    }

    public int giveMoneyPoolMoney(long toPoolId, double value, String memo){
        MoneyPoolObject to = new MoneyPoolObject(toPoolId);
        if(memo == null){
            memo = "";
        }
        if (!to.isAvailable()){
            return -1;
        }
        double toOldBalance = to.getBalance();
        boolean toS = mysql.execute("UPDATE man10_moneypool SET balance = balance +'" + value + "' WHERE id = '" + to.getId() + "'");
        if(!toS){
            mysql.execute("UPDATE man10_moneypool SET balance = balance - '" + value + "' WHERE id = '" + to.getId() + "'");
            return -4;
        }
        double toNewBalance = toOldBalance - value;
        int a = createTransactionLog(TransactionCategory.VOID, TransactionType.PAY, this.pluginName, value, "||VOID||", null, to.getName() + to.getId(), null, 0,0, toOldBalance, toNewBalance, to.getId(), TransactionLogType.BOTH, memo);
        return a;
    }

    public int transferMoneyCountryToPlayer(UUID uuidTo, double value, String memo){
        return transferMoneyPoolToPlayer(1, uuidTo, value, TransactionCategory.TAX, TransactionType.RECIVE, memo);
    }

    public int transferMoneyCountryToPool(long toPoolId, double value, String memo){
        return transferMoneyPoolToPool(1, toPoolId, value, TransactionCategory.TAX, TransactionType.RECIVE, memo);
    }

    public int takeCountryMoney(double value, String memo){
        return takeMoneyPoolMoney(1, value, memo);
    }

    public int giveCountyMoney(double value, String memo){
        return giveMoneyPoolMoney(1, value, memo);
    }










}
