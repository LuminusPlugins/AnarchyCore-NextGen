package luminus.acng.features.gameplay.miscs.stats.player

import cn.guangchen233.crystallistener.events.PlayerDeathByPlayerWithCrystalEvent
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.JoinQuit.getPlayerJoins
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.JoinQuit.getPlayerQuits
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.KillDeath.calculateKD
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.KillDeath.deathsKey
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.KillDeath.getPlayerDeaths
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.KillDeath.getPlayerKills
import luminus.acng.features.gameplay.miscs.stats.player.Listeners.KillDeath.killsKey
import org.bukkit.NamespacedKey
import org.bukkit.entity.EnderCrystal
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.platform.util.killer

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
                if (killsIncludeCrystal) return
            }

            if (event.killer != null && event.killer !is EnderCrystal && event.killer is Player) {
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
}

fun Player.getPlayerData(): Command.PlayerData {
    val kills = getPlayerKills(this); val deaths = getPlayerDeaths(this)
    return Command.PlayerData(kills, deaths, calculateKD(kills, deaths), getPlayerJoins(this), getPlayerQuits(this))
}