package luminus.acng.features.gameplay.duplications

import luminus.acng.Main.config
import luminus.acng.msg
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Chicken
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.*

object ChickenDupe {
    object XinMode {
        private var currentTask: Any? = null

        @Awake(LifeCycle.ENABLE)
        fun onEnable() {
            if (!config.getBoolean("duplication.chicken.xin-mode")) return
            submitNextCheck()
        }

        private fun submitNextCheck() {

            val now = LocalDateTime.now()
            val currentMinute = now.minute
            val periods = targetMinutes.sorted()
            val nextMinute = periods.find { it > currentMinute }
                    ?: periods.firstOrNull()?.plus(60)
                    ?: return

            var minutesToWait = nextMinute - currentMinute
            if (minutesToWait < 0) minutesToWait += 60
            val ticksToWait = minutesToWait * 1200L
            currentTask = null
            currentTask = submit(delay = ticksToWait, async = true) {
                performDupeCheck()
                val interval = calculateInterval()
                submitRepeatedCheck(interval)
            }

            console().sendMessage("§a下一次鸡刷检测将在 §e${minutesToWait}分钟 §a后执行")
        }

        private fun performDupeCheck() {
            console().sendMessage("§e执行鸡刷检测，当前时间：${LocalDateTime.now()}")

            var totalChecked = 0
            var totalDuped = 0

            for (world in Bukkit.getWorlds()) {
                val loadedChunks = world.loadedChunks
                loadedChunks.forEach { chunk ->
                    if (!chunk.isGenerated || !chunk.isEntitiesLoaded) return@forEach

                    chunk.entities.forEach { entity ->
                        totalChecked++
                        if (entity is Chicken) {
                            val item = entity.loadItem()
                            if (item != null) {
                                submit(async = true) {
                                    entity.world.dropItemNaturally(entity.location, item.clone())
                                }
                                totalDuped++
                            }
                        }
                    }
                }
            }

            console().sendMessage("§e检测完成：检查了 §6$totalChecked §e个实体，复制了 §6$totalDuped §e个物品")
        }

        private fun calculateInterval(): Long {
            val periods = targetMinutes.sorted()
            if (periods.size < 2) return 15 * 1200L

            val intervals = mutableListOf<Int>()
            for (i in periods.indices) {
                val current = periods[i]
                val next = periods.getOrNull(i + 1) ?: (periods[0] + 60)
                intervals.add(next - current)
            }
            val firstInterval = intervals.first()
            if (intervals.all { it == firstInterval }) {
                return firstInterval * 1200L
            }

            return intervals.minOrNull()?.times(1200L) ?: (15 * 1200L)
        }

        private fun submitRepeatedCheck(intervalTicks: Long) {
            if (!config.getBoolean("duplication.chicken.xin-mode")) return
            currentTask = null

            currentTask = submit(period = intervalTicks, async = true) {
                val currentMinute = LocalDateTime.now().minute
                if (currentMinute in targetMinutes) {
                    performDupeCheck()
                } else {
                    val newInterval = calculateInterval()
                    if (newInterval != intervalTicks) {
                        submitRepeatedCheck(newInterval)
                    }
                }
            }

            console().sendMessage("§a已设置检测，间隔：§e${intervalTicks / 1200}分钟")
        }

        fun reload() {
            currentTask = null
            submitNextCheck()
        }

        private val targetMinutes = config.getIntegerList("duplication.chicken.xin-mode-periods")

        @SubscribeEvent
        fun onClick(event: PlayerInteractEntityEvent) {
            if (!config.getBoolean("duplication.chicken.xin-mode")) return
            if (!event.player.hasPermission("anarchy.dupe.chicken.xin")) return
            if (!(event.rightClicked.type == EntityType.CHICKEN || event.rightClicked is Chicken)) return

            val player = event.player
            val chicken = event.rightClicked as Chicken
            val item = player.inventory.itemInMainHand


            if (!item.type.toString().uppercase().contains("SHULKER_BOX")) return

            chicken.customName = ChatColor.translateAlternateColorCodes(
                    '&',
                    "&6&l"
            ) + item.itemMeta?.displayName
            chicken.saveItem(item.clone())
            chicken.world.dropItemNaturally(chicken.location, item)
            player.inventory.setItemInMainHand(ItemStack(Material.AIR))
        }

        private fun Chicken.saveItem(item: ItemStack) {
            persistentDataContainer.set(itemKey, ItemStackPersistentDataType, item)
        }

        private fun Chicken.loadItem(): ItemStack? {
            return persistentDataContainer.get(itemKey, ItemStackPersistentDataType)
        }

        private val itemKey = NamespacedKey("anarchycore-nextgen", "stored_item")

        object ItemStackPersistentDataType : PersistentDataType<ByteArray, ItemStack> {

            override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java
            override fun getComplexType(): Class<ItemStack> = ItemStack::class.java

            override fun toPrimitive(
                    complex: ItemStack,
                    context: PersistentDataAdapterContext
            ): ByteArray {
                ByteArrayOutputStream().use { byteOut ->
                    java.io.ObjectOutputStream(byteOut).use { objectOut ->
                        objectOut.writeObject(complex.serialize())
                    }
                    return byteOut.toByteArray()
                }
            }

            override fun fromPrimitive(
                    primitive: ByteArray,
                    context: PersistentDataAdapterContext
            ): ItemStack {
                ByteArrayInputStream(primitive).use { byteIn ->
                    java.io.ObjectInputStream(byteIn).use { objectIn ->
                        @Suppress("UNCHECKED_CAST")
                        val map = objectIn.readObject() as Map<String, Any>
                        return ItemStack.deserialize(map)
                    }
                }
            }
        }

        @SubscribeEvent
        fun onDeath(event: EntityDeathEvent) {
            if (event.entity is Chicken) {
                event.entity.persistentDataContainer.remove(itemKey)
            }
        }
    }

    object ClickMode {
        private val cooldown = mutableMapOf<UUID, Long>()

        @SubscribeEvent
        fun onClick(event: PlayerInteractEntityEvent) {
            if (!config.getBoolean("duplication.chicken.click-mode")) return
            if (!event.player.hasPermission("anarchy.dupe.chicken.click")) return
            if (!(event.rightClicked.type == EntityType.CHICKEN || event.rightClicked is Chicken)) return

            val player = event.player
            val currentTime = System.currentTimeMillis()
            val lastTime = cooldown[player.uniqueId]

            if (lastTime != null && currentTime - lastTime < config.getInt("duplication.chicken.click-mode-cooldown") * 1000L) {
                return
            }

            val chicken = event.rightClicked as Chicken
            val item = player.inventory.itemInMainHand.clone()
            item.amount = 1

            chicken.world.dropItemNaturally(chicken.location, item)
            cooldown[player.uniqueId] = currentTime

            config.getString("messages.success-dupe", "Successfully duped")?.let { player.msg(it) }
        }
    }
}