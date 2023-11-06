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
    val usefulHash = "2f1d9eb899"

    compileOnly("com.github.anuken.arc:arc-core:v$mindustryVersion")
    compileOnly("com.github.anuken.mindustryjitpack:core:v$mindustryVersion") {
        exclude(group = "com.github.Anuken.Arc")
    }

    compileOnly("com.github.anuken.mindustryjitpack:server:v$mindustryVersion") {
        exclude(group = "com.github.Anuken.Arc")
    }

    implementation("com.github.osp54:Sock:ed404a96ff")
    implementation("dev.morphia.morphia:morphia-core:2.3.8")

    implementation("com.discord4j:discord4j-core:3.2.6")
    runtimeOnly("io.netty:netty-transport-native-epoll::linux-aarch_64")

    implementation("org.jline:jline-reader:3.21.0")
    implementation("org.jline:jline-console:3.21.0")
    implementation("org.jline:jline-terminal-jna:3.21.0")

    implementation("com.github.xzxadixzx.useful-stuffs:bundle:$usefulHash")
    implementation("com.github.xzxadixzx.useful-stuffs:collections:$usefulHash")
    implementation("com.github.xzxadixzx.useful-stuffs:commands:$usefulHash")
    implementation("com.github.xzxadixzx.useful-stuffs:config:$usefulHash")
    implementation("com.github.xzxadixzx.useful-stuffs:effect:$usefulHash")
    implementation("com.github.xzxadixzx.useful-stuffs:menu:$usefulHash")
    implementation("com.github.xzxadixzx.useful-stuffs:security:$usefulHash")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}