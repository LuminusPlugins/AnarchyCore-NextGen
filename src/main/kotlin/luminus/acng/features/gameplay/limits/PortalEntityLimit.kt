package luminus.acng.features.gameplay.limits

import org.bukkit.entity.Entity
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object PortalEntityLimit {

    private data class PortalRecord(
        val world: String,
        val chunkX: Int,
        val chunkZ: Int,
        val entityCount: Int = 0
    )

    private val portalRecords = HashMap<String, PortalRecord>()


    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        submit(period = 100000L, async = true) {
            portalRecords.clear()
        }
    }

    @SubscribeEvent
    fun onEntityPortal(event: org.bukkit.event.entity.EntityPortalEvent) {
        val entity = event.entity

        if (!isLimitedEntity(entity)) {
            return
        }

        val config = luminus.acng.Main.config

        if (!config.getBoolean("limits.ender-portal-entities.enable", false)) {
            return
        }

        val portalKey = getPortalKey(entity.location)
        val limit = config.getInt("limits.ender-portal-entities.limit", 10)

        val currentCount = portalRecords[portalKey]?.entityCount ?: 0

        if (currentCount >= limit) {
            event.isCancelled = true
            return
        }

        portalRecords[portalKey] = portalRecords[portalKey]?.copy(
            entityCount = currentCount + 1
        ) ?: PortalRecord(
            world = entity.world.name,
            chunkX = entity.location.blockX shr 4,
            chunkZ = entity.location.blockZ shr 4,
            entityCount = 1
        )
    }

    private fun isLimitedEntity(entity: Entity): Boolean {
        return when (entity.type) {
            org.bukkit.entity.EntityType.PRIMED_TNT,
            org.bukkit.entity.EntityType.MINECART,
            org.bukkit.entity.EntityType.MINECART_CHEST,
            org.bukkit.entity.EntityType.MINECART_HOPPER,
            org.bukkit.entity.EntityType.MINECART_COMMAND,
            org.bukkit.entity.EntityType.MINECART_FURNACE,
            org.bukkit.entity.EntityType.MINECART_TNT,
            org.bukkit.entity.EntityType.MINECART_MOB_SPAWNER -> true
            else -> false
        }
    }

    private fun getPortalKey(location: org.bukkit.Location): String {
        return "${location.world?.name ?: "unknown"}-${location.blockX shr 4}-${location.blockZ shr 4}"
    }
}