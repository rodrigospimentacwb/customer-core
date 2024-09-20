ThisBuild / scalaVersion     := "3.3.3"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "br.com.pepper"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "customer-core",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.1.7",

      // Http4s dependencies

      // Interoperability with ZIO and Http4s
      "dev.zio" %% "zio-interop-cats" % "23.1.0.3",

      "org.typelevel" %% "cats-effect" % "3.5.4",

      // Tapir
      "com.softwaremill.sttp.tapir" %% "tapir-core"         % "1.11.4",
      "com.softwaremill.sttp.tapir" %% "tapir-zio"          % "1.11.4",
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % "1.11.4",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.11.4",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % "1.11.4",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.11.4",

      "org.http4s" %% "http4s-blaze-server" % "0.23.16",

      // Pure Config
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.7",

      // Log
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      "org.slf4j" % "slf4j-api" % "2.0.12",

      "dev.zio" %% "zio-test" % "2.1.4" % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
