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
}

group = "dev.nemecec.kassava"
version = "3.0.0-SNAPSHOT"
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
      remoteUrl.set(URI("https://github.com/nemecec/kassava/tree/master/"))
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

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
}
