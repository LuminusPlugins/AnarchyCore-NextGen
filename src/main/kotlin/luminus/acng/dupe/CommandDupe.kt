package luminus.acng.dupe

import luminus.acng.Main.config
import luminus.acng.msg
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import taboolib.common.platform.event.SubscribeEvent

object CommandDupe {
    private val dupeCmd = listOf("dupe", "fz", "duplication", "cmd-dupe", "duping", "fuzhi")

    @SubscribeEvent
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        if (isDupeCommand(event.message) && event.player.hasPermission("anarchy.dupe.command") && config.getBoolean("duplication.command.enable")) {
            event.isCancelled = true
            val player = event.player
            val item = player.inventory.itemInMainHand
            if (item.type.isAir) {
                config.getString("messages.no-item", "You must hold an item to dupe!")?.let { player.msg(it) }
                return
            }
            val multiplyTimes = config.getInt("duplication.command.multiply-times", 2)
            val newItem = item.clone()
            val maxAmount = config.getInt("duplication.command.max-amount")
            val amount = if (item.amount * multiplyTimes <= maxAmount) item.amount * multiplyTimes else maxAmount
            newItem.amount = amount
            player.inventory.addItem(newItem).forEach { (_, itemStack) ->
                player.world.dropItemNaturally(player.location, itemStack)
            }
            config.getString("messages.success-dupe", "Successfully duped")?.let { player.msg(it) }
        }
    }

    private fun isDupeCommand(s: String): Boolean {
        val command = if (s.startsWith("/")) {
            s.substring(1).split(" ")[0].lowercase()
        } else {
            s.split(" ")[0].lowercase()
        }
        return dupeCmd.contains(command)
    }
}