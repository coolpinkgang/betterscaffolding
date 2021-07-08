import com.modrinth.minotaur.TaskModrinthUpload

plugins {
    kotlin("jvm") version "1.5.10"
    id("fabric-loom") version "0.9.9"
    id("com.github.eutro.hierarchical-lang") version "1.1.3"
    id("com.modrinth.minotaur") version "1.2.1"
    id("com.matthewprenger.cursegradle") version "1.4.0"
}

val modId: String by project
val modVersion: String by project
val minecraftVersion: String by project
val modrinthToken: String? by project
val curseforgeToken: String? by project
val modrinthId: String by project
val modVersionName: String? by project
val curseforgeId: String by project

val changelogTxt = project.rootDir.resolve("CHANGELOG.txt").readText().split("\n====+\n".toRegex()).last().trim()

project.group = "com.romangraef"
version = "$minecraftVersion-$modVersion"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


repositories {
    maven(url = "https://maven.fabricmc.net/")
    maven(url = "https://maven.terraformersmc.com/releases")
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://maven.shedaniel.me/")
    mavenCentral()
}

minecraft {

}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = minecraftVersion)
    mappings(group = "net.fabricmc", name = "yarn", version = minecraftVersion + "+build.1", classifier = "v2")

    modImplementation("net.fabricmc:fabric-loader:0.11.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.36.0+1.17")
    modImplementation(group = "net.fabricmc", name = "fabric-language-kotlin", version = "1.6.1+kotlin.1.5.10")

    modImplementation("com.terraformersmc:modmenu:2.0.2") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modApi("me.shedaniel.cloth:cloth-config-fabric:5.0.34") {
        exclude(group = "net.fabricmc.fabric-api")
    }
}


tasks.create("printChangelog") {
    doLast {
        print(changelogTxt)
    }
}
curseforge {
    options(closureOf<com.matthewprenger.cursegradle.Options> {
        forgeGradleIntegration = false
    })
    this.project(closureOf<com.matthewprenger.cursegradle.CurseProject> {
        id = curseforgeId
        print("c:$curseforgeToken")

        apiKey = if (project.properties.getOrDefault("curse", "no") == "confirm") (curseforgeToken ?: "") else ""
        changelog = changelogTxt
        addGameVersion("Fabric")
        addGameVersion("1.17")
        addGameVersion("Java 16")
        releaseType = "release"

        relations(closureOf<com.matthewprenger.cursegradle.CurseRelation> {
            requiredDependency("fabric-api")
            optionalDependency("modmenu")
            requiredDependency("cloth-config")
            requiredDependency("fabric-language-kotlin")
        })
        afterEvaluate {
            mainArtifact(tasks.getByName("remapJar"), closureOf<com.matthewprenger.cursegradle.CurseArtifact> {
                displayName = modVersionName
            })
            uploadTask.dependsOn("remapJar")
        }
    })
}
tasks.create<TaskModrinthUpload>("publishModrinth") {
    onlyIf {
        modrinthToken != null && project.properties.getOrDefault("modrinth", "no") == "confirm"
    }
    projectId = modrinthId
    token = modrinthToken
    changelog = changelogTxt
    versionName = modVersionName
    uploadFile = tasks.getByName("remapJar")
    dependsOn("remapJar")
    versionNumber = modVersion
    addGameVersion("1.17")
    addLoader("fabric")
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(project.properties)
    }
}
