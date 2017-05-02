name := """play-app-seed"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

resolvers += "bitbucket-release" at "https://bitbucket.org/Adrien/particeep-repository/raw/master/repository/"

resolvers += "Kaliber Internal Repository" at "https://jars.kaliber.io/artifactory/libs-release-local"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


libraryDependencies ++= Seq(
  filters,
  cache,
  ws,
  "org.scalatest"          %% "scalatest"            % "2.2.1"    % "test" withSources(),
  "org.scalatestplus"      %% "play"                 % "1.4.0-M4" % "test" withSources(),
  "com.h2database"         %  "h2"                   % "1.4.187"        withSources(),
  "com.papertrailapp"      %  "logback-syslog4j"     % "1.0.0"          withSources(),
  "com.github.nscala-time" %% "nscala-time"          % "2.4.0"          withSources(),
  "org.scalaz"             %% "scalaz-core"          % "7.1.5"          withSources(),
  //"io.kanaka"              %% "play-monadic-actions" % "1.0.1"          withSources(),
  "joda-time"              %  "joda-time"            % "2.8.1"          withSources(),
  "com.ibm.icu"            %  "icu4j"                % "56.1"           withSources(),
  "org.apache.commons"     %  "commons-email"        % "1.3"            withSources(),
  "commons-validator"      %  "commons-validator"    % "1.5.0"          withSources()
)

libraryDependencies ++= Seq(
  "org.postgresql"       %  "postgresql"            % "9.4-1201-jdbc41" withSources(),
  "mysql"                %  "mysql-connector-java"  % "5.1.37"          withSources(),
  "org.joda"             %  "joda-convert"          % "1.7"             withSources(),
  "com.typesafe.play"    %% "play-slick"            % "1.1.1"           withSources(),
  "com.typesafe.play"    %% "play-slick-evolutions" % "1.1.1"           withSources(),
  "com.typesafe.slick"   %% "slick-codegen"         % "3.1.1"           withSources(),
  "com.github.tminglei"  %% "slick-pg"              % "0.14.1"          withSources(),
  "com.github.tminglei"  %% "slick-pg_joda-time"    % "0.14.1"          withSources(),
  "com.github.tminglei"  %% "slick-pg_play-json"    % "0.14.1"          withSources(),
  "com.github.tototoshi" %% "slick-joda-mapper"     % "2.1.0"           withSources(),
  "net.kaliber"          %% "play-s3"               % "7.0.0"           withSources()
)

libraryDependencies ++= Seq(
  "api-lib"          % "api-lib" % "1.6.4",
  "com.github.driox" %% "sorus"  % "1.0.0"
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
TwirlKeys.templateImports ++= Seq("helpers._", "helpers.CustomTag._", "tags._", "utils._", "views.html.tags._", "views.html.tags.html._")

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

  
