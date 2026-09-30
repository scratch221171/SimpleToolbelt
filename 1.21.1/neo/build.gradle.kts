import net.scratch221171.mdk.build.req

plugins {
    id("neoforge-mod-conventions")
    id("neoforge-config-conventions")
}

val curiosVersion: String by project

// Mod Dependencies
dependencies {
    compileOnly(libs.curios.neoforge) {
        version { require(curiosVersion) }
        artifact { classifier = "api" }
    }
    implementation(libs.curios.neoforge, req(curiosVersion))
    ciRuntimeMods(libs.curios.neoforge, req(curiosVersion))
}
