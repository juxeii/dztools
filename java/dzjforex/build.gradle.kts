group = "com.jforex.dzplugin"
version = "0.9.6"

description = """
The Java plugin part for Zorro which lets you trade with Dukascopy
Project name: ${project.name}
"""

plugins {
    kotlin("jvm") version "1.3.10"
    jacoco
    `maven-publish`
}

repositories {
    mavenLocal()
    jcenter()
    maven(url = "https://www.dukascopy.com/client/jforexlib/publicrepo")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("com.dukascopy.dds2:DDS2-jClient-JForex:3.4.13")
    compile("com.jforex:KForexUtils:0.2.0-SNAPSHOT")
    compile("org.aeonbits.owner:owner:1.0.10")
}

val jarFileName = "${project.name}-$version"
val zorroPath: String by project
val pluginFolder = "${buildDir}/Plugin"
val zorroDukascopyFolder = "${zorroPath}/Plugin/dukascopy"

tasks.withType<Jar> {
    baseName = project.name

    manifest.attributes.apply {
        put("Implementation-Title", "${project.name}")
        put("Implementation-Version", "$version")
        put("Class-Path", ". ${configurations.runtime.get().map { "lib/${it.name}"  }.joinToString(separator = " ")}")
    }
}

tasks.register<Zip>("createDeployZip") {
    dependsOn("createPluginFolder")
    baseName = project.name
    from(pluginFolder)
}

tasks.create("createPluginFolder") {
    outputs.upToDateWhen{ false }
    dependsOn("jar")

    val dukascopyFolder = "${pluginFolder}/dukascopy"
    val configFolder = "src/main/config"
    File(pluginFolder).deleteRecursively()
    copy {
        from("../../c++/Release/dukascopy.dll")
        into(pluginFolder)
    }
    copy {
        from("${buildDir}/libs/${jarFileName}.jar")
        into(dukascopyFolder)
    }
    copy {
        from("${configFolder}/.")
        into(dukascopyFolder)
    }
    copy {
        from(configurations.runtime)
        into("${dukascopyFolder}/lib")
    }
}

tasks.register<Copy>("copyPluginFolderToZorro") {
    outputs.upToDateWhen{ false }
    dependsOn("createPluginFolder")
    File(zorroDukascopyFolder).deleteRecursively()
    from(pluginFolder)
    into("${zorroPath}/Plugin")
}