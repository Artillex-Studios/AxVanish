plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version("9.2.2")
}

group = "com.artillexstudios.axvanish"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()

        maven("https://jitpack.io/")
        maven("https://repo.artillex-studios.com/releases/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":api"))
}

subprojects {
    apply {
        plugin("java")
        plugin("maven-publish")
        plugin("com.gradleup.shadow")
    }

    dependencies {
        implementation("com.artillexstudios.axapi:axapi:1.4.830:all")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.3")
        compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
        compileOnly("org.incendo:cloud-paper:2.0.0-beta.14")
        compileOnly("net.kyori:adventure-platform-bukkit:4.4.1")
        compileOnly("org.apache.commons:commons-lang3:3.20.0")
        compileOnly("commons-io:commons-io:2.21.0")
        compileOnly("it.unimi.dsi:fastutil:8.5.18")
        compileOnly("me.clip:placeholderapi:2.11.7")
        compileOnly("com.h2database:h2:2.4.240")
        compileOnly("com.zaxxer:HikariCP:7.0.2")
        compileOnly("org.slf4j:slf4j-api:2.0.17")
        compileOnly("org.jooq:jooq:3.20.10")
        compileOnly("jakarta.xml.bind:jakarta.xml.bind-api:4.0.4")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        relocate("com.artillexstudios.axapi", "com.artillexstudios.axvanish.libs.axapi")
        relocate("org.incendo.cloud", "com.artillexstudios.axvanish.libs.cloud")
        relocate("com.github.benmanes", "com.artillexstudios.axvanish.libs.axapi.libs.caffeine")
        relocate("com.zaxxer", "com.artillexstudios.axvanish.libs.hikaricp")
        relocate("org.jooq", "com.artillexstudios.axvanish.libs.jooq")
        relocate("org.h2", "com.artillexstudios.axvanish.libs.h2")
    }
}
