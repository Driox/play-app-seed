name := """play-app-seed"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

resolvers += "bitbucket-release" at "https://bitbucket.org/Adrien/particeep-repository/raw/master/repository/"

resolvers += "Kaliber Internal Repository" at "https://jars.kaliber.io/artifactory/libs-release-local"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


libraryDependencies ++= Seq(
  guice,
  filters,
  ehcache,
  ws,
  "com.typesafe.play"      %% "play-json"            % "2.6.0"          withSources(),
  "com.h2database"         %  "h2"                   % "1.4.194"        withSources(),
  "com.papertrailapp"      %  "logback-syslog4j"     % "1.0.0"          withSources(),
  "com.github.nscala-time" %% "nscala-time"          % "2.4.0"          withSources(),
  "org.scalaz"             %% "scalaz-core"          % "7.1.5"          withSources(),
  "joda-time"              %  "joda-time"            % "2.8.1"          withSources(),
  "com.ibm.icu"            %  "icu4j"                % "56.1"           withSources(),
  "org.apache.commons"     %  "commons-email"        % "1.3"            withSources(),
  "commons-validator"      %  "commons-validator"    % "1.5.0"          withSources()
)

libraryDependencies ++= Seq(
  "org.mockito"            % "mockito-core"          % "1.9.5"    % "test" withSources(),
  "org.scalatestplus.play" %% "scalatestplus-play"   % "3.1.2"    % "test" withSources(),
  "org.scalatestplus"      %% "play"                 % "1.4.0-M4" % "test" withSources()

)

val slick_pg_version = "0.15.3"

libraryDependencies ++= Seq(
  "org.postgresql"       %  "postgresql"            % "9.4-1201-jdbc41" withSources(),
  "org.joda"             %  "joda-convert"          % "1.7"             withSources(),
  "com.typesafe.play"    %% "play-slick"            % "3.0.2"           withSources(),
  "com.typesafe.play"    %% "play-slick-evolutions" % "3.0.2"           withSources(),
  "com.typesafe.slick"   %% "slick-codegen"         % "3.2.0"           withSources(),
  "com.github.tminglei"  %% "slick-pg"              % slick_pg_version  withSources(),
  "com.github.tminglei"  %% "slick-pg_joda-time"    % slick_pg_version  withSources(),
  "com.github.tminglei"  %% "slick-pg_play-json"    % slick_pg_version  withSources(),
  "com.github.tototoshi" %% "slick-joda-mapper"     % "2.3.0"           withSources(),
  "net.kaliber"          %% "play-s3"               % "7.0.0"           withSources()
)

libraryDependencies ++= Seq(
  //"api-lib"          % "api-lib" % "1.6.4",
  "com.github.driox" %% "sorus"  % "1.1.2"
)

// ~~~~~~~~~~~~~~~~~
// Framework config

// use DI
routesGenerator := InjectedRoutesGenerator

Concat.groups := Seq(
  "main.js" -> group(baseDirectory.value / "app" / "assets" / "js" ** "*.js")
)

// Public assets pipeline
pipelineStages := Seq(rjs, concat, digest, gzip)

// Public assets pipeline in dev mode
(pipelineStages in Assets) := Seq(concat)

// Template config
TwirlKeys.templateImports ++= Seq("helpers._", "helpers.CustomTag._", "tags._", "_root_.utils._", "views.html.tags._", "views.html.tags.html._")

// ~~~~~~~~~~~~~~~~~
// Compiler config

// code coverage
coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;models\\.data\\..*;views.html.*"

// sbt and compiler option
scalacOptions ++= Seq(
  "-deprecation",
  //"-feature",
  "-unchecked",
  //"-Xfatal-warnings",
  "-Xlint",
  "-Ywarn-dead-code",
  //"-Ywarn-unused",
  //"-Ywarn-unused-import",
  "-Ywarn-value-discard" //when non-Unit expression results are unused 
)

incOptions := incOptions.value.withNameHashing(true)

updateOptions := updateOptions.value.withCachedResolution(true)


// ~~~~~~~~~~~~~~~~~
//Scalariform config

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(scalariform.formatter.preferences.AlignSingleLineCaseStatements, true)
  .setPreference(scalariform.formatter.preferences.AlignParameters, true)
  .setPreference(scalariform.formatter.preferences.DoubleIndentClassDeclaration, true)
  .setPreference(scalariform.formatter.preferences.PreserveDanglingCloseParenthesis, true)

// ~~~~~~~~~~~~~~~~~
// code generation task
slick <<= slickCodeGenTask
lazy val slick = TaskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
  val outputDir = (dir / "slick").getPath // place generated files in sbt's managed sources folder
  val url = "jdbc:h2:mem:test" // connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run
  val jdbcDriver = "org.postgresql.Driver"
  val slickDriver = "slick.jdbc.PostgresProfile"
  val pkg = "demo"
  toError(r.run("slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg), s.log))
  val fname = outputDir + "/demo/Tables.scala"
  Seq(file(fname))
}
