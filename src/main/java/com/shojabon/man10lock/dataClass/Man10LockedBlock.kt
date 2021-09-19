package com.shojabon.man10lock.dataClass

import java.util.*
import kotlin.collections.HashMap

class Man10LockedBlock (val blockId: String){

    companion object{
        val permissionUsers = HashMap<UUID, Man10LockUser>();
    }

    fun addUser(user: Man10LockUser){
        permissionUsers[user.uuid] = user;
    }

}