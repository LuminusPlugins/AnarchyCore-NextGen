package luminus.acng

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import taboolib.common.platform.Plugin
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.info
import taboolib.common.platform.function.pluginVersion
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin

object Main : Plugin() {

    @Config("config.yml")
    lateinit var config: Configuration
    private const val CONFIG_VERSION = 1;

    override fun onEnable() {
        if (config.getInt("config-ver", CONFIG_VERSION) != CONFIG_VERSION)
            BukkitPlugin.getInstance().saveResource("config.yml", true)
        info("Successfully loaded AnarchyCore-NextGen")

    }

    @CommandHeader("anarchycore", aliases = ["acng"])
    object CommandMain {
        @CommandBody
        val info = subCommand {
            execute<CommandSender> { sender, _, _ ->
                sender.sendMessage("Version: $pluginVersion")
            }
        }

        @CommandBody
        val config = subCommand {
            literal("reload", "rl") {
                execute<CommandSender> { sender, _, _ ->
                    Main.config.reload()
                    sender.sendMessage("配置已重载")
                }
            }

            literal("save") {
                execute<CommandSender>{ sender, _, _ ->
                    Main.config.saveToFile()
                    sender.sendMessage("配置已保存")
                }
            }
        }
    }
}

fun CommandSender.msg(str: String, vararg args: Any) {
    var message = str
    args.forEach {
        message += it.toString()
    }
    sendMessage(ChatColor.translateAlternateColorCodes('&', str))
}