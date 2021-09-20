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

        var uuid = p.uniqueId

        if(args.size == 2){
            val targetPlayer = Bukkit.getOfflinePlayerIfCached(args[1])
            if(targetPlayer == null || !targetPlayer.name?.contentEquals(args[1])!!){
                p.sendMessage(Man10Lock.prefix + "§c§lプレイヤーが存在しません")
                return false
            }
            uuid = targetPlayer.uniqueId
        }

        val blockIds = Man10LockAPI.ownerLockBlock[uuid]
        if (blockIds == null || blockIds.size == 0) {
            p.sendMessage(Man10Lock.prefix + "§c§lロックされたブロックがありません")
            return false
        }

        val refinedBlockIds = ArrayList<String>()

        for(blockId in blockIds){
            val lockBlock = Man10Lock.api.getLockBlock(blockId)?: continue
            lockBlock.getLocation()?: continue
            if(!lockBlock.userIsOwner(uuid)) continue

            refinedBlockIds.add(blockId)
        }

        if(refinedBlockIds.size == 0){
            p.sendMessage(Man10Lock.prefix + "§c§lロックされたブロックがありません")
            return false
        }
        if(args.size == 2) p.sendMessage("§e§l" + uuid + "の情報")
        p.sendMessage("§e§l========[ロックされたブロック一覧]========")
        for(blockId in refinedBlockIds){
            val lockBlock = Man10Lock.api.getLockBlock(blockId)?: continue
            val location = lockBlock.getLocation()?: continue


            p.sendMessage("§d§l" + location.world.name + " " + location.blockX + " " + location.blockY + " " + location.blockZ)
        }
        p.sendMessage("§e§l==================================")

        return true
    }
}