plugins {
  id("java-library")
  id("net.kyori.indra")
  id("net.kyori.indra.git")
}

group = rootProject.group
version = rootProject.version
description = rootProject.description

indra {
  javaVersions {
    minimumToolchain(17)
    target(17)
  }
}

repositories {
  mavenCentral()
  maven("https://repo.jpenilla.xyz/snapshots/") {
    mavenContent {
      includeGroup("cloud.commandframework")
      includeGroup("xyz.jpenilla")
      includeModule("net.kyori", "adventure-platform-fabric") // TODO remove when updating platform-fabric to release
      snapshotsOnly()
    }
  }
  sonatype.s01Snapshots()
  sonatype.ossSnapshots()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://maven.fabricmc.net/") {
    mavenContent { includeGroup("net.fabricmc") }
  }
}
