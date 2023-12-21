import com.install4j.gradle.Install4jTask

/**
 * This module uses Install4J to create an installer for data resources used by other applications.
 */

plugins {
    id("base")
    id("com.install4j.gradle") version "10.0.6"
}

version = rootProject.version

description = "Basic Data Module"

val dataScopeConfigurationName = "dataScope"
val dataPathConfigurationName = "dataPath"

@Suppress("UnstableApiUsage")
configurations {
    // Configuration used to access the data hosted on Nexus.
    dependencyScope(dataScopeConfigurationName)

    resolvable(dataPathConfigurationName) {

        extendsFrom(configurations[dataScopeConfigurationName])
    }
}

val dataScope: Configuration = configurations[dataScopeConfigurationName]
val dataPath: Configuration = configurations[dataPathConfigurationName]

val install4jHomeProjectProperty: Any? = project.properties["install4j.home"]
val isInstall4jHomeProjectPropertySet = install4jHomeProjectProperty != null &&
        install4jHomeProjectProperty.toString().trim().isNotEmpty()
if (!isInstall4jHomeProjectPropertySet)
    logger.info(
        "Project property 'install4j.home' is not set, trying INSTALL4J_HOME environment"
    )

val install4jHomeSystemProperty: String? = System.getenv("INSTALL4J_HOME")
val isInstall4jHomeSystemPropertySet = install4jHomeSystemProperty != null &&
        install4jHomeSystemProperty.toString().trim().isNotEmpty()
if (!isInstall4jHomeSystemPropertySet)
    logger.info(
        "Environment property 'INSTALL4J_HOME' not set, using the current directory."
    )

// Resolve the path to the Install4J installation directory
val install4jHomeDirectory = file(
    if (isInstall4jHomeProjectPropertySet) {
        install4jHomeProjectProperty.toString().trim()
    } else if (isInstall4jHomeSystemPropertySet) {
        install4jHomeSystemProperty.toString().trim()
    } else {
        "."
    }
)

dependencies {
    // Don't add ZIP files as "runtimeOnly" dependencies because they'll get
    // included in the Class-Path.
    dataScope(
        rootProject.layout.projectDirectory.dir("dependencies").asFileTree
    )
}

install4j {
    installDir = install4jHomeDirectory
}

tasks {

    val dataDir = project.layout.buildDirectory.dir(
        "installer/data"
    ).get().asFile.absolutePath

    val unzipData by registering(Copy::class) {
        from(provider { dataPath.map { zipTree(it) } })
        into(dataDir)
    }

    named<Install4jTask>("install4j").configure {
        group = "build"
        description = "Creates an installer to deploy data used by other applications."

        notCompatibleWithConfigurationCache(
            "'Install4jTask' invokes 'Task.project' at execution time."
        )

        dependsOn(unzipData)

        val configFile: File = file("data-resources.install4j")

        val installerOutputDir = project.layout.buildDirectory.dir(
            "installer/executables"
        ).get().asFile.absolutePath

        val compilerVariables: Map<Any, Any> = mapOf(
            "installerOutputDir" to installerOutputDir,
            "dataDir" to dataDir,
            "dataInstallerBaseName" to "Basic-Data",
            "dataVersion" to project.version,
        )

        projectFile = configFile
        // Variable name/value pairs to be resolved by the Install4J compiler
        variables = compilerVariables.toMutableMap()

        // I do not expect the need to declare input/output dependencies for
        // up-to-date checking but including them here to eliminate the
        // possibility of not declaring them as being the culprit

        inputs.property("install4jHomeDirectory", install4jHomeDirectory)

        compilerVariables.forEach { entry ->
            inputs.property("${entry.key}", "${entry.value}")
        }

        inputs.file(configFile)
            .withPathSensitivity(PathSensitivity.RELATIVE)

        inputs.dir(dataDir)

        outputs.dir(installerOutputDir)
    }
}
