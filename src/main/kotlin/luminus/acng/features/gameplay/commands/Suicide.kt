package luminus.acng.features.gameplay.commands

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.simpleCommand

object Suicide {
    @Awake(LifeCycle.ENABLE)
    fun init() {
        simpleCommand("kill", aliases = arrayListOf("514", "suicide")) { sender, _ ->
        }
    }
}