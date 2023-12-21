# install4j-incremental-build-reproducer
Uses the Install4J Gradle plugin to create an installer, demonstrating the plugin does not support Gradle's incremental building or configuration cache.

## To test support for incremental building:
1. Execute the Gradle task to build the installer:  
`.\gradlew.bat install4j` (Windows)  
`./gradlew install4j` (Unix/Mac OS)  
2. Note that two tasks are executed (`unzipData` and `install4j`):
```
BUILD SUCCESSFUL in 36s
2 actionable tasks: 2 executed
Configuration cache entry discarded with 21 problems.
```
3. Without modifying any files, execute the Gradle task to build the installer (see above)
4. Note that one task is executed (`install4j`) and one task is up to date (`unzipData`):
```
BUILD SUCCESSFUL in 8s
2 actionable tasks: 1 executed, 1 up-to-date
Configuration cache entry discarded with 21 problems.
```

## To test support for configuration caching:
1. Execute the Gradle task to build the installer:  
   `.\gradlew.bat install4j` (Windows)  
   `./gradlew install4j` (Unix/Mac OS)
2. Note the Gradle outputs relating to configuration cache as follows:
```
Configuration on demand is an incubating feature.
Calculating task graph as no cached configuration is available for tasks: install4j
```
[Install4J output...]
``` 
21 problems were found storing the configuration cache, 1 of which seems unique.
- Task `:installer-module:install4j` of type `com.install4j.gradle.Install4jTask`: invocation of 'Task.project' at execution time is unsupported.
  See https://docs.gradle.org/8.5/userguide/configuration_cache.html#config_cache:requirements:use_project_during_execution

See the complete report at file:///C:/Dev/Projects/SCM/Demos/install4j-incremental-build-reproducer/build/reports/configuration-cache/ds1t8nxbwl2k8qey7c8k3hb81/2qcvkplkib9algn86qwdyjz3i/configuration-cache-report.html
```
3. Note also the `install4j` task in the [./installer-mode/build.gradle.kts](./installer-module/build.gradle.kts?plain=L87) build script uses `notCompatibleWithConfigurationCache`.  
```
notCompatibleWithConfigurationCache(
    "'Install4jTask' invokes 'Task.project' at execution time."
)
```
Removing the call to `notCompatibleWithConfigurationCache` will cause the task to fail:
```
> Task :installer-module:install4j FAILED

FAILURE: Build completed with 2 failures.

1: Task failed with an exception.
-----------
* What went wrong:
Execution failed for task ':installer-module:install4j'.
> Extension with name 'install4j' does not exist. Currently registered extension names: [ext]

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.
==============================================================================

2: Task failed with an exception.
-----------
* What went wrong:
Configuration cache problems found in this build.

1 problem was found storing the configuration cache.
- Task `:installer-module:install4j` of type `com.install4j.gradle.Install4jTask`: invocation of 'Task.project' at execution time is unsupported.
  See https://docs.gradle.org/8.5/userguide/configuration_cache.html#config_cache:requirements:use_project_during_execution

See the complete report at file:///C:/Dev/Projects/SCM/Demos/install4j-incremental-build-reproducer/build/reports/configuration-cache/ds1t8nxbwl2k8qey7c8k3hb81/ex4sfymyivsu6j8jcbo26ysa5/configuration-cache-report.html
> Invocation of 'Task.project' by task ':installer-module:install4j' at execution time is unsupported.

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.
==============================================================================

BUILD FAILED in 28s
2 actionable tasks: 1 executed, 1 up-to-date
Configuration cache entry discarded with 1 problem.
```
