import groovy.json.JsonSlurper

plugins {
    java
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    val json = JsonSlurper().parseText(file("src/main/resources/plugin.json").readText()) as Map<*, *>
    project.version = json["version"]!!

    val mindustryVersion = json["minGameVersion"]
    val usefulHash = "c48df39e17"

    compileOnly("com.github.Anuken.Arc:arc-core:v$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:v$mindustryVersion")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("net.dv8tion:JDA:5.0.0-beta.3") {
        exclude(module = "opus-java")
    }

    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.8.2")

    implementation(platform("io.projectreactor:reactor-bom:2020.0.24"))
    implementation(("io.projectreactor:reactor-core"))

    implementation("com.github.xzxADIxzx.useful-stuffs:server-antiddos:$usefulHash")
    implementation("com.github.xzxADIxzx.useful-stuffs:server-bundle:$usefulHash")
    implementation("com.github.xzxADIxzx.useful-stuffs:server-menus:$usefulHash")
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "darkdustry"
            artifactId = "DarkdustryPLugin"
            version = project.version.toString()

            afterEvaluate {
                from(components["java"])
            }
        }
    }
}