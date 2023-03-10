Test / scalaSource := { (baseDirectory in Test)(_ / "test") }.value

Compile / scalaSource := { (baseDirectory in Compile)(_ / "src") }.value
