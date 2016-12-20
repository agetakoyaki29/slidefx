lazy val root = (project in file("."))
  .settings(
    name := "scala editer fx",
    version := "0.0",
    scalaVersion := "2.12.0",

    // for fxml css
    unmanagedJars in Compile += {
        val ps = new sys.SystemProperties
        val jh = ps("java.home")
        Attributed.blank(file(jh) / "lib/ext/jfxrt.jar")
    }
  )
