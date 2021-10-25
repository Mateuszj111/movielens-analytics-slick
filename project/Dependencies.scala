import sbt._

object Dependencies {
  lazy val slick = "com.typesafe.slick" %% "slick" % "3.3.3"
  lazy val slf4j = "org.slf4j" % "slf4j-nop" % "1.7.32"
  lazy val slickTypesafe = "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"
  lazy val postgres = "org.postgresql" % "postgresql" % "42.3.0"
}
