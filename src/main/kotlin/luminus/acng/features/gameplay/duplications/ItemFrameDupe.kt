package luminus.acng.features.gameplay.duplications

import luminus.acng.Main.config
import luminus.acng.msg
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import java.util.*


object ItemFrameDupe : Listener {
    @EventHandler
    fun onRotate(event: PlayerInteractEntityEvent) {
        if (!config.getBoolean("duplication.item-frame.enable")) return
        if (!event.player.hasPermission("anarchy.dupe.item-frame")) return
        if (event.rightClicked.type == EntityType.ITEM_FRAME) {
            val itemFrame = event.rightClicked as ItemFrame

            if (Random().nextInt(100) + 1 <= config.getInt("dupe.item_frame.possibility")) {
                itemFrame.world.dropItem(itemFrame.location, itemFrame.item)
            }
            config.getString("messages.success-dupe", "Successfully duped")?.let { event.player.msg(it) }
        }
    }
}