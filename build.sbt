name := """image-repository"""

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.5"

val slickVersion     = "3.3.3"
val playSlickVersion = "5.0.0"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "com.github.jwt-scala"   %% "jwt-core"           % "7.1.4"
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick"                 % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp"        % slickVersion,
  "com.typesafe.play"  %% "play-slick"            % playSlickVersion,
  "com.typesafe.play"  %% "play-slick-evolutions" % playSlickVersion,
  "org.postgresql"      % "postgresql"            % "9.4-1206-jdbc42",
  "net.codingwell"     %% "scala-guice"           % "4.2.6",
  filters
)

libraryDependencies += "org.mindrot"                   % "jbcrypt"              % "0.4"
libraryDependencies += "software.amazon.awssdk"        % "s3"                   % "2.16.58"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.2"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "repo.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "repo.binders._"
