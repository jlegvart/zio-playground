scalaVersion := "2.13.8"

name         := "zio-playground"
organization := "zio.playground"
version      := "1.0"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio"         % "2.0.0",
  "dev.zio" %% "zio-streams" % "2.0.0",
)
