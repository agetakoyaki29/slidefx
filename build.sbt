
val scalactic = "org.scalactic" %% "scalactic" % "2.2.5"
val scalatest = "org.scalatest" %% "scalatest" % "2.2.5" % "test"

lazy val root = (project in file(".")).
  settings(
    name := "scala editer fx",
    version := "0.0",
    scalaVersion := "2.11.8",

    // for fxml css
    unmanagedJars in Compile += {
        val ps = new sys.SystemProperties
        val jh = ps("java.home")
        Attributed.blank(file(jh) / "lib/ext/jfxrt.jar")
    },

    libraryDependencies += scalactic,
    libraryDependencies += scalatest,

    logBuffered in Test := false
  )
