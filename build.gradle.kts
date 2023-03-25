import groovy.json.JsonSlurper

plugins {
    java
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    val json = JsonSlurper().parseText(file("src/main/resources/plugin.json").readText()) as Map<*, *>
    project.version = json["version"]!!

    val mindustryVersion = json["minGameVersion"]
    val usefulHash = "8385a75c52"

    compileOnly("com.github.Anuken.Arc:arc-core:v$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:v$mindustryVersion")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.discord4j:discord4j-core:3.2.3")
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.9.0")

    implementation("com.github.xzxADIxzx.useful-stuffs:antiddos:$usefulHash")
    implementation("com.github.xzxADIxzx.useful-stuffs:bundle:$usefulHash")
    implementation("com.github.xzxADIxzx.useful-stuffs:cooldowns:$usefulHash")
    implementation("com.github.xzxADIxzx.useful-stuffs:effect:$usefulHash")
    implementation("com.github.xzxADIxzx.useful-stuffs:menu:$usefulHash")
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}