package com.shojabon.man10lock.dataClass

import org.bukkit.Material
import org.bukkit.block.Block

class Man10LockWorldConfig (val worldName: String, val allowedBlocks: ArrayList<Material>, var maxAllowedLocks: Int, var enabled: Boolean){

    fun blockIsLockable(b: Block): Boolean {
        return allowedBlocks.contains(b.type)
    }
}