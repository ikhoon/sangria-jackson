import sbtrelease.ReleaseStateTransformations._

name := "sangria-jackson"
organization := "com.github.ikhoon"

description := "Sangria jackson marshalling"
homepage := Some(url("https://github.ikhoon/snagria-jackson"))
licenses := Seq("Apache License, ASL Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion := "2.12.4"
crossScalaVersions := Seq("2.11.12", "2.12.4")

scalacOptions ++= Seq("-deprecation", "-feature")

scalacOptions ++= {
  if (scalaVersion.value startsWith "2.12")
    Seq.empty
  else
    Seq("-target:jvm-1.7")
}

val jacksonVersion = "2.8.4"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria-marshalling-api" % "1.0.0",

  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,

  "org.sangria-graphql" %% "sangria-marshalling-testkit" % "1.0.1" % Test,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)

// Publishing

publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := (_ => false)
publishTo := Some(
  if (version.value.trim.endsWith("SNAPSHOT"))
    "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")

startYear := Some(2018)
organizationHomepage := Some(url("https://github.com/sangria-graphql"))
developers := Developer("ikhoon", "Ikhoon Eom", "", url("https://github.com/ikhoon")) :: Nil
scmInfo := Some(ScmInfo(
  browseUrl = url("https://github.com/ikhoon/sangria-jackson.git"),
  connection = "scm:git:git@github.com:ikhoon/sangria-jackson.git"
))

releasePublishArtifactsAction := PgpKeys.publishSigned.value
releaseCrossBuild := true
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  //        releaseStepCommand("publishSigned"),
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)
// nice *magenta* prompt!

shellPrompt in ThisBuild := { state =>
  scala.Console.MAGENTA + Project.extract(state).currentRef.project + "> " + scala.Console.RESET
}
