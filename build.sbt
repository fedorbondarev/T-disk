val tapirVersion   = "1.10.13"
val tethysVersion  = "0.26.0"
val doobieVersion  = "1.0.0-RC2"
val tofuVersion    = "0.13.2"
val derevoVersion  = "0.13.0"
val refinedVersion = "0.11.1"

lazy val rootProject = (project in file("."))
  .settings(
    name         := "tiny-disk",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := "2.13.14",
    libraryDependencies ++= Seq(
      // Tapir
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-tethys" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-derevo" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-cats" % "1.10.8",
      "com.softwaremill.sttp.tapir" %% "tapir-cats-effect" % tapirVersion,
      "ch.qos.logback" % "logback-classic" % "1.5.6",

      // Tethys
      "com.tethys-json" %% "tethys" % "0.26.0",
      "com.tethys-json" %% "tethys-derivation" % "0.28.4",

      // Cats
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.0",

      // Fs2
      "co.fs2" %% "fs2-core" % "3.10.2",
      "co.fs2" %% "fs2-io" % "3.10.2",

      // Tofu
      "tf.tofu" %% "tofu-core-ce3" % tofuVersion,
      "tf.tofu" %% "tofu-kernel" % tofuVersion,
      "tf.tofu" %% "tofu-logging" % tofuVersion,
      "tf.tofu" %% "tofu-logging-derivation" % tofuVersion,
      "tf.tofu" %% "tofu-logging-layout" % tofuVersion,
      "tf.tofu" %% "tofu-logging-logstash-logback" % tofuVersion,
      "tf.tofu" %% "tofu-logging-structured" % tofuVersion,

      // Derevo for tethys
      "tf.tofu" %% "derevo-tethys" % derevoVersion,

      // Doobie
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-h2" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,

      // Http4s
      "org.http4s" %% "http4s-ember-server" % "0.23.24",

      // Test
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % "1.10.8" % Test,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,

      // Pureconfig
      "com.github.pureconfig" %% "pureconfig" % "0.17.7",

      // Newtype
      "io.estatico" %% "newtype" % "0.4.4",

      // Liquibase
      "org.liquibase" % "liquibase-core" % "4.27.0"
    ),
    scalacOptions ++= Seq(
      "-Ymacro-annotations",
      "-feature",
      "-Werror",
      "-language:implicitConversions",
      "-Xlint",
      "-Xlint:-byname-implicit",
      "-Xlint:-implicit-recursion"
    ),
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.3" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
