import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL

version = 1.0

tasks {

    // Use this task to upgrade the Gradle version in order to keep
    // gradle-wrapper.properties in sync.
    named<Wrapper>("wrapper").configure {
        gradleVersion = "8.5"
        distributionType = ALL
    }
}
