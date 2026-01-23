package luminus.acng

import luminus.acng.features.gameplay.duplications.ChickenDupe
import luminus.acng.features.gameplay.duplications.MineAndPlaceDupe
import luminus.acng.features.gameplay.limits.CrystalSpeedLimit
import luminus.acng.utils.Metrics
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import taboolib.common.platform.Plugin
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.info
import taboolib.common.platform.function.pluginVersion
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitMetrics
import taboolib.platform.BukkitPlugin


// 1
object Main : Plugin() {

    @Config("config.yml")
    lateinit var config: Configuration
    private const val CONFIG_VERSION = 4

    override fun onEnable() {
        if (config.getInt("config-ver", CONFIG_VERSION) != CONFIG_VERSION)
            BukkitPlugin.getInstance().saveResource("config.yml", true)
        info("Successfully loaded AnarchyCore-NextGen")
        BukkitMetrics(BukkitPlugin.getInstance(), "AnarchyCore-NextGen", 28895, "0.1.0")
    }

    @CommandHeader("anarchycore", aliases = ["acng"], permission = "anarchy.reload", permissionDefault = PermissionDefault.OP)
    object CommandMain {
        @CommandBody
        val info = subCommand {
            execute<CommandSender> { sender, _, _ ->
                sender.sendMessage("Version: $pluginVersion")
            }
        }

        @CommandBody
        val reload = subCommand {
            execute<CommandSender> { sender, _, _ ->
                config.reload()
                luminus.acng.features.gameplay.miscs.stats.player.config.reload()
                sender.msg("&eReloaded config.")
            }
        }

        @CommandBody
        val clearcache = subCommand {
            execute<CommandSender> { sender, _, _ ->
                ChickenDupe.XinMode.reload()
                MineAndPlaceDupe.map = hashMapOf()
                CrystalSpeedLimit.cleanupOldData()
                sender.msg("&eCleared caches")
            }
        }
    }
}

fun CommandSender.msg(vararg args: Any) {
    adaptCommandSender(this).message(args)
}

fun ProxyCommandSender.message(vararg args: Any) {
    var message = ""
    args.forEach {
        message += it.toString()
    }
    sendMessage(ChatColor.translateAlternateColorCodes('&', message)) // 这里原来是str 我怎么傻了吧唧的
}
