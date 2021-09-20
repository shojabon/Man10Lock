package com.shojabon.man10lock.listeners

import com.shojabon.man10lock.Man10Lock
import com.shojabon.man10lock.commands.subCommands.LockBlockCommand
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class Man10LockListeners(val plugin: Man10Lock) : Listener {

    @EventHandler
    fun onInteract(e: PlayerInteractEvent){
        if(e.useInteractedBlock() == Event.Result.DENY) return
        if(e.clickedBlock == null) return
        val targetLockBlock = Man10Lock.api.getLockBlock(e.clickedBlock!!.location) ?: return
        if(targetLockBlock.userCanEdit(e.player.uniqueId)) return

        e.setUseInteractedBlock(Event.Result.DENY)
        e.player.sendMessage(Man10Lock.prefix + "§c§lこのブロックはロックされています")

    }
}