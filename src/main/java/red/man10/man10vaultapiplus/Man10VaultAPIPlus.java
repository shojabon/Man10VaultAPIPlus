package red.man10.man10vaultapiplus;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import red.man10.man10vaultapiplus.enums.TransactionCategory;
import red.man10.man10vaultapiplus.enums.TransactionType;
import red.man10.man10vaultapiplus.utils.MySQLAPI;

import java.util.*;

public final class Man10VaultAPIPlus extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        mysql = new MySQLAPI(this, "Man10VaultAPIPlus");
        vault = new Man10VaultAPI("Man10VaultAPIPlus");
        original = new VaultAPI();
        mysql.execute(createMoneyPool);
        mysql.execute(createTransaction);

        new BukkitRunnable(){

            @Override
            public void run() {
                Runnable r = () -> {
                    Set<Long> keys = MoneyPoolManager.changesMade.keySet();
                    for(Long l : keys){
                        mysql.executeThread("UPDATE man10_moneypool SET balance ='" + manager.get(l).balance + "' WHERE id ='" + manager.get(l).id + "'");
                        MoneyPoolManager.poolObjects.remove(l);
                    }
                };
                Thread t = new Thread(r);
                t.start();
            }
        }.runTaskTimer(this, 1200, 1200);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static MySQLAPI mysql = null;
    public Man10VaultAPI vault = null;
    public static MoneyPoolManager manager = new MoneyPoolManager();

    VaultAPI original = null;

    String createMoneyPool = "CREATE TABLE `man10_moneypool` (\n" +
            "\t`id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
            "\t`plugin` VARCHAR(256) NULL DEFAULT NULL,\n" +
            "\t`plugin_id` INT(11) NULL DEFAULT '0',\n" +
            "\t`balance` DOUBLE NULL DEFAULT '0',\n" +
            "\t`pool_term` VARCHAR(128) NULL DEFAULT NULL,\n" +
            "\t`type` VARCHAR(128) NULL DEFAULT NULL,\n" +
            "\t`wired` TINYINT(4) NULL DEFAULT '0',\n" +
            "\t`wired_uuid` VARCHAR(64) NULL DEFAULT NULL,\n" +
            "\t`wired_name` VARCHAR(64) NULL DEFAULT NULL,\n" +
            "\t`memo` TEXT NULL,\n" +
            "\t`frozen` TINYINT(4) NULL DEFAULT NULL,\n" +
            "\t`time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
            "\tPRIMARY KEY (`id`)\n" +
            ")\n" +
            "COLLATE='utf8_general_ci'\n" +
            "ENGINE=InnoDB\n" +
            ";\n";

    String createTransaction = "CREATE TABLE `man10_transaction` (\n" +
            "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
            "\t`category` VARCHAR(256) NOT NULL DEFAULT '0',\n" +
            "\t`type` VARCHAR(256) NOT NULL DEFAULT '0',\n" +
            "\t`plugin` VARCHAR(2048) NOT NULL DEFAULT '0',\n" +
            "\t`balance` DOUBLE NOT NULL DEFAULT '0',\n" +
            "\t`from_name` VARCHAR(64) NOT NULL DEFAULT '0',\n" +
            "\t`to_name` VARCHAR(64) NOT NULL DEFAULT '0',\n" +
            "\t`from_uuid` VARCHAR(64) NOT NULL DEFAULT '0',\n" +
            "\t`to_uuid` VARCHAR(64) NOT NULL DEFAULT '0',\n" +
            "\t`from_old_balance` DOUBLE NOT NULL DEFAULT '0',\n" +
            "\t`to_old_balance` DOUBLE NOT NULL DEFAULT '0',\n" +
            "\t`from_new_balance` DOUBLE NOT NULL DEFAULT '0',\n" +
            "\t`to_new_balance` DOUBLE NOT NULL DEFAULT '0',\n" +
            "\t`pool_id` INT(11) NOT NULL DEFAULT '0',\n" +
            "\t`log_type` VARCHAR(128) NOT NULL DEFAULT '0',\n" +
            "\t`memo` VARCHAR(2048) NOT NULL DEFAULT '0',\n" +
            "\t`time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
            "\tPRIMARY KEY (`id`)\n" +
            ")\n" +
            "COLLATE='utf8_general_ci'\n" +
            "ENGINE=InnoDB\n" +
            ";\n";

    HashMap<UUID, Double>  payComfirm = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("pay")){
            if(sender instanceof Player){
                Player p = (Player) sender;
                if(args.length == 2){
                    try{
                        double value = Double.valueOf(args[1]);
                        Player to = Bukkit.getPlayer(args[0]);
                        if(value < 1){
                            p.sendMessage("§40円以下の支払いはできません/ You cannot create a transaction below 0 $");
                            return false;
                        }
                        if(to == null || !to.isOnline() || !to.getName().equalsIgnoreCase(args[0])){
                            p.sendMessage("§4プレイヤーがオンラインではありません  / Player is not online");
                            return false;
                        }
                        if(to.getUniqueId() == p.getUniqueId()){
                            p.sendMessage("§4自分自身には送信できません");
                            p.sendMessage("§4You cannot send money to yourself");
                            return false;
                        }
                        if(!payComfirm.containsKey(p.getUniqueId())){
                            p.sendMessage("§7§l支払いを認証するためにはもう一度" + command.getName() + "と打ってください");
                            p.sendMessage("§7§lType" + command.getName() + " to confirm your transaction");
                            payComfirm.put(p.getUniqueId(), value);
                            return false;
                        }
                        if(payComfirm.get(p.getUniqueId()) == value){
                            vault.transferMoneyPlayerToPlayer(p.getUniqueId(), to.getUniqueId(), value, TransactionCategory.GENERAL, TransactionType.PAY, "Pay " + p.getName() + " =>" + to.getName() + " value:" + value);
                            to.sendMessage("§a" + p.getName() + "さんから" + String.format("%,d", new Double(value).longValue())   + "円送金されました");
                            to.sendMessage("§a" + String.format("%,d", new Double(value).longValue())  + "$ has been sent from " + p.getName());

                            p.sendMessage("§a" + to.getName() + "さんに" + String.format("%,d", new Double(value).longValue())   + "円送金されました");
                            p.sendMessage("§a" + String.format("%,d", new Double(value).longValue())  + "$ has been sent to " + to.getName());
                            payComfirm.remove(p.getUniqueId());
                            return false;
                        }
                        if(payComfirm.get(p.getUniqueId()) != value){
                            p.sendMessage("§7§l支払いを認証するためにはもう一度" + command.getName() + "と打ってください");
                            p.sendMessage("§7§lType" + command.getName() + " to confirm your transaction");
                            payComfirm.put(p.getUniqueId(), value);
                            return false;
                        }
                    }catch (NumberFormatException e){
                        p.sendMessage("§c/pay <プレイヤー名/player name> <金額/value>");
                    }
                }else{
                    p.sendMessage("§c/pay <プレイヤー名/player name> <金額/value>");
                }
            }
        }
        return true;
    }
}
