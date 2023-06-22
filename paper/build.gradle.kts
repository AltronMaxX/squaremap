import io.papermc.hangarpublishplugin.model.Platforms
import xyz.jpenilla.runpaper.task.RunServer
import xyz.jpenilla.runtask.service.DownloadsAPIService

plugins {
  id("platform-conventions")
  id("io.papermc.paperweight.userdev")
  id("xyz.jpenilla.run-paper")
  id("io.papermc.hangar-publish-plugin")
}

val minecraftVersion = libs.versions.minecraft

dependencies {
  paperweight.devBundle("dev.folia", minecraftVersion.map { "$it-R0.1-SNAPSHOT" }.get())

  implementation(projects.squaremapCommon)

  implementation(libs.cloudPaper)
  implementation(libs.bStatsBukkit)
}

configurations.mojangMappedServer {
  exclude("org.yaml", "snakeyaml")
}

tasks {
  jar {
    manifest {
      attributes("squaremap-target-minecraft-version" to minecraftVersion.get())
    }
  }
  shadowJar {
    listOf(
      "cloud.commandframework",
      "io.leangen.geantyref",
      "org.bstats",
      "javax.inject",
      "com.google.inject",
      "org.aopalliance",
    ).forEach(::reloc)
  }
  reobfJar {
    outputJar.set(productionJarLocation(minecraftVersion))
  }
  processResources {
    filesMatching("plugin.yml") {
      filter { it.replace("1.20", "'1.20'") }
    val props = mapOf(
      "version" to project.version,
      "website" to providers.gradleProperty("githubUrl").get(),
      "description" to project.description,
      "apiVersion" to minecraftVersion.get().take(4),
    )
    inputs.properties(props)
    filesMatching("plugin.yml") {
      expand(props)
    }
  }
}

squaremapPlatform {
  productionJar.set(tasks.reobfJar.flatMap { it.outputJar })
}

bukkit {
  main = "xyz.jpenilla.squaremap.paper.SquaremapPaperBootstrap"
  name = rootProject.name
  apiVersion = "1.20"
  website = providers.gradleProperty("githubUrl").get()
  authors = listOf("jmp")
val foliaService = DownloadsAPIService.registerIfAbsent(project) {
  downloadsEndpoint = "https://api.papermc.io/v2/"
  downloadProjectName = "folia"
}
tasks.register<RunServer>("runFolia") {
  downloadsApiService.set(foliaService)
  version.set(minecraftVersion)
  pluginJars.from(squaremapPlatform.productionJar)
  group = "run paper"
}

hangarPublish.publications.register("plugin") {
  version.set(project.version as String)
  owner.set("jmp")
  slug.set("squaremap")
  channel.set("Release")
  changelog.set(releaseNotes)
  apiKey.set(providers.environmentVariable("HANGAR_UPLOAD_KEY"))
  platforms.register(Platforms.PAPER) {
    jar.set(squaremapPlatform.productionJar)
    platformVersions.add(minecraftVersion)
  }
}
