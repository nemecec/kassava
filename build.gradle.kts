buildscript {
  // Dokka pulls Jackson 2.15.3 onto two separate classpaths, and GitHub flags it for
  // seven advisories on both. This is the plugin's own classpath; the generator worker
  // classpath is pinned further down. Build-time only either way - Jackson never
  // reaches the published artifact, whose sole dependency is kotlin-stdlib - but
  // pinning keeps the alert list meaningful.
  //
  // 2.18.9 is deliberate rather than "the newest": every one of these advisories has a
  // second vulnerable range covering 2.19.0 up to 2.21.4/2.21.5, so moving to a newer
  // 2.19.x or 2.20.x would leave all seven open, and 2.21.5+ is not on Maven Central
  // yet. The lower bound of GHSA-5jmj-h7xm-6q6v is what makes it .9 and not .8.
  configurations.classpath {
    resolutionStrategy.eachDependency {
      if (requested.group.startsWith("com.fasterxml.jackson")) {
        useVersion("2.18.9")
        because(
          "GHSA-r7wm-3cxj-wff9, GHSA-rmj7-2vxq-3g9f, GHSA-j3rv-43j4-c7qm, " +
            "GHSA-72hv-8253-57qq, GHSA-hgj6-7826-r7m5, GHSA-3pjw-73gf-8qr5, " +
            "GHSA-5jmj-h7xm-6q6v"
        )
      }
    }
  }
}

import aQute.bnd.gradle.BundleTaskExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.net.URI

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.bnd)
  alias(libs.plugins.dokka)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.spotless)
}

// Dokka resolves its generator worker classpath (dokkaHtmlGeneratorRuntime) as a
// project configuration, so the buildscript pin above does not reach it. Same Jackson
// advisories, same target version - see the comment in `buildscript` for why 2.18.9.
configurations.configureEach {
  resolutionStrategy.eachDependency {
    if (requested.group.startsWith("com.fasterxml.jackson")) {
      useVersion("2.18.9")
      because(
        "GHSA-r7wm-3cxj-wff9, GHSA-rmj7-2vxq-3g9f, GHSA-j3rv-43j4-c7qm, " +
          "GHSA-72hv-8253-57qq, GHSA-hgj6-7826-r7m5, GHSA-3pjw-73gf-8qr5, " +
          "GHSA-5jmj-h7xm-6q6v"
      )
    }
  }
}

group = "dev.nemecec.kassava"
version = "3.0.0"
description =
  "Kotlin extension functions for implementing toString(), equals() and hashCode() " +
    "without all of the boilerplate."

java {
  // Java 21 compiles and runs the tests; the published bytecode targets Java 8 (see
  // below). The toolchain is auto-provisioned via the foojay resolver in
  // settings.gradle.kts when no JDK 21 is installed.
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

dependencies {
  // No implementation dependencies: the library only uses kotlin-stdlib (which the
  // Kotlin plugin adds) and java.util.Objects. KProperty1 - the only Kotlin
  // reflection type in the public API - lives in kotlin-stdlib, and the property
  // references callers pass in resolve their name and getter without kotlin-reflect.

  testImplementation(kotlin("test"))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
  // Explicit API mode keeps the published surface deliberate: every exported
  // declaration spells out its visibility and return type.
  explicitApi()
}

// Target Java 8 bytecode for the published classes, and reject JDK APIs newer than
// Java 8 while compiling them. The test sources build against the toolchain's own
// JDK, so they are free to use newer APIs.
tasks.named<KotlinJvmCompile>("compileKotlin") {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_1_8)
    freeCompilerArgs.add("-Xjdk-release=1.8")
  }
}

// There are no Java sources, but the task's target has to agree with the Kotlin one
// or the Kotlin plugin's JVM-target validation fails the build.
tasks.named<JavaCompile>("compileJava") {
  options.release.set(8)
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed")
    setExceptionFormat("full")
  }
}

tasks.jar {
  // OSGi bundle headers plus an Automatic-Module-Name, so the jar is usable as-is
  // from OSGi containers and as an automatic JPMS module. Both names match the
  // single exported package.
  val bundle = extensions.getByType(BundleTaskExtension::class.java)
  bundle.setBnd(
    """
    Bundle-SymbolicName: dev.nemecec.kassava
    Bundle-Name: Kassava
    Bundle-Description: ${project.description}
    Automatic-Module-Name: dev.nemecec.kassava
    Export-Package: dev.nemecec.kassava
    -removeheaders: Private-Package
    """.trimIndent()
  )
  manifest {
    attributes("Implementation-Title" to project.name, "Implementation-Version" to project.version)
  }
}

dokka {
  dokkaSourceSets.named("main") {
    documentedVisibilities.set(setOf(VisibilityModifier.Public, VisibilityModifier.Protected))
    reportUndocumented.set(true)
    jdkVersion.set(8)
    sourceLink {
      localDirectory.set(projectDir)
      remoteUrl.set(URI("https://github.com/nemecec/kassava/tree/main"))
      remoteLineSuffix.set("#L")
    }
  }
}

// Sign only when in-memory keys are provided (CI release). Local builds skip signing.
tasks.withType<Sign>().configureEach {
  enabled = project.findProperty("signingInMemoryKey") != null
}

mavenPublishing {
  // The release workflow invokes `publishAndReleaseToMavenCentral`, which both
  // uploads and releases; no need to also auto-release from the plain task.
  publishToMavenCentral()
  signAllPublications()

  coordinates(group.toString(), "kassava", version.toString())

  pom {
    name.set("Kassava")
    description.set(project.description)
    url.set("https://github.com/nemecec/kassava")
    licenses {
      license {
        name.set("The Apache Software License, Version 2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
        comments.set("A business-friendly OSS license")
      }
    }
    developers {
      developer {
        id.set("nemecec")
        name.set("Neeme Praks")
        email.set("neeme@praks.net")
        url.set("https://github.com/nemecec")
      }
      developer {
        id.set("jamesbassett")
        name.set("James Bassett")
        roles.set(listOf("Original author"))
      }
    }
    scm {
      url.set("https://github.com/nemecec/kassava")
      connection.set("scm:git:git://github.com/nemecec/kassava.git")
      developerConnection.set("scm:git:ssh://git@github.com/nemecec/kassava.git")
    }
  }
}

// `check` depends on `spotlessCheck`, so CI fails on unformatted sources; run
// `./gradlew spotlessApply` to fix them.
spotless {
  kotlin {
    target("src/**/*.kt")
    ktfmt(libs.versions.ktfmt.get()).googleStyle()
  }
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
}
