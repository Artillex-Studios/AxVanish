plugins {
    id("java")
    id("com.gradleup.shadow") version("8.3.6")
}

group = "com.artillexstudios.axvanish"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()

        maven("https://jitpack.io/")
        maven("https://repo.artillex-studios.com/releases/")
        maven("https://repo.codemc.org/repository/maven-public/")
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
        plugin("com.gradleup.shadow")
    }

    dependencies {
        implementation("com.artillexstudios.axapi:axapi:1.4.677:all")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.0")
        compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
        compileOnly("dev.jorel:commandapi-bukkit-shade:10.0.1")
        compileOnly("org.apache.commons:commons-lang3:3.17.0")
        compileOnly("commons-io:commons-io:2.19.0")
        compileOnly("it.unimi.dsi:fastutil:8.5.15")
        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("com.h2database:h2:2.3.232")
        compileOnly("com.zaxxer:HikariCP:6.3.0")
        compileOnly("org.slf4j:slf4j-api:2.0.17")
        compileOnly("org.jooq:jooq:3.20.4")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        relocate("com.artillexstudios.axapi", "com.artillexstudios.axvanish.libs.axapi")
        relocate("dev.jorel.commandapi", "com.artillexstudios.axvanish.libs.commandapi")
        relocate("com.github.benmanes", "com.artillexstudios.axvanish.libs.axapi.libs.caffeine")
        relocate("com.zaxxer", "com.artillexstudios.axvanish.libs.hikaricp")
        relocate("org.jooq", "com.artillexstudios.axvanish.libs.jooq")
        relocate("org.h2", "com.artillexstudios.axvanish.libs.h2")
    }
}