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
    val mindustryVersion = json["minGameVersion"]!!
    project.version = json["version"]!!

    compileOnly("com.github.Anuken.Arc:arc-core:v$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:v$mindustryVersion")

    implementation("com.google.code.gson:gson:2.10")
    implementation("net.dv8tion:JDA:5.0.0-alpha.22")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.7.1")

    implementation(platform("io.projectreactor:reactor-bom:2020.0.24"))
    implementation(("io.projectreactor:reactor-core"))

    implementation("com.github.xzxADIxzx.useful-stuffs:server-bundle:main-SNAPSHOT")
    implementation("com.github.xzxADIxzx.useful-stuffs:server-menus:main-SNAPSHOT")
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