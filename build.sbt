enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)


name := "auslaenderbehoerde"

version := "0.1.0-SNAPSHOT"
organization := "com.commercetools.marketplace"

scalaVersion := "2.12.3"

resolvers ++= Seq(
  "Rewe Artifactory" at "https://artifactory.rewe-digital.com/artifactory/libs-release",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "lightshed-maven" at "http://dl.bintray.com/content/lightshed/maven"
)

scalacOptions ++= Seq(
  "-Xmax-classfile-name", "128",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-feature")

javacOptions ++= Seq(
  "-deprecation",
  "-Xlint:unchecked")
val akkaVersion = "2.5.2"
val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor",
  "com.typesafe.akka" %% "akka-stream",
  "com.typesafe.akka" %% "akka-slf4j"
).map(_ % akkaVersion)

val loggingDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)
val testDependencies = Seq(
  "org.scalatest"  %% "scalatest"    % "3.0.3",
  "org.mockito"    %  "mockito-core" % "1.10.19" ,
  "org.scalacheck" %% "scalacheck"   % "1.13.5" ,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
).map(_ % "test")

libraryDependencies ++=
  akkaDependencies ++
  loggingDependencies ++
  testDependencies
////docker settings
imageNames in docker := Seq(
  ImageName(s"ctrewe/${name.value}:" + System.getProperty("docker.tag", "latest"))
)

buildOptions in docker := BuildOptions(
  cache = false,
  pullBaseImage = BuildOptions.Pull.Always
)

dockerfile in docker := {
  val appDir: File = stage.value
  val targetDir = "/app"

  new Dockerfile {
    from("ctrewe/java8")
    entryPoint(s"$targetDir/bin/${executableScriptName.value}")
    copy(appDir, targetDir)
  }
}
