resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe repository plugin" at "https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"

resolvers += "jBCrypt Repository" at "https://repo1.maven.org/maven2/org/"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += Classpaths.sbtPluginReleases

resolvers += Resolver.jcenterRepo

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.19")

// scala lint tool : https://github.com/puffnfresh/wartremover
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.0.6")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.6")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.3")

addSbtPlugin("io.kamon" % "sbt-kanela-runner-play-2.8" % "2.0.14")

// use to display dependencies graph
// https://github.com/jrudolph/sbt-dependency-graph
// addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

// code plugins

addSbtPlugin(
  "org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0" excludeAll (ExclusionRule(
    organization = "com.danieltrinh"
  ))
)

// run sbt dependencyCheckAnyProject
// doc generated in /particeep-api/target/scala-2.13/dependency-check-report.html
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "4.3.0")

// This is an issue with lib / sbt plugin who don't have the same version for scala-xml
// Binary compatility is "nearly" ok between scala-xml version
// And this impact only sbt coverage test in jenkins
// cf. https://github.com/sbt/sbt/issues/6997
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
