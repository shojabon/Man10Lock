package com.shojabon.man10lock.commands.subCommands

import com.shojabon.man10lock.Man10Lock
import com.shojabon.man10lock.Man10LockAPI
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.*
import java.util.function.Consumer

class LockBlockInfoCommand(private val plugin: Man10Lock) : CommandExecutor, Listener {


    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if(sender !is Player) return false;
        val p: Player = sender

        val b = p.getTargetBlock(10)
        if(b == null){
            p.sendMessage(Man10Lock.prefix + "§c§lブロックがありません")
            return false
        }

        val targetLockBlock = Man10Lock.api.getLockBlock(b.location)
        if(targetLockBlock == null){
            p.sendMessage(Man10Lock.prefix + "§c§lロックされたブロックではありません")
            return false
        }

        p.sendMessage("§e§l========[編集可能な人]========")
        for(user in targetLockBlock.permissionUsers.values){
            p.sendMessage("§d§l" + user.name)
        }
        p.sendMessage("§e§l==========================")

        return true
    }
}