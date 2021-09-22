package com.shojabon.man10lock.commands.subCommands

import com.shojabon.man10lock.Man10Lock
import com.shojabon.man10lock.Man10Lock.Companion.config
import com.shojabon.man10lock.Man10LockAPI
import com.shojabon.mcutils.Utils.MySQL.ThreadedMySQLAPI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadCommand(private val plugin: Man10Lock) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        plugin.reloadConfig()
        config = plugin.config
        Man10Lock.mysql = ThreadedMySQLAPI(plugin)
        Man10Lock.prefix = config.getString("prefix")
        Man10Lock.serverName = config.getString("server")

        Man10LockAPI.worldConfigurations.clear()
        Man10LockAPI.ownerLockCount.clear()
        Man10LockAPI.ownerLockBlock.clear()
        Man10LockAPI.lockedBlockData.clear()

        Man10Lock.api.loadConfig()
        Man10Lock.api.loadAllLockedBlocks()

        sender.sendMessage(Man10Lock.prefix.toString() + "§a§lプラグインがリロードされました")
        return true
    }
}