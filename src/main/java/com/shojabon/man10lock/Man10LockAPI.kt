package com.shojabon.man10lock

import com.shojabon.man10lock.dataClass.Man10LockWorldConfig
import com.shojabon.man10lock.dataClass.Man10LockedBlock
import it.unimi.dsi.fastutil.Hash
import org.bukkit.Material
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Man10LockAPI {

    companion object{
        val worldConfigurations = HashMap<String, Man10LockWorldConfig>()
        val lockedBlockData = HashMap<String, Man10LockedBlock>()
        val lockedBlockOwnerData = HashMap<String, UUID>();
    }

    init {
        loadConfig()
    }

    private fun loadConfig(){
        val keyList = Man10Lock.config.getConfigurationSection("worlds")?.getKeys(false) ?: return
        for(key in keyList){
            val allowedBlocks = Man10Lock.config.getStringList("worlds.$key.lockableBlockTypes")
            val allowedMaterials = ArrayList<Material>();

            for(materialName in allowedBlocks){
                try{
                    allowedMaterials.add(Material.valueOf(materialName));
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }

            var maxAllowedLocks = Man10Lock.config.getInt("worlds.$key.maxLocksPerPlayer")
            if(maxAllowedLocks <= 0) maxAllowedLocks = -1

            val worldEnabled = Man10Lock.config.getBoolean("worlds.$key.enabled")

            worldConfigurations[key] = Man10LockWorldConfig(key, allowedMaterials, maxAllowedLocks, worldEnabled)
        }
    }

}