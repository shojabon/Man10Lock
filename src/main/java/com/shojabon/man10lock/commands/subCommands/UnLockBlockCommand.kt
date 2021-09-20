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

class UnLockBlockCommand(private val plugin: Man10Lock) : CommandExecutor, Listener {


    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if(sender !is Player) return false;
        val p: Player = sender


        if(!Man10LockAPI.worldConfigurations.containsKey(p.world.name)){
            p.sendMessage(Man10Lock.prefix + "§c§lこのワールドではロックできません")
            return false
        }

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

        if(!targetLockBlock.userIsOwner(p.uniqueId) && !p.hasPermission("man10lock.admin")){
            p.sendMessage(Man10Lock.prefix + "§c§lあなたはこのブロックを編集することはできません")
            return false
        }



        Man10Lock.api.deleteLockBlock(b.location, Consumer {
            if(!it){
                p.sendMessage(Man10Lock.prefix + "§c§l内部エラーが発生しました")
                return@Consumer
            }

            p.sendMessage(Man10Lock.prefix + "§a§lロックを解除しました")
        })

        return true
    }
}