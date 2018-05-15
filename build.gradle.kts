import org.gradle.internal.impldep.org.apache.maven.model.DependencyManagement
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.2.41"
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))

    // Framework stuff
    compile("io.javalin:javalin:1.6.1") {
        exclude("org.jetbrains.kotlin")
        exclude("org.slf4j")
    }
    compile("io.github.config4k:config4k:0.3.2") {
        exclude("org.jetbrains.kotlin")
    }
    compile("com.google.code.gson:gson:2.8.4")

    // DB
    compile("com.zaxxer:HikariCP:3.1.0") {
        exclude("org.slf4j")
    }
    compile("org.postgresql:postgresql:42.2.2")

    val jdbiVersion = "3.2.0"
    compile("org.jdbi:jdbi3-core:$jdbiVersion") {
        exclude("org.slf4j")
    }
    compile("org.jdbi:jdbi3-postgres:$jdbiVersion") {
        exclude("org.jdbi", "jdbi3-core")
        exclude("org.postgresql", "postgresql")
    }
    compile("org.jdbi:jdbi3-kotlin-sqlobject:$jdbiVersion") {
        exclude("org.jdbi", "jdbi3-core")
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains", "annotations")
    }

    // Logging
    compile("org.slf4j:slf4j-api:1.7.25")
    compile("ch.qos.logback:logback-classic:1.2.3") {
        exclude("org.slf4j")
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.allWarningsAsErrors = true
}
