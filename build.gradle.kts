buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("com.github.johnrengelman:shadow:8.1.1")
        classpath("org.ow2.asm:asm:9.6")
        classpath("org.ow2.asm:asm-commons:9.6")
    }
}

plugins {
    java
}

allprojects {
    group = "net.lyndara"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    dependencies {
        implementation("com.zaxxer:HikariCP:6.0.0")
        implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")
        implementation("org.slf4j:slf4j-api:2.0.12")
    }

    tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")

        relocate("com.zaxxer.hikari", "net.lyndara.libs.hikari")
        relocate("org.mariadb.jdbc", "net.lyndara.libs.mariadb")
    }

    tasks.withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}