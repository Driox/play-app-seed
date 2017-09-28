resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe repository plugin" at "https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"

resolvers += "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "scalaz-bintray" at "https://de.bintray.com/scalaz/releases/"

resolvers += "bitbucket-release" at "https://bitbucket.org/Adrien/particeep-repository/raw/master/repository/"

resolvers += Classpaths.sbtPluginReleases

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.5")

// use to display dependencies graph
// https://github.com/jrudolph/sbt-dependency-graph
// addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.1.0")

addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.1.1")

addSbtPlugin("net.ground5hark.sbt" % "sbt-concat" % "0.1.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "1.0.3")

// code plugins

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0" excludeAll(
  ExclusionRule(organization = "com.danieltrinh")))

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")

// scala lint tool : https://github.com/puffnfresh/wartremover
addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.13")

// new task that check maven for dependencies update
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

libraryDependencies += "org.scalariform" %% "scalariform" % "0.1.7"

addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC1")
