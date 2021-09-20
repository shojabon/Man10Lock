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

class ListLockedBlockInfoCommand(private val plugin: Man10Lock) : CommandExecutor, Listener {


    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if(sender !is Player) return false;
        val p: Player = sender


        val blockIds = Man10LockAPI.ownerLockBlock[p.uniqueId]
        if (blockIds == null || blockIds.size == 0) {
            p.sendMessage(Man10Lock.prefix + "§c§lロックされたブロックがありません")
            return false
        }
        p.sendMessage("§e§l========[ロックされたブロック一覧]========")
        for(blockId in blockIds){
            val lockBlock = Man10Lock.api.getLockBlock(blockId)?: continue
            val location = lockBlock.getLocation()?: continue
            if(!lockBlock.userIsOwner(p.uniqueId)) continue


            p.sendMessage("§d§l" + location.world.name + " " + location.blockX + " " + location.blockY + " " + location.blockZ)
        }
        p.sendMessage("§e§l==================================")

        return true
    }
}