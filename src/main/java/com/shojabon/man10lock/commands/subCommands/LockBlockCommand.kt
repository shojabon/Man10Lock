package com.shojabon.man10lock.commands.subCommands

import com.shojabon.man10lock.Man10Lock
import com.shojabon.man10lock.Man10LockAPI
import com.shojabon.man10lock.dataClass.Man10LockWorldConfig
import com.shojabon.man10lock.enums.Man10LockPermission
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList

class LockBlockCommand(private val plugin: Man10Lock) : CommandExecutor, Listener {

    companion object{
        val inLockBlockState = ArrayList<UUID>()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if(sender !is Player) return false;
        val p: Player = sender
        if(inLockBlockState.contains(p.uniqueId)){
            p.sendMessage(Man10Lock.prefix + "§c§lロックしたいブロックを壊してください")
            return false
        }
        val worldConfig = Man10LockAPI.worldConfigurations[p.world.name]
        if(worldConfig == null){
            p.sendMessage(Man10Lock.prefix + "§c§lこのワールドではロックできません")
            return false
        }

        if(Man10Lock.api.getLockedBlockCountInWorld(p.uniqueId, p.world.name) >=  worldConfig.maxAllowedLocks && worldConfig.maxAllowedLocks != -1 && !p.hasPermission("man10lock.admin")){
            inLockBlockState.remove(p.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            p.sendMessage(Man10Lock.prefix + "§c§lこのワールドでは最大" + worldConfig.maxAllowedLocks + "個しかロックできません")
            return false
        }
        //register listeners if fist person
        if(inLockBlockState.size == 0)Bukkit.getPluginManager().registerEvents(this, plugin)


        inLockBlockState.add(p.uniqueId)
        p.sendMessage(Man10Lock.prefix + "§c§lロックしたいブロックを壊してください")
        return true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onLockBlock(e: BlockBreakEvent){
        if(e.isCancelled) return
        if(!inLockBlockState.contains(e.player.uniqueId)) return
        if(e.block.type == Material.AIR) return
        e.isCancelled = true

        val worldConfig = Man10LockAPI.worldConfigurations[e.block.location.world.name]
        if(worldConfig == null){
            inLockBlockState.remove(e.player.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            e.player.sendMessage(Man10Lock.prefix + "§c§lこのワールドではロックできません")
            return
        }

        if(!worldConfig.blockIsLockable(e.block)) {
            inLockBlockState.remove(e.player.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            e.player.sendMessage(Man10Lock.prefix + "§c§lこのブロックはロックできません")
            return
        }


        val targetLockBlock = Man10Lock.api.getLockBlock(e.block.location)
        if(targetLockBlock != null){
            inLockBlockState.remove(e.player.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            e.player.sendMessage(Man10Lock.prefix + "§c§lこのブロックはすでに保護されています")
            return
        }

        if(Man10Lock.api.getLockedBlockCountInWorld(e.player.uniqueId, e.player.world.name) >=  worldConfig.maxAllowedLocks && worldConfig.maxAllowedLocks != -1 && !e.player.hasPermission("man10lock.admin")){
            inLockBlockState.remove(e.player.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            e.player.sendMessage(Man10Lock.prefix + "§c§lこのワールドでは最大" + worldConfig.maxAllowedLocks + "個しかロックできません")
            return
        }


        inLockBlockState.remove(e.player.uniqueId)
        Man10Lock.api.lockBlock(e.block.location, e.player.name, e.player.uniqueId, Man10LockPermission.OWNER, Consumer {
            if(!it){
                e.player.sendMessage(Man10Lock.prefix + "§c§l内部エラーが発生しました")
                return@Consumer
            }

            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            e.player.sendMessage(Man10Lock.prefix + "§a§lブロックをロックしました")
        })
    }

    @EventHandler
    fun onItemFrameDestroy(e: HangingBreakByEntityEvent){
        if(e.isCancelled) return
        if(e.remover == null) return

        val rem = e.remover?: return

        if(!inLockBlockState.contains(rem.uniqueId)) return
        e.isCancelled = true

        val worldConfig = Man10LockAPI.worldConfigurations[rem.location.world.name]
        if(worldConfig == null){
            inLockBlockState.remove(rem.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            rem.sendMessage(Man10Lock.prefix + "§c§lこのワールドではロックできません")
            return
        }

        if(!worldConfig.blockIsLockable(e.entity.type.name)) {
            inLockBlockState.remove(rem.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            rem.sendMessage(Man10Lock.prefix + "§c§lこのブロックはロックできません")
            return
        }


        val targetLockBlock = Man10Lock.api.getLockBlock(e.entity.location)
        if(targetLockBlock != null){
            inLockBlockState.remove(rem.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            rem.sendMessage(Man10Lock.prefix + "§c§lこのブロックはすでに保護されています")
            return
        }

        if(Man10Lock.api.getLockedBlockCountInWorld(rem.uniqueId, rem.world.name) >=  worldConfig.maxAllowedLocks && worldConfig.maxAllowedLocks != -1 && !rem.hasPermission("man10lock.admin")){
            inLockBlockState.remove(rem.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            rem.sendMessage(Man10Lock.prefix + "§c§lこのワールドでは最大" + worldConfig.maxAllowedLocks + "個しかロックできません")
            return
        }


        inLockBlockState.remove(rem.uniqueId)
        Man10Lock.api.lockBlock(e.entity.location, rem.name, rem.uniqueId, Man10LockPermission.OWNER, Consumer {
            if(!it){
                rem.sendMessage(Man10Lock.prefix + "§c§l内部エラーが発生しました")
                return@Consumer
            }

            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            rem.sendMessage(Man10Lock.prefix + "§a§lブロックをロックしました")
        })
    }

    @EventHandler
    fun onItemFrameHasItemAndDestroy(e: EntityDamageByEntityEvent){
        if(e.isCancelled) return

        val rem = e.damager?: return

        if(!inLockBlockState.contains(rem.uniqueId)) return
        e.isCancelled = true

        val worldConfig = Man10LockAPI.worldConfigurations[rem.location.world.name]
        if(worldConfig == null){
            inLockBlockState.remove(rem.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            rem.sendMessage(Man10Lock.prefix + "§c§lこのワールドではロックできません")
            return
        }

        if(!worldConfig.blockIsLockable(e.entity.type.name)) {
            inLockBlockState.remove(rem.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            rem.sendMessage(Man10Lock.prefix + "§c§lこのブロックはロックできません")
            return
        }


        val targetLockBlock = Man10Lock.api.getLockBlock(e.entity.location)
        if(targetLockBlock != null){
            inLockBlockState.remove(rem.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            rem.sendMessage(Man10Lock.prefix + "§c§lこのブロックはすでに保護されています")
            return
        }

        if(Man10Lock.api.getLockedBlockCountInWorld(rem.uniqueId, rem.world.name) >=  worldConfig.maxAllowedLocks && worldConfig.maxAllowedLocks != -1 && !rem.hasPermission("man10lock.admin")){
            inLockBlockState.remove(rem.uniqueId)
            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            rem.sendMessage(Man10Lock.prefix + "§c§lこのワールドでは最大" + worldConfig.maxAllowedLocks + "個しかロックできません")
            return
        }


        inLockBlockState.remove(rem.uniqueId)
        Man10Lock.api.lockBlock(e.entity.location, rem.name, rem.uniqueId, Man10LockPermission.OWNER, Consumer {
            if(!it){
                rem.sendMessage(Man10Lock.prefix + "§c§l内部エラーが発生しました")
                return@Consumer
            }

            if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
            rem.sendMessage(Man10Lock.prefix + "§a§lブロックをロックしました")
        })
    }

}