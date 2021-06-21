plugins {
    kotlin("jvm") version "1.5.10"
    id("fabric-loom") version "0.9.9"
}

val modId: String by project
val modVersion: String by project
val minecraftVersion: String by project

project.group = "com.romangraef"
version = "$minecraftVersion-$modVersion"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

base {
    archivesBaseName = modId + "-test"
}



repositories {
    maven(url = "https://maven.fabricmc.net/")
    maven(url = "https://maven.terraformersmc.com/releases")
    maven(url = "https://kotlin.bintray.com/kotlinx")
    mavenCentral()
}

minecraft {

}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = minecraftVersion)
    mappings(group = "net.fabricmc", name = "yarn", version = minecraftVersion + "+build.1", classifier = "v2")

    modImplementation("net.fabricmc:fabric-loader:0.11.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.34.9+1.17")
    modImplementation(group = "net.fabricmc", name = "fabric-language-kotlin", version = "1.6.1+kotlin.1.5.10")

    modImplementation("com.terraformersmc:modmenu:2.0.2")
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(project.properties)
    }
}