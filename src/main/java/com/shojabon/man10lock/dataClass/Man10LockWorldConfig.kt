package com.shojabon.man10lock.dataClass

import org.bukkit.Material
import org.bukkit.block.Block

class Man10LockWorldConfig (val worldName: String, val allowedBlocks: ArrayList<Material>, var maxAllowedLocks: Int){

    fun blockIsLockable(b: Block): Boolean {
        return allowedBlocks.contains(b.type)
    }

    fun blockIsLockable(type: String): Boolean {
        for(material in allowedBlocks){
            if(material.name == type) return true
        }
        return false
    }
}