plugins {
  // Auto-provisions the Java 21 toolchain when it isn't already installed, so the
  // build works on machines that only ship a different JDK.
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}

rootProject.name = "kassava"
