plugins {
    kotlin("multiplatform") version "2.0.21" apply false
    kotlin("plugin.serialization") version "2.0.21" apply false
    id("org.jetbrains.compose") version "1.7.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    kotlin("jvm") version "2.0.21" apply false
    id("pmd")
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

allprojects {
    apply(plugin = "pmd")
}
