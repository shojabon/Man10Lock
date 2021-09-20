package com.shojabon.man10lock

import com.shojabon.man10lock.Utils.MySQL.MySQLAPI
import com.shojabon.man10lock.commands.subCommands.LockBlockCommand
import com.shojabon.man10lock.dataClass.Man10LockUser
import com.shojabon.man10lock.dataClass.Man10LockWorldConfig
import com.shojabon.man10lock.dataClass.Man10LockedBlock
import com.shojabon.man10lock.enums.Man10LockPermission
import it.unimi.dsi.fastutil.Hash
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import java.util.*
import java.util.function.Consumer
import javax.security.sasl.AuthorizeCallback
import kotlin.collections.HashMap

class Man10LockAPI {

    companion object{
        val worldConfigurations = HashMap<String, Man10LockWorldConfig>()
        val lockedBlockData = HashMap<String, Man10LockedBlock?>()
        val ownerLockCount = HashMap<UUID, HashMap<String, Int>>()
        val ownerLockBlock = HashMap<UUID, ArrayList<String>>()
    }

    init {
        loadConfig()
        loadAllLockedBlocks()
    }

    fun loadConfig(){
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

            worldConfigurations[key] = Man10LockWorldConfig(key, allowedMaterials, maxAllowedLocks)
        }
    }

    fun blockIdCore(l: Location): String{
        return l.world.name + "|" + l.blockX + "|" + l.blockY + "|" + l.blockZ
    }

    //util functions
     fun splitDoubleChest(dChest: DoubleChest): Array<Chest> {
        return arrayOf(
            dChest.leftSide as Chest,
            dChest.rightSide as Chest
        )
    }

    fun blockId(l: Location): String {
        var blockId = blockIdCore(l)
        if(l.block.type == Material.CHEST || l.block.type == Material.TRAPPED_CHEST){
            if(l.block.state is InventoryHolder){
                if((l.block.state as InventoryHolder).inventory.holder is DoubleChest){
                    val chests = splitDoubleChest((l.block.state as InventoryHolder).inventory.holder as DoubleChest)
                    blockId = blockIdCore(chests[0].location)
                }
            }
        }
        //door
        if(l.block.blockData is Bisected){
            val door = l.block.blockData as Bisected
            if(door.half == Bisected.Half.TOP){
                blockId = blockIdCore(l.subtract(0.0, 1.0, 0.0))
            }
        }
        return blockId
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

            if(!lockedBlockData.containsKey(blockId)) {
                lockedBlockData[blockId] = Man10LockedBlock(rs.getString("block_id"))
            }

            lockedBlockData[blockId]?.addUser(user)

            //owner count
            val blockLocation = lockedBlockData[blockId]?.getLocation()
            if(user.permission == Man10LockPermission.OWNER){
                if (blockLocation != null) {
                    addLockedBlockCountInWorld(user.uuid, blockLocation.world.name)
                    //locked block cache
                    if(!ownerLockBlock.containsKey(user.uuid)){
                        ownerLockBlock[user.uuid] = ArrayList()
                    }
                    ownerLockBlock[user.uuid]?.add(blockId)
                }
            }


        }
        return lockedBlockData[blockId]
    }

    fun getLockBlock(l: Location): Man10LockedBlock?{

        val lockBlockId = blockId(l)

        return getLockBlock(lockBlockId)
    }

    fun lockBlock(l: Location, name: String, uuid: UUID, perm: Man10LockPermission, consumer: Consumer<Boolean>){

        if(l.block.type == Material.AIR){
            consumer.accept(false)
            return
        }

        var lockBlockId = blockId(l)

        var lockBlockObject = getLockBlock(l)
        if(lockBlockObject == null){
            lockBlockObject = Man10LockedBlock(lockBlockId)
        }
        lockBlockObject.addUser(Man10LockUser(name, uuid, perm))


        val payload = HashMap<String, Any?>()
        payload["server"] = Man10Lock.serverName
        payload["block_id"] = lockBlockId
        payload["user_block_id"] = "$uuid|$lockBlockId"
        payload["block_type"] = l.block.type.name
        payload["name"] = name
        payload["uuid"] = uuid
        payload["permission"] = perm.name

        Man10Lock.mysql.asyncExecute(MySQLAPI.buildReplaceQuery(payload, "man10lock_blocks")) {
            if(!it) {
                consumer.accept(it)
                return@asyncExecute
            }
            lockedBlockData[lockBlockId] = lockBlockObject


            if(perm == Man10LockPermission.OWNER){
                if(!ownerLockBlock.containsKey(uuid)){
                    ownerLockBlock[uuid] = ArrayList()
                }
                ownerLockBlock[uuid]?.add(lockBlockId)
                addLockedBlockCountInWorld(uuid, l.world.name)
            }

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

        val lockBlockId = blockId(l)
        Man10Lock.mysql.asyncExecute("DELETE FROM man10lock_blocks WHERE user_block_id = '$uuid|$lockBlockId'") {
            if(!it) {
                consumer.accept(it)
                return@asyncExecute
            }

            //delete owner block map
            if(lockedBlock.userIsOwner(uuid)){
                if(ownerLockBlock.containsKey(uuid)) ownerLockBlock[uuid]?.remove(lockBlockId)//delete owner locked block map
            }

            //delete block in cache
            lockedBlock.removeUser(uuid)
            if(lockedBlock.permissionUsers.isEmpty()){
                lockedBlockData.remove(lockBlockId)
            }
            consumer.accept(it)
        }
    }

    fun deleteLockBlock(l: Location, consumer: Consumer<Boolean>){
        if(l.block.type == Material.AIR){
            consumer.accept(false)
            return
        }

        val lockedBlock = getLockBlock(l)
        if(lockedBlock == null) {
            consumer.accept(false)
            return
        }

        val lockBlockId = blockId(l)
        Man10Lock.mysql.asyncExecute("DELETE FROM man10lock_blocks WHERE block_id = '$lockBlockId'") {
            if(!it) {
                consumer.accept(it)
                return@asyncExecute
            }

            for(user in lockedBlock.permissionUsers.values){
                if(user.permission == Man10LockPermission.OWNER){
                    removeLockedBlockCountInWorld(user.uuid, l.world.name) // delete owner block count
                    if(ownerLockBlock.containsKey(user.uuid)) ownerLockBlock[user.uuid]?.remove(lockBlockId) //delete owner locked block map
                }
            }

            lockedBlockData.remove(lockBlockId)
            consumer.accept(it)
        }
    }

    //cache functions

    fun loadAllLockedBlocks(){
        Man10Lock.loadingChests = true
        Man10Lock.mysql.asyncQuery("SELECT block_id FROM man10lock_blocks WHERE server = '" + Man10Lock.serverName + "' GROUP BY block_id"
        ) {
            try{
                for (rs in it) {
                    getLockBlock(rs.getString("block_id"))
                }
            }catch (e: Exception){}
            Man10Lock.loadingChests = false
        }
    }

    //lock count

    fun getLockedBlockCountInWorld(uuid: UUID, world: String): Int {
        if(!ownerLockCount.containsKey(uuid))return 0
        if(!ownerLockCount[uuid]?.containsKey(world)!!) return 0
        return ownerLockCount[uuid]?.get(world) ?: return 0
    }

    fun addLockedBlockCountInWorld(uuid: UUID, world: String){
        if(!ownerLockCount.containsKey(uuid)){
            ownerLockCount[uuid] = HashMap()
        }
        if(!ownerLockCount[uuid]?.containsKey(world)!!) {
            ownerLockCount[uuid]?.set(world, 0)
        }
        val originalValue = ownerLockCount[uuid]?.get(world)
        if (originalValue != null) {
            ownerLockCount[uuid]?.set(world, originalValue+1)
        }
    }

    fun removeLockedBlockCountInWorld(uuid: UUID, world: String){
        if(!ownerLockCount.containsKey(uuid))return
        if(!ownerLockCount[uuid]?.containsKey(world)!!) return
        var originalValue = ownerLockCount[uuid]?.get(world)
        if (originalValue != null) {
            if(originalValue-1 < 0) originalValue = 0
        }
        if (originalValue != null) {
            ownerLockCount[uuid]?.set(world, originalValue-1)
        }
    }


}