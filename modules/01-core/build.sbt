scalaSource in Test := { (baseDirectory in Test)(_ / "test") }.value

scalaSource in Compile := { (baseDirectory in Compile)(_ / "src") }.value
