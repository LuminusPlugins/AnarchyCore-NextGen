package luminus.acng.features.gameplay.miscs

import luminus.acng.Main.config
import luminus.acng.msg
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.simpleCommand
import taboolib.platform.util.kill

object Suicide {
    @Awake(LifeCycle.ENABLE)
    fun init() {
        simpleCommand("kill", aliases = arrayListOf("514", "suicide"), permission = "anarchy.suicide", permissionDefault = PermissionDefault.TRUE) { sender, args ->
            if (!config.getBoolean("suicide-enable", true)) return@simpleCommand
            if (sender !is Player) return@simpleCommand
            if (sender.isOp) {
                sender.performCommand("minecraft:kill "+args.joinToString(" "))
                return@simpleCommand
            }
            sender.kill()
            sender.msg(config.getString("messages.suicide", "")!!)
        }
    }
}