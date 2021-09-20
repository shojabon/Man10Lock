package com.shojabon.man10lock.listeners

import com.shojabon.man10lock.Man10Lock
import com.shojabon.man10lock.Man10LockAPI
import org.bukkit.Bukkit
import org.bukkit.event.*
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.InventoryHolder
import java.util.function.Consumer

class Man10LockListeners(val plugin: Man10Lock) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onInteract(e: PlayerInteractEvent){
        if(e.useInteractedBlock() == Event.Result.DENY) return
        if(e.clickedBlock == null) return
        //boot loading
        if(Man10Lock.loadingChests){
            e.setUseInteractedBlock(Event.Result.DENY)
            e.player.sendMessage(Man10Lock.prefix + "§c§lデータをロード中です")
            return
        }

        val availableBlock = Man10LockAPI.worldConfigurations[e.clickedBlock!!.world.name]?.blockIsLockable(e.clickedBlock!!)?: return
        if(!availableBlock)return


        val targetLockBlock = Man10Lock.api.getLockBlock(e.clickedBlock!!.location) ?: return
        if(targetLockBlock.userCanEdit(e.player.uniqueId) || e.player.hasPermission("man10lock.admin")) return

        e.setUseInteractedBlock(Event.Result.DENY)
        e.player.sendMessage(Man10Lock.prefix + "§c§lこのブロックはロックされています")

    }

    @EventHandler
    fun onBreak(e: BlockBreakEvent){
        if(e.isCancelled) return
        //boot loading
        if(Man10Lock.loadingChests){
            e.isCancelled = true
            e.player.sendMessage(Man10Lock.prefix + "§c§lデータをロード中です")
            return
        }
        val targetLockBlock = Man10Lock.api.getLockBlock(e.block.location) ?: return
        if(!targetLockBlock.userIsOwner(e.player.uniqueId) && !e.player.hasPermission("man10lock.admin")) {
            e.isCancelled = true
            e.player.sendMessage(Man10Lock.prefix + "§c§lこのブロックはロックされています")
            return
        }
        Man10Lock.api.deleteLockBlock(e.block.location, Consumer {
            if(!it){
                e.player.sendMessage(Man10Lock.prefix + "§a§l内部エラーが発生しました")
                return@Consumer
            }
            e.player.sendMessage(Man10Lock.prefix + "§c§lロックが解除されました")

        })


    }
}