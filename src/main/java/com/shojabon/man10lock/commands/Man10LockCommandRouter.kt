package com.shojabon.man10lock.commands

import com.shojabon.man10lock.Man10Lock
import com.shojabon.man10lock.Utils.SCommandRouter.SCommandArgument
import com.shojabon.man10lock.Utils.SCommandRouter.SCommandArgumentType
import com.shojabon.man10lock.Utils.SCommandRouter.SCommandObject
import com.shojabon.man10lock.Utils.SCommandRouter.SCommandRouter
import com.shojabon.man10lock.commands.subCommands.AddUserToLockBlockCommand
import com.shojabon.man10lock.commands.subCommands.LockBlockCommand
import com.shojabon.man10lock.commands.subCommands.ReloadCommand
import org.bukkit.plugin.java.JavaPlugin

class Man10LockCommandRouter (private val plugin: Man10Lock): SCommandRouter() {

    init{
        registerCommands()
        registerEvents()
    }

    private fun registerEvents(){
        setNoPermissionEvent { e -> e.sender.sendMessage(Man10Lock.prefix.toString() + "§c§lあなたは権限がありません") }
        setOnNoCommandFoundEvent { e -> e.sender.sendMessage(Man10Lock.prefix.toString() + "§c§lコマンドが存在しません") }
    }

    private fun registerCommands(){
        addCommand(
            SCommandObject()
                .addArgument(SCommandArgument().addAllowedString("reload")).addRequiredPermission("man10lock.reload")
                .addExplanation("コンフィグをリロードする").setExecutor(ReloadCommand(plugin))
        )

        // lock block

        addCommand(
            SCommandObject()
                .addArgument(SCommandArgument().addAllowedString("lock")).addRequiredPermission("man10lock.lock")
                .addExplanation("ブロックをロックする").setExecutor(LockBlockCommand(plugin))
        )

        //add user
        addCommand(
            SCommandObject()
                .addArgument(SCommandArgument().addAllowedString("addUser"))
                .addArgument(SCommandArgument().addAlias("ユーザー名"))
                .addRequiredPermission("man10lock.addUser")
                .addExplanation("ロックされたブロックにユーザーを追加する").setExecutor(AddUserToLockBlockCommand(plugin))
        )
    }
}