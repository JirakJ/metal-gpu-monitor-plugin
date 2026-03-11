buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.6.1")
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.intellij.platform)
    alias(libs.plugins.detekt)
    jacoco
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(providers.gradleProperty("javaVersion").get().toInt())
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion"),
        )
        instrumentationTools()
        pluginVerifier()
        zipSigner()
    }

    implementation(libs.jna)
    implementation(libs.jna.platform)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.mockk)
}

// Exclude IDE-provided libraries from plugin distribution
configurations {
    runtimeClasspath {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains", module = "annotations")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = providers.gradleProperty("pluginGroup")
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }

        vendor {
            name = "HTMETA.dev (Jakub Jirák)"
            email = "support@jakubjirak.com"
            url = "https://htmeta.dev"
        }
    }

    pluginVerification {
        ides {
            ide("IC", "2025.1")
        }
    }

    signing {
        // Configure via environment variables:
        // ORG_GRADLE_PROJECT_signCertificateChain
        // ORG_GRADLE_PROJECT_signPrivateKey
        // ORG_GRADLE_PROJECT_signPassword
    }

    publishing {
        token.set(System.getenv("PLUGIN_TOKEN"))
    }
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    wrapper {
        gradleVersion = "8.12"
    }
}

detekt {
    config.setFrom(files("detekt.yml"))
    buildUponDefaultConfig = true
}

tasks.register<proguard.gradle.ProGuardTask>("obfuscate") {
    dependsOn("jar")

    val jarFile = tasks.named<Jar>("jar").flatMap { it.archiveFile }
    val obfuscatedJar = layout.buildDirectory.file("obfuscated/${project.name}-${project.version}-obfuscated.jar")

    injars(jarFile)
    outjars(obfuscatedJar)

    // IntelliJ Platform classpath
    val platformClasspath = configurations.getByName("compileClasspath").files
    libraryjars(platformClasspath)

    // JDK modules
    val javaHome = System.getProperty("java.home")
    libraryjars(mapOf("jarfilter" to "!**.jar", "filter" to "!module-info.class"), "$javaHome/jmods/java.base.jmod")
    libraryjars(mapOf("jarfilter" to "!**.jar", "filter" to "!module-info.class"), "$javaHome/jmods/java.desktop.jmod")
    libraryjars(mapOf("jarfilter" to "!**.jar", "filter" to "!module-info.class"), "$javaHome/jmods/java.xml.jmod")

    // ---- Keep attributes ----
    keepattributes("SourceFile,LineNumberTable")
    keepattributes("*Annotation*")
    keepattributes("Signature")
    keepattributes("InnerClasses")
    keepattributes("Exceptions")

    // ---- Obfuscation settings ----
    optimizationpasses(3)
    allowaccessmodification()
    repackageclasses("")
    overloadaggressively()

    // Keep IntelliJ extension point implementations
    keep("public class * implements com.intellij.openapi.wm.StatusBarWidgetFactory { *; }")
    keep("public class * implements com.intellij.openapi.wm.ToolWindowFactory { *; }")
    keep("public class * implements com.intellij.openapi.components.PersistentStateComponent { *; }")
    keep("public class * implements com.intellij.openapi.options.Configurable { *; }")
    keep("public class * extends com.intellij.openapi.actionSystem.AnAction { *; }")
    keep("public class * extends com.intellij.openapi.startup.StartupActivity { *; }")
    keep("public class * extends com.intellij.openapi.startup.ProjectActivity { *; }")

    // Keep project/application services (looked up by class)
    keep("public class * { public static * getInstance(com.intellij.openapi.project.Project); }")
    keep("public class * { public static * getInstance(); }")

    // Keep plugin entry points
    keep(mapOf("allowobfuscation" to false), "class ${project.group}.** { void runActivity(com.intellij.openapi.project.Project, kotlin.coroutines.Continuation); }")

    // Keep State classes for XML serialization
    keepclassmembers("class * { ** INSTANCE; }")
    keepclassmembers("class * extends com.intellij.openapi.components.PersistentStateComponent { *; }")

    // Keep Kotlin metadata
    keepattributes("RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations")
    keep("class kotlin.Metadata { *; }")

    // Keep resource bundle references
    keepclassmembers("class * { static java.util.ResourceBundle getBundle(...); }")

    // Keep JNA interfaces and callbacks
    keep("public interface * extends com.sun.jna.Library { *; }")
    keep("public class * extends com.sun.jna.Structure { *; }")
    keep("public interface * extends com.sun.jna.Callback { *; }")

    // Don't warn about platform classes
    dontwarn("com.intellij.**")
    dontwarn("org.jetbrains.**")
    dontwarn("kotlin.**")
    dontwarn("kotlinx.**")
    dontwarn("com.sun.jna.**")

    // Verbose output
    printmapping(layout.buildDirectory.file("proguard/mapping.txt"))
    printseeds(layout.buildDirectory.file("proguard/seeds.txt"))
    printusage(layout.buildDirectory.file("proguard/usage.txt"))
}
