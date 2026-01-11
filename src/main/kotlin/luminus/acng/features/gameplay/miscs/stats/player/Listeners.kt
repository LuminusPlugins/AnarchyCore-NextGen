package luminus.acng.features.gameplay.miscs.stats.player

import cn.guangchen233.crystallistener.events.PlayerDeathByPlayerWithCrystalEvent
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.JoinQuit.getPlayerJoins
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.JoinQuit.getPlayerQuits
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.KillDeath.calculateKD
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.KillDeath.deathsKey
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.KillDeath.getPlayerDeaths
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.KillDeath.getPlayerKills
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.KillDeath.killsKey
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.OnlineTime.getTotalOnlineTime
import org.bukkit.NamespacedKey
import org.bukkit.entity.EnderCrystal
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.platform.util.killer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

@Config("statistics.yml")
lateinit var config: ConfigFile

object Listeners {
    object KillDeath {
        val killsKey = NamespacedKey("anarchycore-nextgen", "player-kills")
        val deathsKey = NamespacedKey("anarchycore-nextgen", "player-deaths")



        private val isStatisticsEnabled: Boolean
            get() = config.getBoolean("player.enable", true)

        private val killsIncludeCrystal: Boolean
            get() = config.getBoolean("player.kills-include-crystal", true)

        private val crystalKillsIncludeSuicide: Boolean
            get() = config.getBoolean("player.crystal-kills-include-suicide", true)

        @SubscribeEvent
        fun onKill(event: PlayerDeathEvent) {
            if (!isStatisticsEnabled) return

            if (event.killer is EnderCrystal) {
                return
            }

            if (event.killer != null && event.killer is Player) {
                updateKills(event.killer as Player, 1)
            }
        }

        @SubscribeEvent
        fun onCrystalKill(event: PlayerDeathByPlayerWithCrystalEvent) {
            if (!isStatisticsEnabled) return

            if (!killsIncludeCrystal) return

            if (event.isSuicide && !crystalKillsIncludeSuicide) return

            val killer = event.killer

            updateKills(killer, 1)
        }

        @SubscribeEvent
        fun onDeath(event: PlayerDeathEvent) {
            updateDeaths(event.entity, 1)
        }

        private fun updateKills(player: Player?, amount: Int) {
            player?.let {
                val pdc = it.persistentDataContainer
                val currentKills = pdc.get(killsKey, PersistentDataType.INTEGER) ?: 0
                pdc.set(killsKey, PersistentDataType.INTEGER, currentKills + amount)
            }
        }

        private fun updateDeaths(player: Player?, amount: Int) {
            player?.let {
                val pdc = it.persistentDataContainer
                val currentDeaths = pdc.get(deathsKey, PersistentDataType.INTEGER) ?: 0
                pdc.set(deathsKey, PersistentDataType.INTEGER, currentDeaths + amount)
            }
        }

        fun getPlayerKills(player: Player): Int {
            return player.persistentDataContainer.get(killsKey, PersistentDataType.INTEGER) ?: 0
        }

        fun getPlayerDeaths(player: Player): Int {
            return player.persistentDataContainer.get(deathsKey, PersistentDataType.INTEGER) ?: 0
        }

        fun getPlayerKD(player: Player): Double {
            val kills = getPlayerKills(player)
            val deaths = getPlayerDeaths(player)
            return calculateKD(kills, deaths)
        }

        fun calculateKD(kills: Int, deaths: Int): Double {
            return if (deaths == 0) kills.toDouble() else kills.toDouble() / deaths.toDouble()
        }
    }

    object JoinQuit {
        val joinsKey = NamespacedKey("anarchycore-nextgen", "player-joins")
        val quitsKey = NamespacedKey("anarchycore-nextgen", "player-quits")
        private fun updateJoins(player: Player?, amount: Int) {
            player?.let {
                val pdc = it.persistentDataContainer
                val currentJoins = pdc.get(joinsKey, PersistentDataType.INTEGER) ?: 0
                pdc.set(killsKey, PersistentDataType.INTEGER, currentJoins + amount)
            }
        }

        private fun updateQuits(player: Player?, amount: Int) {
            player?.let {
                val pdc = it.persistentDataContainer
                val currentQuits = pdc.get(quitsKey, PersistentDataType.INTEGER) ?: 0
                pdc.set(deathsKey, PersistentDataType.INTEGER, currentQuits + amount)
            }
        }

        fun getPlayerJoins(player: Player): Int {
            return player.persistentDataContainer.get(joinsKey, PersistentDataType.INTEGER) ?: 0
        }

        fun getPlayerQuits(player: Player): Int {
            return player.persistentDataContainer.get(quitsKey, PersistentDataType.INTEGER) ?: 0
        }

        @SubscribeEvent
        fun onJoin(event: PlayerJoinEvent) {
            updateJoins(event.player, 1)
        }

        @SubscribeEvent
        fun onQuit(event: PlayerQuitEvent) {
            updateQuits(event.player,1)
        }
    }

    object OnlineTime {

        private val joinTimeMap = ConcurrentHashMap<ProxyPlayer, Long>()

        private val ONLINE_TIME_KEY by lazy {
            NamespacedKey("anarchycore-nextgen", "online-time")
        }

        private val initialized = AtomicBoolean(false)

        @Awake(LifeCycle.ENABLE)
        fun init() {
            if (!initialized.compareAndSet(false, true)) return

            submit (async = true, delay = 1200L, period = 1200L) {
                cleanupOfflinePlayers()
            }
        }
        @SubscribeEvent
        fun onPlayerJoin(event: PlayerJoinEvent) {
            val player = adaptPlayer(event.player)
            joinTimeMap[player] = System.currentTimeMillis()
        }

        @SubscribeEvent
        fun onPlayerQuit(event: PlayerQuitEvent) {
            val player = adaptPlayer(event.player)
            val joinTime = joinTimeMap.remove(player) ?: return

            val sessionSeconds = (System.currentTimeMillis() - joinTime) / 1000

            updatePDCOnQuit(player, sessionSeconds)
        }

        private fun updatePDCOnQuit(player: ProxyPlayer, sessionSeconds: Long) {
            if (sessionSeconds <= 0) return

            player.castSafely<Player>()?.let { bukkitPlayer ->
                submit(async = false) {
                    val pdc = bukkitPlayer.persistentDataContainer
                    val currentTotal = pdc.get(ONLINE_TIME_KEY, PersistentDataType.LONG) ?: 0L
                    val newTotal = currentTotal + sessionSeconds

                    pdc.set(ONLINE_TIME_KEY, PersistentDataType.LONG, newTotal)
                }
            }
        }

        fun getTotalOnlineTime(player: ProxyPlayer): Long {
            val bukkitPlayer = player.castSafely<Player>() ?: return 0L

            val pdcTotal = bukkitPlayer.persistentDataContainer.get(ONLINE_TIME_KEY, PersistentDataType.LONG) ?: 0L

            val currentSessionSeconds = joinTimeMap[player]?.let { joinTime ->
                (System.currentTimeMillis() - joinTime) / 1000
            } ?: 0L

            return pdcTotal + currentSessionSeconds
        }

        fun getCurrentSessionTime(player: ProxyPlayer): Long {
            return joinTimeMap[player]?.let { joinTime ->
                (System.currentTimeMillis() - joinTime) / 1000
            } ?: 0L
        }
        fun getStoredOnlineTime(player: ProxyPlayer): Long {
            val bukkitPlayer = player.castSafely<Player>() ?: return 0L
            var result: Long = 0
            submit(async = false) {
                result = bukkitPlayer.persistentDataContainer.get(ONLINE_TIME_KEY, PersistentDataType.LONG) ?: 0L
            }
            return result
        }

        private fun cleanupOfflinePlayers() {
            val iterator = joinTimeMap.iterator()
            while (iterator.hasNext()) {
                val (player, _) = iterator.next()
                if (!player.isOnline()) {
                    iterator.remove()
                }
            }
        }

        fun getJoinTime(player: ProxyPlayer): Long? {
            return joinTimeMap[player]
        }

        fun isTracking(player: ProxyPlayer): Boolean {
            return joinTimeMap.containsKey(player)
        }

        fun resetOnlineTime(player: ProxyPlayer, clearCurrentSession: Boolean = true) {
            player.castSafely<Player>()?.let { bukkitPlayer ->
                submit(async = false) {
                    bukkitPlayer.persistentDataContainer.remove(ONLINE_TIME_KEY)
                }
            }

            if (clearCurrentSession) {
                joinTimeMap.remove(player)
            }
        }

        fun saveCurrentSession(player: ProxyPlayer): Boolean {
            val joinTime = joinTimeMap[player] ?: return false

            val sessionSeconds = (System.currentTimeMillis() - joinTime) / 1000
            if (sessionSeconds <= 0) return false

            updatePDCOnQuit(player, sessionSeconds)

            joinTimeMap[player] = System.currentTimeMillis()

            return true
        }

        fun getTrackedPlayers(): Set<ProxyPlayer> {
            return joinTimeMap.keys.filter { it.isOnline() }.toSet()
        }
    }
}

fun Player.getPlayerData(): Command.PlayerData {
    val kills = getPlayerKills(this); val deaths = getPlayerDeaths(this)
    return Command.PlayerData(kills, deaths, calculateKD(kills, deaths), getPlayerJoins(this), getPlayerQuits(this), Command.DateTime.decodeSecondsToDateTime(getTotalOnlineTime(adaptPlayer(this))))
}