group = rootProject.group
version = rootProject.version

dependencies {
    implementation("com.artillexstudios.axapi:axapi:1.4.830:all")
}

tasks {
    publish {
        dependsOn(shadowJar)
    }

    shadowJar {
        relocate("com.artillexstudios.axapi", "com.artillexstudios.axvanish.libs.axapi")
    }
}

publishing {
    repositories {
        maven {
            name = "Artillex-Studios"
            url = uri("https://repo.artillex-studios.com/releases/")
            credentials(PasswordCredentials::class) {
                username = project.properties["maven_username"].toString()
                password = project.properties["maven_password"].toString()
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = "axvanish"

            from(components["shadow"])
        }
    }
}
