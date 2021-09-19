package com.shojabon.man10lock.commands.subCommands

import com.shojabon.man10lock.Man10Lock
import com.shojabon.man10lock.Utils.MySQL.ThreadedMySQLAPI
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import java.util.*
import kotlin.collections.ArrayList

class LockBlockCommand(private val plugin: Man10Lock) : CommandExecutor, Listener {

    companion object{
        val inLockBlockState = ArrayList<UUID>()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if(sender !is Player) return false;
        val p: Player = sender
        if(inLockBlockState.contains(p.uniqueId)){
            p.sendMessage(Man10Lock.prefix + "§c§lロックしたいブロックを右クリックしてください")
            return false
        }
        //register listeners if fist person
        if(inLockBlockState.size == 0)Bukkit.getPluginManager().registerEvents(this, plugin)

        inLockBlockState.add(p.uniqueId)
        p.sendMessage(Man10Lock.prefix + "§c§lロックしたいブロックを右クリックしてください")
        return true
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onLockBlock(e: PlayerInteractEvent){
        if(e.action != Action.RIGHT_CLICK_BLOCK) return
        if(e.useInteractedBlock() == Event.Result.DENY) return
        if(!inLockBlockState.contains(e.player.uniqueId)) return
        Bukkit.broadcastMessage("clicked!")



        inLockBlockState.remove(e.player.uniqueId)
        if(inLockBlockState.size == 0) HandlerList.unregisterAll(this)
    }
}