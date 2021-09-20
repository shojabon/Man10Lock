package com.shojabon.man10lock.listeners

import com.shojabon.man10lock.Man10Lock
import com.shojabon.man10lock.Man10LockAPI
import com.shojabon.man10lock.Utils.SItemStack
import com.shojabon.man10lock.commands.subCommands.LockBlockCommand
import com.shojabon.man10lock.enums.Man10LockPermission
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.block.data.type.Door
import org.bukkit.event.*
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
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
        val worldConfiguration = Man10LockAPI.worldConfigurations[e.clickedBlock!!.world.name] ?: return
        val availableBlock = worldConfiguration.blockIsLockable(e.clickedBlock!!)
        if(!availableBlock)return


        val targetLockBlock = Man10Lock.api.getLockBlock(e.clickedBlock!!.location) ?: return
        if(targetLockBlock.userCanEdit(e.player.uniqueId) || e.player.hasPermission("man10lock.admin")){

            //admin message
            if(e.player.hasPermission("man10lock.admin") && !targetLockBlock.userCanEdit(e.player.uniqueId)){
                e.player.sendMessage(Man10Lock.prefix + "§c§l権限行使中")
            }
            return
        }

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

        val worldConfiguration = Man10LockAPI.worldConfigurations[e.block.world.name] ?: return
        val availableBlock = worldConfiguration.blockIsLockable(e.block)
        if(!availableBlock)return

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

    @EventHandler
    fun onNaturalBreak(e: BlockPhysicsEvent){
        if(e.sourceBlock.type != Material.AIR) return
        if(!isInstanceOfBottomSupported(e.block)) return

        if(e.isCancelled) return
        //boot loading
        if(Man10Lock.loadingChests){
            e.isCancelled = true
            return
        }

        val worldConfiguration = Man10LockAPI.worldConfigurations[e.block.world.name] ?: return
        val availableBlock = worldConfiguration.blockIsLockable(e.block)
        if(!availableBlock)return
        val targetLockBlock = Man10Lock.api.getLockBlock(e.block.location) ?: return
        Man10Lock.api.deleteLockBlock(e.block.location, Consumer {})
    }

    fun isInstanceOfBottomSupported(b: Block): Boolean {
        if(b.blockData is Door) return true
        if(b.blockData is Sign) return true

        return false
    }

    @EventHandler
    fun onPlace(e: BlockPlaceEvent){
        if(e.isCancelled) return
        if(!e.canBuild()) return

        val itemInHand = SItemStack(e.itemInHand)
        if(itemInHand.lore.isEmpty() || !itemInHand.lore[0]!!.contentEquals("Man10Lock")){
            return
        }

        val worldConfig = Man10LockAPI.worldConfigurations[e.block.location.world.name]
        if(worldConfig == null){
            e.isCancelled = true
            e.player.sendMessage(Man10Lock.prefix + "§c§lこのワールドではロックできません")
            return
        }

        if(!worldConfig.blockIsLockable(e.block)) {
            e.isCancelled = true
            e.player.sendMessage(Man10Lock.prefix + "§c§lこのブロックはロックできません")
            return
        }


        if(Man10Lock.api.getLockedBlockCountInWorld(e.player.uniqueId, e.player.world.name) >=  worldConfig.maxAllowedLocks && worldConfig.maxAllowedLocks != -1 && !e.player.hasPermission("man10lock.admin")){
            e.player.sendMessage(Man10Lock.prefix + "§c§lこのワールドでは最大" + worldConfig.maxAllowedLocks + "個しかロックできません")
            e.isCancelled = true
            return
        }


        Man10Lock.api.lockBlock(e.block.location, e.player.name, e.player.uniqueId, Man10LockPermission.OWNER, Consumer {
            if(!it){
                e.player.sendMessage(Man10Lock.prefix + "§c§l内部エラーが発生しました")
                return@Consumer
            }
            e.player.sendMessage(Man10Lock.prefix + "§a§lブロックをロックしました")
        })
    }

}