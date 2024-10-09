plugins {
  java
  application
  id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
        mavenCentral()
}

version = "1.0.0"
group = "org.rwtodd"

dependencies {
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

javafx {
    version = "23"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainModule = "rwt.minesweeper"
    mainClass = "rwt.minesweeper.MainApp"
}

