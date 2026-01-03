package luminus.acng.dupe

import luminus.acng.Main.config
import luminus.acng.msg
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Chicken
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
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
import java.util.UUID

object ChickenDupe {
    object XinMode {
        @Awake(LifeCycle.ENABLE)
        fun onEnable() {
            if (!(config.getBoolean("duplication.chicken.xin-mode"))) return
            submit(period = 40, async = true) {
                val minute = LocalDateTime.now().minute
                if (minute in targetMinutes) {
                    console().sendMessage("§e固定频率鸡刷检测中，当前时间：${LocalDateTime.now()}")

                    Bukkit.getWorlds().forEach { world ->
                        world.loadedChunks.forEach { chunk ->
                            if (chunk.isGenerated && chunk.isEntitiesLoaded) {
                                chunk.entities.forEach { entity ->
                                    if (entity is Chicken && entity.loadItem() != null) {
                                        entity.loadItem()?.clone()
                                            ?.let { entity.world.dropItemNaturally(entity.location, it) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private val targetMinutes = config.getIntegerList("duplication.chicken.xin-mode-periods")
        @SubscribeEvent
        fun onClick(event: PlayerInteractEntityEvent) {
            if (!(config.getBoolean("duplication.chicken.xin-mode"))) return
            if (!(event.player.hasPermission("anarchy.dupe.chicken.xin"))) return
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
        private val cooldown = mapOf<UUID, Long>()
        @SubscribeEvent
        fun onClick(event: PlayerInteractEntityEvent) {
            if (!(config.getBoolean("duplication.chicken.click-mode"))) return
            if (!(event.player.hasPermission("anarchy.dupe.chicken.click"))) return
            if (!(event.rightClicked.type == EntityType.CHICKEN || event.rightClicked is Chicken)) return
            val player = event.player
            if (cooldown.contains(player.uniqueId) && System.currentTimeMillis() - cooldown[player.uniqueId]!! < config.getInt("duplication.chicken.click-mode-cooldown")*1000 ) return
            val chicken = event.rightClicked as Chicken
            val item = player.inventory.itemInMainHand.clone()
            item.amount = 1
            chicken.world.dropItemNaturally(chicken.location, item)
            config.getString("messages.success-dupe", "Successfully duped")?.let { player.msg(it) }
        }
    }
}

