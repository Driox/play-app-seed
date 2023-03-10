import sbt._
import play.sbt.PlayImport._

object Dependencies {

  val scala_version = "2.13.10"

  lazy val combined_resolvers = Seq(
    "bitbucket-release" at "https://bitbucket.org/Adrien/particeep-repository/raw/master/repository/",
    "Kaliber Internal Repository" at "https://jars.kaliber.io/artifactory/libs-release-local",
    Resolver.mavenCentral
  )

  lazy val deps_all = deps_common ++ deps_db ++ deps_akka ++ deps_tests

  val zio_version      = "1.0.13"
  lazy val deps_common = Seq(
    guice,
    filters,
    ehcache,
    ws,
    "ch.qos.logback"     % "logback-classic"             % "1.4.5" withSources (),
    "com.papertrailapp"  % "logback-syslog4j"            % "1.0.0" withSources () excludeAll (
      ExclusionRule(organization = "ch.qos.logback")
    ),
    "pl.iterators"      %% "kebs-tagged"                 % "1.9.5" withSources (),
    "pl.iterators"      %% "kebs-slick"                  % "1.9.5" withSources (),
    "pl.iterators"      %% "kebs-play-json"              % "1.9.5" withSources (),
    "dev.zio"           %% "zio"                         % zio_version withSources (),
    "dev.zio"           %% "zio-streams"                 % zio_version withSources (),
    "dev.zio"           %% "zio-interop-reactivestreams" % "1.3.0.7-2" withSources (),
    "ai.x"              %% "play-json-extensions"        % "0.42.0" withSources (),
    "org.scalaz"        %% "scalaz-core"                 % "7.2.35" withSources (),
    "com.ibm.icu"        % "icu4j"                       % "72.1" withSources (),
    "org.apache.commons" % "commons-lang3"               % "3.12.0" withSources (),
    "org.apache.commons" % "commons-email"               % "1.5" withSources (),
    "commons-codec"      % "commons-codec"               % "1.15" withSources (),
    "commons-validator"  % "commons-validator"           % "1.7" withSources (),
    "org.mindrot"        % "jbcrypt"                     % "0.4" withSources ()
  )

  val akkaVersion    = "2.6.20"
  lazy val deps_akka = Seq(
    "com.typesafe.akka"  %% "akka-testkit"             % akkaVersion % Test withSources () excludeAll ExclusionRule(organization =
      "com.typesafe.akka"),
    "com.lightbend.akka" %% "akka-stream-alpakka-csv"  % "4.0.0" withSources (),
    "com.lightbend.akka" %% "akka-stream-alpakka-file" % "4.0.0" withSources ()
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

  val play_slick_version = "5.1.0"
  val slick_pg_version   = "0.21.1"

  lazy val deps_db = Seq(
    "org.postgresql"       % "postgresql"            % "42.5.3" withSources (),
    "com.typesafe.play"   %% "play-slick"            % play_slick_version withSources (),
    "com.typesafe.play"   %% "play-slick-evolutions" % play_slick_version withSources (),
    "com.github.tminglei" %% "slick-pg"              % slick_pg_version withSources (),
    "com.github.tminglei" %% "slick-pg_play-json"    % slick_pg_version withSources () excludeAll ExclusionRule(
      organization = "com.typesafe.play"
    )
  )
}
