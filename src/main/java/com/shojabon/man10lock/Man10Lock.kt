package com.shojabon.man10lock

import com.shojabon.man10lock.commands.Man10LockCommandRouter
import com.shojabon.man10lock.listeners.Man10LockListeners
import com.shojabon.mcutils.Utils.MySQL.ThreadedMySQLAPI
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class Man10Lock : JavaPlugin() {

    companion object{

        lateinit var mysql : ThreadedMySQLAPI
        lateinit var config : FileConfiguration
        lateinit var api: Man10LockAPI
        var prefix : String? = "§6[§6Man10Lock§6]"
        var serverName: String? = ""

        var loadingChests = false

    }

    override fun onEnable() {
        // Plugin startup logic

        saveDefaultConfig()
        Man10Lock.config = config
        mysql = ThreadedMySQLAPI(this)
        createTables()
        prefix = config.getString("prefix")
        serverName = config.getString("server")

        api = Man10LockAPI()

        server.pluginManager.registerEvents(Man10LockListeners(this), this)
        val commandRouter =  Man10LockCommandRouter(this)
        commandRouter.pluginPrefix = prefix
        getCommand("mlock")?.setExecutor(commandRouter)
        getCommand("mlock")?.tabCompleter = commandRouter
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun createTables(){
        mysql.execute("CREATE TABLE IF NOT EXISTS `man10lock_blocks` (\n" +
                "\t`id` INT(10) NOT NULL AUTO_INCREMENT,\n" +
                "\t`server` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`block_id` VARCHAR(512) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`user_block_id` VARCHAR(512) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`block_type` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`name` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`uuid` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`permission` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',\n" +
                "\t`date_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "\tPRIMARY KEY (`id`) USING BTREE,\n" +
                "\tUNIQUE INDEX `unique` (`user_block_id`) USING BTREE\n" +
                ")\n" +
                "COLLATE='utf8mb4_0900_ai_ci'\n" +
                "ENGINE=InnoDB\n" +
                ";\n")
    }
}