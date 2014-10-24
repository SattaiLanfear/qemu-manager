name := """qemu"""

version := "1.0"

scalaVersion := "2.11.1"

mainClass in (Compile, run) := Some("com.greyscribes.IptablesManager")

mainClass in (Compile, packageBin) := Some("com.greyscribes.IptablesManager")

exportJars := true

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.6" % "test"

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.3.3"

libraryDependencies ++= Seq(
	"com.typesafe.play" %% "play-json" % "2.3.4",
	"com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
	"com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3"
)

// Enable additional warnings from the Scala compiler
scalacOptions ++= Seq("-deprecation","-unchecked","-feature", "-language:reflectiveCalls")

