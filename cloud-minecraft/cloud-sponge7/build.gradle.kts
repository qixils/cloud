plugins {
    id("cloud.base-conventions")
}

dependencies {
    api(projects.cloudCore)
    compileOnly(libs.spongeApi7)
}
