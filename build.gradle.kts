import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}


repositories {
    mavenCentral()

    maven {
        url = uri("https://rutgerkok.nl/repo")
    }

    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }

    maven {
        url = uri("https://ci.mg-dev.eu/plugin/repository/everything")
    }

    maven {
        url = uri("https://repo.onarandombox.com/content/groups/public/")
    }

    maven {
        url = uri("https://repo.codemc.io/repository/nms/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    testImplementation("junit:junit:4.13.1")
    paperweight.paperDevBundle("1.21.5-no-moonrise-SNAPSHOT")
    compileOnly("com.bergerkiller.bukkit:MyWorlds:1.12-v2") {
        exclude(group="org.spigotmc") // Don't let Spigot API overwrite Paper
    }
    compileOnly("com.griefcraft:LWC:4.3.2")
    compileOnly("org.mvplugins.multiverse.core:multiverse-core:5.0.2")
    compileOnly("org.mvplugins.multiverse.inventories:multiverse-inventories:5.0.2")
    compileOnly("me.drayshak.worldinventories:WorldInventories:1.7.0")
    compileOnly("nl.rutgerkok:blocklocker:1.9.2")
    compileOnly("org.yi.acru.bukkit:Lockette:1.7.12")
    compileOnly("uk.co.tggl.pluckerpluck.multiinv:MultiInv:3.3.0")
}

group = "nl.rutgerkok.betterenderchest"
version = "2.7.3-custom"
description = "BetterEnderChest"
java.sourceCompatibility = JavaVersion.VERSION_21

// For supporting Spigot
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION
tasks.assemble {
    dependsOn(tasks.reobfJar)
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
