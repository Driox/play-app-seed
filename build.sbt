name := """play-app-seed"""

lazy val commonSettings = Seq(
  organization := "com.particeep",
  version := "1.0.0",
  scalaVersion := "2.13.8",
  resolvers ++= Seq(
    "Bintray_DL" at "https://dl.bintray.com/kamon-io/releases/"
  ),
  libraryDependencies ++= (deps_common ++ deps_db ++ deps_akka ++ deps_tests),
  // don't run test in parallel. It will break the DB
  // concurrentRestrictions in Global += Tags.limit(Tags.Test, 1),
  scalacOptions ++= compiler_option,
  routesGenerator := InjectedRoutesGenerator,
  updateOptions := updateOptions.value.withCachedResolution(true),
  Compile / doc / sources := Seq.empty
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
  .settings(commonSettings: _*)

lazy val domain: Project = (project in file("modules/02-domain"))
  .settings(commonSettings: _*)
  .dependsOn(core % "test->test;compile->compile")

lazy val root: Project   = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(playSettings: _*)
  .aggregate(core, domain)
  .dependsOn(core % "test->test;compile->compile", domain)

val zio_version          = "1.0.13"
lazy val deps_common     = Seq(
  guice,
  filters,
  caffeine,
  ws,
  "com.papertrailapp"  % "logback-syslog4j"            % "1.0.0" withSources (),
  "pl.iterators"      %% "kebs-tagged"                 % "1.8.1" withSources (),
  "pl.iterators"      %% "kebs-slick"                  % "1.8.1" withSources (),
  "pl.iterators"      %% "kebs-play-json"              % "1.8.1" withSources (),
  "dev.zio"           %% "zio"                         % zio_version withSources (),
  "dev.zio"           %% "zio-streams"                 % zio_version withSources (),
  "dev.zio"           %% "zio-interop-reactivestreams" % "1.3.0.7-2" withSources (),
  "ai.x"              %% "play-json-extensions"        % "0.42.0" withSources (),
  "org.scalaz"        %% "scalaz-core"                 % "7.2.30" withSources (),
  "com.ibm.icu"        % "icu4j"                       % "68.1" withSources (),
  "commons-codec"      % "commons-codec"               % "1.13" withSources (),
  "org.apache.commons" % "commons-email"               % "1.5" withSources (),
  "commons-validator"  % "commons-validator"           % "1.6" withSources (),
  "org.apache.commons" % "commons-lang3"               % "3.9" withSources (),
  "org.mindrot"        % "jbcrypt"                     % "0.4" withSources ()
)

lazy val deps_tests = Seq(
  "org.scalatestplus"       %% "mockito-3-4"        % "3.2.6.0" % Test withSources (),
  "org.scalatestplus.play"  %% "scalatestplus-play" % "5.1.0"   % Test withSources () excludeAll ExclusionRule(
    organization = "org.mockito"
  ),
  "com.opentable.components" % "otj-pg-embedded"    % "0.13.3"  % Test withSources (),
  "org.gnieh"               %% "diffson-play-json"  % "4.1.1"   % Test withSources (),
  "com.h2database"           % "h2"                 % "1.4.194" % Test withSources ()
)

// Play 2.8.5 use akka 2.6.8
val akkaVersion    = "2.6.18"
lazy val deps_akka = Seq(
  "com.typesafe.akka"  %% "akka-testkit"             % akkaVersion % Test withSources () excludeAll ExclusionRule(organization =
    "com.typesafe.akka"),
  "com.lightbend.akka" %% "akka-stream-alpakka-csv"  % "2.0.1" withSources (),
  "com.lightbend.akka" %% "akka-stream-alpakka-file" % "2.0.1" withSources ()
)

val play_slick_version = "5.0.0"
val slick_pg_version   = "0.20.2"

lazy val deps_db = Seq(
  "org.postgresql"       % "postgresql"            % "42.2.6" withSources (),
  "com.typesafe.play"   %% "play-slick"            % play_slick_version withSources (),
  "com.typesafe.play"   %% "play-slick-evolutions" % play_slick_version withSources (),
  "com.github.tminglei" %% "slick-pg"              % slick_pg_version withSources (),
  "com.github.tminglei" %% "slick-pg_play-json"    % slick_pg_version withSources () excludeAll ExclusionRule(
    organization = "com.typesafe.play"
  )
)

// ~~~~~~~~~~~~~~~~~
// Compiler config

// code coverage
coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;models\\.data\\..*;views.html.*"

addCommandAlias("fmt", "; scalafix RemoveUnused; scalafix SortImports; all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "; scalafixAll --check; all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val compiler_option = Seq(
  "-deprecation",                  // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8",                         // Specify character encoding used by source files.
  "-explaintypes",                 // Explain type errors in more detail.
  "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
  "-language:postfixOps",
  "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds",         // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                   // Wrap field accessors to throw an exception on uninitialized access.
  // "-Xfatal-warnings",              // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
  "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",        // Option.apply used implicit view.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code",              // Warn when dead code is identified.
  "-Ywarn-extra-implicit",         // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",          // Warn when numerics are widened.
  // "-Ywarn-unused:implicits",      // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",          // Warn if a local definition is unused.
  //"-Ywarn-unused:params",          // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
  //"-Ywarn-unused:privates",        // Warn if a private member is unused.
  "-Ywarn-value-discard"           // Warn when non-Unit expression results are unused.
)

inThisBuild(
  List(
    semanticdbEnabled := true,                        // enable SemanticDB
    semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
    scalafmtOnCompile := true
  )
)

ThisBuild / scalafixDependencies ++= Seq(
  "com.nequissimus" %% "sort-imports" % "0.5.5"
)

import com.typesafe.sbt.packager.MappingsHelper.directory
Universal / mappings ++= directory(baseDirectory.value / "public")

// ~~~~~~~~~~~~~~~~~
// code generation task

//lazy val slick = TaskKey[Seq[File]]("gen-tables")
//slick := { //(sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
//  val dir = sourceManaged.value
//  val cp = (dependencyClasspath in Compile).value
//  val r = (runner in Compile).value
//  val s = streams.value
//
//  val outputDir = (dir / "slick").getPath // place generated files in sbt's managed sources folder
//  val url = "jdbc:h2:mem:test" // connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run
//  val jdbcDriver = "org.postgresql.Driver"
//  val slickDriver = "slick.jdbc.PostgresProfile"
//  val pkg = "demo"
//  (r.run("slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg), s.log))
//
//  val fname = outputDir + "/demo/Tables.scala"
//  Seq(file(fname))
//}
