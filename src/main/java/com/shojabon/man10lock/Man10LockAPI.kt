package com.shojabon.man10lock

import com.shojabon.man10lock.Utils.MySQL.MySQLAPI
import com.shojabon.man10lock.commands.subCommands.LockBlockCommand
import com.shojabon.man10lock.dataClass.Man10LockUser
import com.shojabon.man10lock.dataClass.Man10LockWorldConfig
import com.shojabon.man10lock.dataClass.Man10LockedBlock
import com.shojabon.man10lock.enums.Man10LockPermission
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Consumer
import javax.security.sasl.AuthorizeCallback

class Man10LockAPI {

    companion object{
        val worldConfigurations = HashMap<String, Man10LockWorldConfig>()
        val lockedBlockData = HashMap<String, Man10LockedBlock?>()
    }

    init {
        loadConfig()
        loadAllLockedBlocks()
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

    private fun blockId(l: Location): String{
        return l.world.name + "|" + l.blockX + "|" + l.blockY + "|" + l.blockZ
    }

    //util functions
    private fun splitDoubleChest(dChest: DoubleChest): Array<Chest> {
        return arrayOf(
            dChest.leftSide as Chest,
            dChest.rightSide as Chest
        )
    }

    //main functions

    fun getLockBlock(blockId: String): Man10LockedBlock?{
        if(lockedBlockData.containsKey(blockId)){
            return lockedBlockData[blockId]
        }
        val result = Man10Lock.mysql.query("SELECT * FROM man10lock_blocks WHERE server = '" + Man10Lock.serverName + "' AND block_id = '" + blockId + "'")
        if(result.size == 0){
            lockedBlockData[blockId] = null
            return null
        }
        for(rs in result){
            val user = Man10LockUser(rs.getString("name"), UUID.fromString(rs.getString("uuid")), Man10LockPermission.valueOf(rs.getString("permission")))



            //if second time just add user
            if(lockedBlockData.containsKey(blockId)) {
                lockedBlockData[blockId]?.addUser(user)
                continue
            }

            lockedBlockData[blockId] = Man10LockedBlock(rs.getString("block_id"))
            lockedBlockData[blockId]?.addUser(user)


        }
        return lockedBlockData[blockId]
    }

    fun getLockBlock(l: Location): Man10LockedBlock?{
        return getLockBlock(blockId(l))
    }

    fun lockBlock(l: Location, name: String, uuid: UUID, consumer: Consumer<Boolean>){

        if(l.block.type == Material.AIR){
            consumer.accept(false)
            return
        }

        var lockBlockId = blockId(l)
        if(l.block.type == Material.CHEST && l.block.state is DoubleChest){
            val dChest = l.block.state as DoubleChest
            lockBlockId = blockId(splitDoubleChest(dChest)[0].location)
        }

        var lockBlockObject = getLockBlock(l)
        if(lockBlockObject == null){
            lockBlockObject = Man10LockedBlock(lockBlockId)
        }
        lockBlockObject.addUser(Man10LockUser(name, uuid, Man10LockPermission.OWNER))


        val payload = HashMap<String, Any?>()
        payload["server"] = Man10Lock.serverName
        payload["block_id"] = lockBlockId
        payload["user_block_id"] = "$uuid|$lockBlockId"
        payload["block_type"] = l.block.type.name
        payload["name"] = name
        payload["uuid"] = uuid
        payload["permission"] = Man10LockPermission.OWNER.name

        Man10Lock.mysql.asyncExecute(MySQLAPI.buildReplaceQuery(payload, "man10lock_blocks")) {
            if(!it) {
                consumer.accept(it)
                return@asyncExecute
            }
            lockedBlockData[lockBlockId] = lockBlockObject
            consumer.accept(it)
        }

    }

    fun removeUserLock(l: Location, uuid: UUID, consumer: Consumer<Boolean>){
        if(l.block.type == Material.AIR){
            consumer.accept(false)
            return
        }

        val lockedBlock = getLockBlock(l)
        if(lockedBlock == null) {
            consumer.accept(false)
            return
        }
        if(!lockedBlock.userCanEdit(uuid)){
            consumer.accept(false)
            return
        }

        val lockBlockId = blockId(l)
        Man10Lock.mysql.asyncExecute("DELETE FROM man10lock_blocks WHERE user_block_id = '$uuid|$lockBlockId'") {
            if(!it) {
                consumer.accept(it)
                return@asyncExecute
            }
            lockedBlock.removeUser(uuid)
            if(lockedBlock.permissionUsers.isEmpty()){
                lockedBlockData.remove(lockBlockId)
            }
            consumer.accept(it)
        }
    }

    //cache functions

    fun loadAllLockedBlocks(){
        Man10Lock.mysql.asyncQuery("SELECT block_id FROM man10lock_blocks WHERE server = '" + Man10Lock.serverName + "' GROUP BY block_id"
        ) {
            for (rs in it) {
                getLockBlock(rs.getString("block_id"))
            }
        }
    }

}