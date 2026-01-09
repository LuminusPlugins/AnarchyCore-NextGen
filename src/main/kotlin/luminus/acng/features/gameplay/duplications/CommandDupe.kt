package luminus.acng.features.gameplay.duplications

import luminus.acng.Main.config
import luminus.acng.msg
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.*

object CommandDupe {
    private val dupeCmd = listOf("dupe", "fz", "duplication", "cmd-dupe", "duping", "fuzhi")

    @Awake(LifeCycle.ENABLE)
    fun init() {
        simpleCommand("dupe") { sender, _ ->
            if (sender !is Player) {

                return@simpleCommand
            }

            if (!config.getBoolean("duplication.command.enable")) {
                return@simpleCommand
            }

            val item = sender.inventory.itemInMainHand

            if (item.type.isAir) {
                config.getString("messages.no-item", "You must hold an item to dupe!")?.let { sender.msg(it) }
                return@simpleCommand
            }

            val multiplyTimes = config.getInt("duplication.command.multiply-times", 2)
            val newItem = item.clone()
            val maxAmount = config.getInt("duplication.command.max-amount")
            val amount = if (item.amount * multiplyTimes <= maxAmount) item.amount * multiplyTimes else maxAmount
            newItem.amount = amount

            sender.inventory.addItem(newItem).forEach { (_, itemStack) ->
                sender.world.dropItemNaturally(sender.location, itemStack)
            }

            config.getString("messages.success-dupe", "Successfully duped")?.let { sender.msg(it) }

        }
    }
}