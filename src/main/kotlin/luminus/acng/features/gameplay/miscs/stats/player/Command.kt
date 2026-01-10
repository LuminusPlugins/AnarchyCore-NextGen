package luminus.acng.features.gameplay.miscs.stats.player

import luminus.acng.msg
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.command
import taboolib.common.platform.command.player


object Command {

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        command("stat", aliases = listOf("stats", "statistics", "统计")) {
            execute<CommandSender> { sender, _, _ ->
                if (sender is Player) {
                    sender.msg(buildStatisticsMessage(sender))
                }
            }
            player {
                execute<CommandSender> { sender, context, _ ->
                    sender.msg(context.player().castSafely<Player>()?.let { buildStatisticsMessage(it) } ?: "Error Statistics")
                }
            }
        }
    }

    private fun buildStatisticsMessage(player: Player): String {
        val messages = config.getStringList("messages.player.messages")
        val data = player.getPlayerData()

        val permissionPlaceholders = config.getConfigurationSection("messages.player.permission-placeholders")
        var processedMessages = ""

        messages.forEach { message ->
            var processedMessage = message

            processedMessage = processedMessage
                .replace("%name%", player.name)
                .replace("%kills%", data.kills.toString())
                .replace("%deaths%", data.deaths.toString())
                .replace("%kd%", data.kds.toString())
                .replace("%joins%", data.joins.toString())
                .replace("%quits%", data.quits.toString())

            permissionPlaceholders?.let { section ->
                section.getKeys(false).forEach { permission ->
                    val placeholder = "%has-${permission}%"
                    if (processedMessage.contains(placeholder)) {
                        val permissionConfig = section.getConfigurationSection(permission)
                        if (permissionConfig != null) {
                            val replacement = if (player.hasPermission(permission)) {
                                permissionConfig.getString("if-has", "true")
                            } else {
                                permissionConfig.getString("if-not", "false")
                            }
                            processedMessage = processedMessage.replace(placeholder, replacement ?: "")
                        }
                    }
                }
            }
            processedMessages += processedMessage + "\n"
        }
        return processedMessages
    }

    data class PlayerData(val kills: Int, val deaths: Int, val kds: Double, val joins: Int, val quits: Int) {
    }
}