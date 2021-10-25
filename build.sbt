

ThisBuild / scalaVersion := "2.13.6"


lazy val root = (project in file("."))
  .settings(
    name := "movielens",
    version := "0.1",
    libraryDependencies ++= Seq(
      Dependencies.slick,
      Dependencies.slf4j,
      Dependencies.slickTypesafe,
      Dependencies.postgres
    )
  )