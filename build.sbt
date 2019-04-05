import scala.languageFeature.experimental.macros

name := "staticcheck"

version := "0.1"

lazy val commonSettings = Def.settings(
  scalaVersion := "2.12.8"
)

lazy val myWarts = project.in(file("my-warts")).settings(
  commonSettings,
  libraryDependencies ++= Seq(
    "org.wartremover" %% "wartremover" % wartremover.Wart.PluginVersion
  )
)

lazy val root = (project in file(".")).dependsOn(myWarts).settings(
  commonSettings,
  wartremoverWarnings += Wart.custom("mywarts.CaseClassParametersCoverage"),
  wartremoverClasspaths ++= {
    (fullClasspath in (myWarts, Compile)).value.map(_.data.toURI.toString)
  },
  wartremoverWarnings ++= Warts.unsafe
)
