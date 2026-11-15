plugins {
    java
    id("net.weavemc.gradle") version "1.0.0-PRE3"
}

group = "dev.yaclient"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.weavemc.dev/releases")
}

weave {
    configure {
        name = "YA Client"
        modId = "yaclient"
        entryPoints = listOf("dev.yaclient.YAClient")
        mcpMappings()
    }
    version("1.8.9")
}

dependencies {
    compileOnly("com.github.Weave-MC:Weave-Loader:v0.2.4")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.jar {
    archiveFileName.set("YAClient.jar")
}
