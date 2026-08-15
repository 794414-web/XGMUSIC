buildscript {
    ext {
        compile_sdk = 35
        min_sdk = 24
        target_sdk = 35
        kotlin_version = "2.0.21"
        compose_version = "1.7.3"
        hilt_version = "2.52"
        room_version = "2.6.1"
        exoplayer_version = "1.3.1"
        quickjs_version = "1.0.0"
        okhttp_version = "4.12.0"
        coil_version = "3.0.0-rc01"
        nav_version = "2.8.4"
        datastore_version = "1.1.1"
        retrofit_version = "2.11.0"
        kotlinx_serialization_version = "1.7.3"
        accompanist_version = "0.36.0"
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
