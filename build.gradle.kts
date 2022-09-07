import groovy.json.JsonSlurper

plugins {
    java
}

repositories {
    mavenCentral()
    maven(url = "https://www.jitpack.io")
}

dependencies {
    val json = JsonSlurper().parseText(file("src/main/resources/plugin.json").readText()) as Map<*, *>
    val mindustryVersion = json["minGameVersion"]!!
    project.version = json["version"]!!

    compileOnly("com.github.Anuken.Arc:arc-core:$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:$mindustryVersion")

    implementation("com.google.code.gson:gson:2.9.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.18.0")
    implementation("net.dv8tion:JDA:5.0.0-alpha.18")
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.7.1")

    implementation(platform("io.projectreactor:reactor-bom:2020.0.22"))
    implementation(("io.projectreactor:reactor-core"))
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
