plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
}

// OneDrive marks generated directories as reparse points, which makes Gradle clean unreliable.
// Keep all disposable build state in a local ASCII-only path instead of the source tree.
val externalBuildRootPath = providers.environmentVariable("MATHOLIC_BUILD_ROOT")
    .orElse(
        providers.environmentVariable("LOCALAPPDATA")
            .map { "$it/CodexBuild/matholic-kiosk" },
    )
val externalBuildRoot = layout.dir(externalBuildRootPath.map { file(it) })

layout.buildDirectory.set(externalBuildRoot.map { it.dir("root") })
subprojects {
    layout.buildDirectory.set(externalBuildRoot.map { it.dir(name) })
}
