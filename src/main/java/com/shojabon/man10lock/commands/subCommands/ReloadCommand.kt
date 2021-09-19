package com.shojabon.man10lock.commands.subCommands

import com.shojabon.man10lock.Man10Lock
import com.shojabon.man10lock.Utils.MySQL.ThreadedMySQLAPI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadCommand(private val plugin: Man10Lock) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        plugin.reloadConfig()
        Man10Lock.config = plugin.config
        Man10Lock.mysql = ThreadedMySQLAPI(plugin)
        Man10Lock.prefix = Man10Lock.config.getString("prefix")
        sender.sendMessage(Man10Lock.prefix.toString() + "§a§lプラグインがリロードされました")
        return true
    }
}