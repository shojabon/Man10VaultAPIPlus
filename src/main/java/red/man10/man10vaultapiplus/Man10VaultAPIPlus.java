package red.man10.man10vaultapiplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.man10vaultapiplus.utils.MySQLAPI;

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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static MySQLAPI mysql = null;
    public Man10VaultAPI vault = null;

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
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return true;
    }
}
