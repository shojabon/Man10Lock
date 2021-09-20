package com.shojabon.man10lock.commands.subCommands

import com.shojabon.man10lock.Man10Lock
import com.shojabon.man10lock.Man10LockAPI
import com.shojabon.man10lock.enums.Man10LockPermission
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.*
import java.util.function.Consumer

class AddUserToLockBlockCommand(private val plugin: Man10Lock) : CommandExecutor, Listener {


    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if(sender !is Player) return false
        val p: Player = sender

        val targetPlayer = Bukkit.getOfflinePlayerIfCached(args[1])
        if(targetPlayer == null){
            p.sendMessage(Man10Lock.prefix + "§c§lプレイヤーが存在しません")
            return false
        }

        if(targetPlayer.name == null){
            p.sendMessage(Man10Lock.prefix + "§c§lプレイヤーが存在しません")
            return false
        }

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

        if(targetPlayer.uniqueId == p.uniqueId){
            p.sendMessage(Man10Lock.prefix + "§c§l自分を追加することはできません")
            return false
        }

        if(targetLockBlock.userCanEdit(targetPlayer.uniqueId)){
            p.sendMessage(Man10Lock.prefix + "§c§lプレイヤーはすでにブロックを編集することができます")
            return false
        }


        Man10Lock.api.lockBlock(b.location, targetPlayer.name!!, targetPlayer.uniqueId, Man10LockPermission.MEMBER, Consumer {
            if(!it){
                p.sendMessage(Man10Lock.prefix + "§c§l内部エラーが発生しました")
                return@Consumer
            }

            p.sendMessage(Man10Lock.prefix + "§a§lユーザーを追加しました")
        })

        return true
    }
}