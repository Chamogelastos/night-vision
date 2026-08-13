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
    maven {
        name = "eldonexus"
        url = uri("https://eldonexus.de/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly("net.strokkur.commands:annotations-paper:2.1.4")
    annotationProcessor("net.strokkur.commands:processor-paper:2.1.4")
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