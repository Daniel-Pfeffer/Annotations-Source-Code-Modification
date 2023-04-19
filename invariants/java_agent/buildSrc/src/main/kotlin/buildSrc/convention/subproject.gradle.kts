package buildSrc.convention

if (project != rootProject) {
    group = rootProject.group
    version = rootProject.version
}

description = "Common settings for all Invariant subproject"