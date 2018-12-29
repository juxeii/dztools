import org.gradle.internal.impldep.org.apache.ivy.osgi.util.ZipUtil.zip

group = "com.jforex.dzplugin"
version = "0.9.6"

description = """
The Java plugin part for Zorro which lets you trade with Dukascopy
Project name: ${project.name}
"""

plugins {
    kotlin("jvm") version "1.3.11"
    jacoco
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
    //jcenter()
    maven(url = "https://www.dukascopy.com/client/jforexlib/publicrepo")
}

val arrowVersion = "0.8.1"
dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("com.dukascopy.dds2:DDS2-jClient-JForex:3.4.13")
    compile("com.jforex:KForexUtils:0.2.0-SNAPSHOT")
    compile("org.aeonbits.owner:owner:1.0.10")
}

val jarFileName = "${project.name}-$version"
val zorroPath: String by project
val deployFolder = "${buildDir}/zorro"
val historyFolder = "${deployFolder}/History"
val pluginFolder = "${deployFolder}/Plugin"
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
    dependsOn("jar")
    dependsOn("createZorroFolders")
    baseName = project.name

    from(deployFolder)
}

tasks.create("createZorroFolders") {
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
        from("${configFolder}/."){
            exclude ("AssetsDukascopy.csv", "zorroDukascopy.bat")
        }
        into(dukascopyFolder)
    }
    copy {
        from(configurations.runtime)
        into("${dukascopyFolder}/lib")
    }

    File(historyFolder).deleteRecursively()
    copy {
        from("${configFolder}/AssetsDukascopy.csv")
        into(historyFolder)
    }

    copy {
        from("${configFolder}/zorroDukascopy.bat")
        into(deployFolder)
    }
}

tasks.register<Copy>("copyFoldersToZorro") {
    outputs.upToDateWhen{ false }

    dependsOn("createZorroFolders")
    File(zorroDukascopyFolder).deleteRecursively()
    copy {
        from(deployFolder)
        into("${zorroPath}")
    }
}