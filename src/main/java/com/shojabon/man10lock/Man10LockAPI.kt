package com.shojabon.man10lock

import com.shojabon.man10lock.Utils.MySQL.MySQLAPI
import com.shojabon.man10lock.dataClass.Man10LockUser
import com.shojabon.man10lock.dataClass.Man10LockWorldConfig
import com.shojabon.man10lock.dataClass.Man10LockedBlock
import com.shojabon.man10lock.enums.Man10LockPermission
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Consumer

class Man10LockAPI {

    companion object{
        val worldConfigurations = HashMap<String, Man10LockWorldConfig>()
        val lockedBlockData = HashMap<String, Man10LockedBlock?>()
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

    fun blockId(l: Location): String{
        return l.world.name + "|" + l.blockX + "|" + l.blockY + "|" + l.blockZ
    }

    fun getLockBlock(l: Location): Man10LockedBlock?{
        val blockId = blockId(l)
        if(blockId in lockedBlockData){
            return lockedBlockData[blockId]
        }
        val result = Man10Lock.mysql.query("SELECT * FROM man10lock_blocks WHERE server = '" + Man10Lock.serverName + "' AND block_id = '" + blockId + " '")
        if(result.size == 0){
            lockedBlockData[blockId] = null
            return null
        }
        for(rs in result){
            val user = Man10LockUser(rs.getString("name"), UUID.fromString(rs.getString("uuid")), Man10LockPermission.valueOf(rs.getString("permission")))



            //if second time just add user
            if(blockId in lockedBlockData) {
                lockedBlockData[blockId]?.addUser(user)
                continue
            }

            lockedBlockData[blockId] = Man10LockedBlock(rs.getString("block_id"))


        }
        return lockedBlockData[blockId]
    }

    fun lockBlock(l: Location, player: Player, consumer: Consumer<Boolean>){

        val targetLockBlock = getLockBlock(l)
        if(targetLockBlock != null) {
            consumer.accept(false)
            return
        }

        if(l.block.type == Material.AIR){
            consumer.accept(false)
            return
        }

        val lockBlockId = blockId(l)

        val lockBlockObject = Man10LockedBlock(lockBlockId)
        lockBlockObject.addUser(Man10LockUser(player.name, player.uniqueId, Man10LockPermission.OWNER))


        val payload = HashMap<String, Any?>()
        payload["server"] = Man10Lock.serverName
        payload["block_id"] = lockBlockId
        payload["user_block_id"] = player.uniqueId.toString() + "|" + lockBlockId
        payload["block_type"] = l.block.type.name
        payload["name"] = player.name
        payload["uuid"] = player.uniqueId
        payload["permission"] = Man10LockPermission.OWNER.name


        Man10Lock.mysql.asyncExecute(MySQLAPI.buildReplaceQuery(payload, "man10lock_blocks")) {
            if(!it) return@asyncExecute
            lockedBlockData[lockBlockId] = lockBlockObject
            consumer.accept(it)
        }

    }

}