package com.shojabon.man10lock.dataClass

import com.shojabon.man10lock.enums.Man10LockPermission
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*
import kotlin.collections.HashMap

class Man10LockedBlock (val blockId: String){

    val permissionUsers = HashMap<UUID, Man10LockUser>();

    fun addUser(user: Man10LockUser){
        permissionUsers[user.uuid] = user;
    }

    fun userCanEdit(uuid: UUID): Boolean{
        return permissionUsers.containsKey(uuid)
    }

    fun userIsOwner(uuid: UUID): Boolean{
        if(!permissionUsers.containsKey(uuid)) return false
        return permissionUsers[uuid]?.permission == Man10LockPermission.OWNER
    }

    fun removeUser(uuid: UUID){
        permissionUsers.remove(uuid)
    }

    fun getLocation(): Location? {
        val args = blockId.split("|")
        val world = Bukkit.getWorld(args[0])?: return null
        return Location(world,
            Integer.valueOf(args[1]).toDouble(), Integer.valueOf(args[2]).toDouble(), Integer.valueOf(args[3]).toDouble())
    }


}