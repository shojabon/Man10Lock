package com.shojabon.man10lock.commands

import com.shojabon.man10lock.Man10Lock
import com.shojabon.man10lock.Utils.SCommandRouter.SCommandArgument
import com.shojabon.man10lock.Utils.SCommandRouter.SCommandArgumentType
import com.shojabon.man10lock.Utils.SCommandRouter.SCommandObject
import com.shojabon.man10lock.Utils.SCommandRouter.SCommandRouter
import com.shojabon.man10lock.commands.subCommands.*
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
                .addRequiredPermission("man10lock.lock")
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

        //remove user

        addCommand(
            SCommandObject()
                .addArgument(SCommandArgument().addAllowedString("delUser"))
                .addArgument(SCommandArgument().addAlias("ユーザー名"))
                .addRequiredPermission("man10lock.delUser")
                .addExplanation("ロックされたブロックからユーザーを削除する").setExecutor(RemoveUserFromLockBlockCommand(plugin))
        )

        addCommand(
            SCommandObject()
                .addArgument(SCommandArgument().addAllowedString("info"))
                .addRequiredPermission("man10lock.info")
                .addExplanation("ロックされたブロックの情報を見る").setExecutor(LockBlockInfoCommand(plugin))
        )

        addCommand(
            SCommandObject()
                .addArgument(SCommandArgument().addAllowedString("unlock"))
                .addRequiredPermission("man10lock.unLock")
                .addExplanation("ロックされたブロックのロックを解除する").setExecutor(UnLockBlockCommand(plugin))
        )

        addCommand(
            SCommandObject()
                .addArgument(SCommandArgument().addAllowedString("list"))
                .addRequiredPermission("man10lock.list")
                .addExplanation("自分がロックしているブロック一覧").setExecutor(ListLockedBlockInfoCommand(plugin))
        )

        addCommand(
            SCommandObject()
                .addArgument(SCommandArgument().addAllowedString("list"))
                .addArgument(SCommandArgument().addAlias("プレイヤー名"))
                .addRequiredPermission("man10lock.admin")
                .addExplanation("他人のがロックしているブロック一覧").setExecutor(ListLockedBlockInfoCommand(plugin))
        )

    }
}