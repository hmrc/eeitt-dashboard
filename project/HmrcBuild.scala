/*
 * Copyright 2015 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt.Keys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object HmrcBuild extends Build {
import BuildDependenices._
  val appName = "eeitt-dashboard"

  val appDependencies = Seq(
    "uk.gov.hmrc" %% "secure" % "7.0.0",
    "com.github.pureconfig" %% "pureconfig" % "0.7.2",
    "uk.gov.hmrc" %% "play-reactivemongo" % "5.1.0",
    "uk.gov.hmrc" %% "microservice-bootstrap" % "5.8.0",
    "uk.gov.hmrc" %% "play-authorisation" % "4.2.0",
    "uk.gov.hmrc" %% "play-health" % "2.0.0",
    "uk.gov.hmrc" %% "play-url-binders" % "2.0.0",
    "uk.gov.hmrc" %% "play-config" % "3.1.0",
    "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
    "uk.gov.hmrc" %% "domain" % "4.0.0",
    "org.scalaz" % "scalaz-core_2.11" % "7.3.0-M8",
    "com.google.api-client" % "google-api-client-appengine" % "1.22.0",
    "com.google.gdata" % "core" % "1.47.1",
    "com.google.apis" % "google-api-services-sheets" % "v4-rev127-1.18.0-rc",
    "com.google.apis" % "google-api-services-oauth2" % "v2-rev124-1.22.0",
    "com.google.apis" % "google-api-services-drive" % "v3-rev59-1.22.0",
    "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.4",
    "com.netaporter" % "scala-uri_2.11" % "0.4.16",
    "org.scalaj" %% "scalaj-http" % "2.3.0",
    "org.jsoup" % "jsoup" % "1.8.1" % "test",
    "org.typelevel" % "cats-core_2.11" % "0.9.0",
    Test.scalaTest,
    Test.pegdown,
    Test.hmrcTest,
    Test.scalaTestPlus
  )

  lazy val library : Project = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      name := appName,
      scalaVersion := "2.11.6",
      libraryDependencies ++= appDependencies,
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases")
      ),
      crossScalaVersions := Seq("2.11.6")
    )
    .settings(unmanagedResourceDirectories in sbt.Compile += baseDirectory.value / "resources")
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
}

object Dependencies {
  object Compile {
    val play : ModuleID = "com.typesafe.play" %% "play_2.11" % "2.5.12"

  }
}

private object BuildDependenices{
  private val hmrcTestVersion = "2.2.0"
  object Compile {
  }

  sealed abstract class Test(scope: String) {
     val hmrcTest : ModuleID  = "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope
    val scalaTest : ModuleID = "org.scalatest" %% "scalatest" % "2.2.4" % scope
    val pegdown : ModuleID = "org.pegdown" % "pegdown" % "1.5.0" % scope
    val scalaTestPlus : ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0-M1" % scope

  }
  object Test extends Test("test")


  }



