package com.shojabon.man10lock

import com.shojabon.man10lock.Utils.MySQL.ThreadedMySQLAPI
import com.shojabon.man10lock.commands.Man10LockCommandRouter
import com.shojabon.man10lock.listeners.Man10LockListeners
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class Man10Lock : JavaPlugin() {

    companion object{

        lateinit var mysql : ThreadedMySQLAPI
        lateinit var config : FileConfiguration
        lateinit var api: Man10LockAPI
        var prefix : String? = "§6[§yMan10Lock§6]"
        var serverName: String? = ""

        var loadingChests = false

    }

    override fun onEnable() {
        // Plugin startup logic

        saveDefaultConfig()
        Man10Lock.config = config
        mysql = ThreadedMySQLAPI(this)
        prefix = config.getString("prefix")
        serverName = config.getString("server")

        api = Man10LockAPI()

        server.pluginManager.registerEvents(Man10LockListeners(this), this)
        val commandRouter =  Man10LockCommandRouter(this)
        getCommand("mlock")?.setExecutor(commandRouter)
        getCommand("mlock")?.tabCompleter = commandRouter
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}