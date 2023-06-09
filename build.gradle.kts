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
    val usefulHash = "798e49b33b"

    compileOnly("com.github.anuken.arc:arc-core:v$mindustryVersion")
    compileOnly("com.github.anuken.mindustryjitpack:core:v$mindustryVersion")
    compileOnly("com.github.anuken.mindustryjitpack:server:v$mindustryVersion")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.mongodb:mongodb-driver-sync:4.9.0")

    implementation("com.discord4j:discord4j-core:3.2.3")

    implementation("org.jline:jline-reader:3.21.0")
    implementation("org.jline:jline-console:3.21.0")
    implementation("org.jline:jline-terminal-jna:3.21.0")

    implementation("com.github.xzxadixzx.useful-stuffs:antiddos:$usefulHash")
    implementation("com.github.xzxadixzx.useful-stuffs:bundle:$usefulHash")
    implementation("com.github.xzxadixzx.useful-stuffs:cooldowns:$usefulHash")
    implementation("com.github.xzxadixzx.useful-stuffs:database:$usefulHash")
    implementation("com.github.xzxadixzx.useful-stuffs:effect:$usefulHash")
    implementation("com.github.xzxadixzx.useful-stuffs:menu:$usefulHash")

    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}