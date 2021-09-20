package com.shojabon.man10lock.dataClass

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

    fun removeUser(uuid: UUID){
        permissionUsers.remove(uuid)
    }


}