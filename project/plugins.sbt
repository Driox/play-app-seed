resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe repository plugin" at "https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"

resolvers += "jBCrypt Repository" at "https://repo1.maven.org/maven2/org/"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "scalaz-bintray" at "https://de.bintray.com/scalaz/releases/"

resolvers += "bitbucket-release" at "https://bitbucket.org/Adrien/particeep-repository/raw/master/repository/"

resolvers += Classpaths.sbtPluginReleases

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.13")

// scala lint tool : https://github.com/puffnfresh/wartremover
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.16")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.34")

addSbtPlugin("io.kamon" % "sbt-kanela-runner-play-2.8" % "2.0.12")

// use to display dependencies graph
// https://github.com/jrudolph/sbt-dependency-graph
// addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

// code plugins

addSbtPlugin(
  "org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0" excludeAll (ExclusionRule(
    organization = "com.danieltrinh"
  ))
)
