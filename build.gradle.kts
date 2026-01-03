import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
import io.izzel.taboolib.gradle.Basic
import io.izzel.taboolib.gradle.App
import io.izzel.taboolib.gradle.Bukkit
import io.izzel.taboolib.gradle.BukkitNMS
import io.izzel.taboolib.gradle.BukkitNMSUtil
import io.izzel.taboolib.gradle.BukkitUtil
import io.izzel.taboolib.gradle.BukkitUI
import io.izzel.taboolib.gradle.BukkitHook
import io.izzel.taboolib.gradle.I18n
import io.izzel.taboolib.gradle.MinecraftChat
import io.izzel.taboolib.gradle.CommandHelper
import io.izzel.taboolib.gradle.Metrics
import io.izzel.taboolib.gradle.Database
import io.izzel.taboolib.gradle.DatabasePlayer
import io.izzel.taboolib.gradle.DatabasePlayerRedis


plugins {
    java
    id("io.izzel.taboolib") version "2.0.27"
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
}

taboolib {
    env {
        install(Basic)
        install(App)
        install(Bukkit)
        install(BukkitUtil)
        install(BukkitUI)
        install(BukkitHook)
        install(I18n)
        install(MinecraftChat)
        install(CommandHelper)
        install(Metrics)
        install(Database)
        install(DatabasePlayer)
        install(DatabasePlayerRedis)
    }
    description {
        name = "AnarchyCore-NextGen"
        desc("Anarchy Server Core Plugin")
        contributors {
            name("NaN")
        }
        links {
            name("https://luminus.leyanshi.fun")
        }
        dependencies {
            name("PlaceholderAPI")
        }
    }
    version { taboolib = "6.2.0" }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JVM_1_8)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}