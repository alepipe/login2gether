val Http4sVersion = "0.23.17"
val TapirVersion = "1.2.4"
val JwtScalaVersion = "9.1.2"
val ScalaTestVersion = "3.2.14"
val ScalaTestCatsEffect = "1.5.0"
val LogbackVersion = "1.2.11"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(
    organization := "nl.cleverbase",
    name         := "login2gether",
    version      := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",

    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % TapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % TapirVersion,
      "com.github.jwt-scala" %% "jwt-circe" % JwtScalaVersion,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % ScalaTestCatsEffect % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    scalacOptions ++= Seq("-Xfatal-warnings")
  )
