package luminus.acng.features.gameplay.duplications
import luminus.acng.Main.config
import luminus.acng.msg
import org.bukkit.Bukkit
import org.bukkit.entity.AbstractHorse
import org.bukkit.entity.Boat
import org.bukkit.entity.Entity
import org.bukkit.entity.HumanEntity
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.platform.util.killer

object DonkeyDupe {
    object XinMode {
        @SubscribeEvent
        fun onKill(event: EntityDeathEvent) {
            if (config.getBoolean("duplication.donkey.xin-mode", false) && event.entity is AbstractHorse && event.killer is HumanEntity && event.killer?.hasPermission("anarchy.dupe.donkey.xin") == true) {
                val entity = event.entity as AbstractHorse
                submit(async = true) {
                    entity.inventory.contents.forEach {
                        entity.world.dropItemNaturally(entity.location, it)
                        config.getString("messages.success-dupe", "Successfully duped")?.let { it1 -> event.killer?.msg(it1) }
                    }
                }
            }
        }
    }

    object OrgMode {
            @SubscribeEvent
            fun onPlayerQuit(event: PlayerQuitEvent) {
                val player = event.player
                val vehicle = player.vehicle ?: return
                if (!player.hasPermission("anarchy.dupe.donkey.org")) return

                submit(async = true) {
                    if (config.getBoolean("duplication.donkey.org-mode-allow-boat-chain", false)) {
                        processVehicleChain(vehicle)
                    } else {
                        processAbstractHorse(vehicle)
                    }
                    config.getString("messages.success-dupe", "Successfully duped")?.let { it1 -> player.msg(it1) }
                }
            }

            private fun processVehicleChain(entity: Entity) {
                when {
                    entity is Boat -> entity.passengers.forEach(OrgMode::processAbstractHorse)
                    entity.vehicle is Boat -> (entity.vehicle as Boat).passengers.forEach(OrgMode::processAbstractHorse)
                    else -> processAbstractHorse(entity)
                }
            }


            private fun processAbstractHorse(entity: Entity) {
                if (entity !is AbstractHorse) return
                entity.duplicateInventoryForViewers()
            }

            private fun AbstractHorse.duplicateInventoryForViewers() {
                val originalInventory = inventory
                val viewers = originalInventory.viewers.toList()

                if (viewers.isEmpty()) return

                val clonedInventory = createDuplicatedInventory(originalInventory)

                viewers.asReversed().forEach { viewer ->
                    viewer.closeInventory()
                    submit(delay = 2) {
                        viewer.openInventory(clonedInventory)
                    }
                }
            }

            private fun createDuplicatedInventory(source: Inventory): Inventory {
                val cloned = Bukkit.createInventory(null, source.type, "Duplicated Inventory")

                source.contents
                    .withIndex()
                    .filter { (_, item) -> item != null }
                    .forEach { (slot, item) ->
                        cloned.setItem(slot, item!!.clone())
                    }

                return cloned
            }
    }
}