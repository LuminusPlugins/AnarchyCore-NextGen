package luminus.acng.features.gameplay.limits

import luminus.acng.Main.config
import luminus.acng.msg
import org.bukkit.entity.EnderCrystal
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.common.platform.event.SubscribeEvent
import java.util.*

object CrystalSpeedLimit {
    private val lastAttackTimes = mutableMapOf<UUID, Long>()
    private val cooldownPlayers = mutableSetOf<UUID>()

    @SubscribeEvent
    fun onClick(event: EntityDamageByEntityEvent) {
        if (event.entity !is EnderCrystal) return
        val damager = event.damager
        if (damager !is Player) return

        val enable = config.getBoolean("limits.crystal-speed.enable", false)
        if (!enable) return

        val limit = config.getLong("limits.crystal-speed.limit", 300L)

        val now = System.currentTimeMillis()
        val playerId = damager.uniqueId
        val lastAttackTime = lastAttackTimes[playerId]

        if (lastAttackTime != null) {
            val timeDiff = now - lastAttackTime
            if (timeDiff < limit) {
                event.isCancelled = true
                if (!cooldownPlayers.contains(playerId)) {
                    val remainingTime = limit - timeDiff
                    val remainingSeconds = String.format("%.1f", remainingTime / 1000.0)
                    val msg = config.getString("messages.crystal-too-fast") ?: ""
                    if (msg != "") damager.msg(msg.replace("%remain-seconds%", remainingSeconds))
                    cooldownPlayers.add(playerId)
                    taboolib.common.platform.function.submit(delay = 20) {
                        cooldownPlayers.remove(playerId)
                    }
                }
                return
            }
        }
        lastAttackTimes[playerId] = now
    }
    fun cleanupOldData() {
        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - 60000

        val iterator = lastAttackTimes.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value < oneMinuteAgo) {
                iterator.remove()
            }
        }
    }
}