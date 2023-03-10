name := """play-app-seed"""

Global / lintUnusedKeysOnLoad := false

ThisBuild / scalaVersion := Dependencies.scala_version

lazy val commonSettings = Seq(
  organization                  := "com.particeep",
  version                       := "1.0.0",
  scalaVersion                  := Dependencies.scala_version,
  resolvers ++= Dependencies.combined_resolvers,
  libraryDependencies ++= Dependencies.deps_all,
  // don't run test in parallel. It will break the DB
  // concurrentRestrictions in Global += Tags.limit(Tags.Test, 1),
  routesGenerator               := InjectedRoutesGenerator,
  updateOptions                 := updateOptions.value.withCachedResolution(true),
  (Compile / scalastyleSources) := {
    val scalaSourceFiles = ((Compile / scalaSource).value ** "*.scala").get
    scalaSourceFiles
      .filterNot(_.getAbsolutePath.contains("Dao.scala"))
      .filterNot(_.getAbsolutePath.contains("views"))
  },
  Compile / doc / sources       := Seq.empty
)

lazy val playSettings = commonSettings ++ Seq(
  TwirlKeys.templateImports ++= Seq(
    "helpers._",
    "tags._",
    "_root_.utils._",
    "views.html.tags._",
    "views.html.tags.html._"
  )
)

lazy val core: Project = (project in file("modules/01-core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys    := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "core.build"
  )
  .settings(commonSettings: _*)

lazy val domain: Project = (project in file("modules/02-domain"))
  .settings(commonSettings: _*)
  .dependsOn(core % "test->test;compile->compile")

lazy val event: Project = (project in file("modules/03-event"))
  .settings(commonSettings: _*)
  .dependsOn(core % "test->test;compile->compile")

lazy val root: Project = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(playSettings: _*)
  .aggregate(core, domain, event)
  .dependsOn(core % "test->test;compile->compile", domain, event)

// code coverage
coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;models\\.data\\..*;views.html.*"

import com.typesafe.sbt.packager.MappingsHelper.directory
Universal / mappings ++= directory(baseDirectory.value / "public")

// Check Dependancy CVSS config
ThisBuild / dependencyCheckFailBuildOnCVSS         := 9.9f
ThisBuild / dependencyCheckFormats                 := Seq("XML", "HTML")
ThisBuild / dependencyCheckAssemblyAnalyzerEnabled := Option(false)
dependencyCheckOutputDirectory                     := Some(baseDirectory.value / "target/security-reports")
