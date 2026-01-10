import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.27"
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
}

taboolib {
    env {
        install(Basic)
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
            name("CrystalKillListener")
        }
    }
    version { taboolib = "6.2.0" }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
    compileOnly("io.github.guangchen2333:CrystalKillListener:2.0.0-SNAPSHOT")
}

tasks {
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