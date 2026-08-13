plugins {
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("java")
}

group = "io.github.Chamogelastos.nightvision"
version = "1.0.0"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}

tasks {
    runServer {
        minecraftVersion("26.2")
    }
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}