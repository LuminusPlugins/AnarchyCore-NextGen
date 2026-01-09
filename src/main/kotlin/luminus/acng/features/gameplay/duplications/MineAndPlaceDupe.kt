package luminus.acng.features.gameplay.duplications

import luminus.acng.Main.config
import luminus.acng.msg
import org.bukkit.block.ShulkerBox
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import taboolib.common.platform.event.SubscribeEvent


object MineAndPlaceDupe {
    private var map: HashMap<String, Int> = HashMap()

    @SubscribeEvent
    fun onMine(event: BlockBreakEvent) {
        if (!config.getBoolean("duplication.mine-and-place.enable")) return
        if (!event.player.hasPermission("anarchy.dupe.mine-and-place")) return
        if (!event.block.type.toString().lowercase().contains("shulker")) return
        if (map.containsKey(event.player.name)) {
            map[event.player.name] = 1
        } else {
            map.replace(event.player.name, map[event.player.name]!! + 1)
            if (map[event.player.name]!! >= config.getInt("duplication.mine-and-place.amount")) {
                val shulkerBox = event.block.state as ShulkerBox
                val shulkerItem = ItemStack(event.block.type)
                val blockStateMeta = shulkerItem.itemMeta as BlockStateMeta
                blockStateMeta.blockState = shulkerBox
                shulkerItem.setItemMeta(blockStateMeta)
                shulkerBox.world.dropItem(shulkerBox.location, shulkerItem)
                map.remove(event.player.name)
            }
            config.getString("messages.success-dupe", "Successfully duped")?.let { event.player.msg(it) }
        }
    }
}