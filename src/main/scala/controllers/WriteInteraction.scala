/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import java.security.PrivateKey
import java.time.Instant

import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.json.webtoken.{JsonWebSignature, JsonWebToken}
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import models.{GoogleApp, JsonClass}
import play.api.libs.json.{JsObject, JsValue, Json}
import services.{AuthService, GoogleSheetsService, Json}

import uk.gov.hmrc.secure.AsymmetricDecrypter

import scala.sys.process.Process

class WriteInteraction {
  lazy val loadApp = services.Json.fromJson[GoogleApp](scala.io.Source.fromFile("src/main/resources/serviceAccount.json").mkString)
  val dataCenters = Map(
    "Qa" -> "https://kibana-datacentred-sal01-qa.tax.service.gov.uk/elasticsearch/logstash-qa*/_search?pretty",
    "Aws" -> "https://kibana-datacentred-sal01-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty",
    "Skyscape" -> "https://kibana-skyscape-farnborough-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty"
  )

  val authService = new AuthService
  val serviceSpreadSheet = new GoogleSheetsService

  val key: String = loadApp.privateKey

   val privateKey: PrivateKey = AsymmetricDecrypter.buildPrivateKey(key, "RSA")

  def oauthOneTimeCode: AppendValuesResponse = {

    val dataMap = getCurlResults

    val accessToken = getAccessToken

    serviceSpreadSheet.getWorksheetByName(accessToken, loadApp.fileId, privateKey, dataMap)
  }

  def getCurlResults: Map[String, List[String]] = {

//    val resultFrontend = resultsFrontendVerificationAws(0, 24) -> resultsFrontendVerificationSkyscape(0, 24)
//    val resultBackend = resultsBackEndVerificationAws(0, 24) -> resultsBackEndVerificationSkyscape(0, 24)
//    val frontend = findErrors(resultsFrontendVerificationAws(0, 24))
//
//    val backend: List[String] = findErrors(resultsBackEndVerificationAws(0, 24))

    Map(
      "BusinessUsers" -> resultsBuissnessQuery(0, 24),
      "Agents" -> resultsAgentQuery(0, 24),
      "Backend" -> List(),
      "Frontend" -> List()
    )
  }

  def compareDataCentreResults(first: List[String], second: List[String]): Boolean = {
    first.size == second.size
  }

  def createPreConfiguredJWT: String = {
    val issueTime = Instant.now.getEpochSecond
    val exp = Instant.now.plusSeconds(600).getEpochSecond

    val header = new JsonWebSignature.Header
    header.setAlgorithm("RS256")
    header.setType("JWT")

    val payload = new JsonWebToken.Payload
    payload.set("scope", "https://spreadsheets.google.com/feeds/spreadsheets https://www.googleapis.com/auth/spreadsheets https://www.googleapis.com/auth/drive")
    payload.setIssuer(loadApp.clientEmail)
    payload.setAudience("https://www.googleapis.com/oauth2/v4/token")
    payload.setSubject(loadApp.userImpersonation)
    payload.setIssuedAtTimeSeconds(issueTime)
    payload.setExpirationTimeSeconds(exp)

    val jsonFactory = new JacksonFactory

    JsonWebSignature.signUsingRsaSha256(privateKey, jsonFactory, header, payload)
  }

  def getAccessToken: String = {
    val signature = createPreConfiguredJWT
    authService.buildCredentialServiceAccount(signature).accessToken
  }

  def resultsAgentQuery(start: Int, end: Int): List[String] = {
    val resultAws = play.api.libs.json.Json.parse(Process(s"./LiveAgent.sh $start $end ${dataCenters("Qa")}") !!)
    //    val resultSkyscape = Json.parse(Process(s"./LiveAgent.sh $start $end ${dataCenters("Skyscape")}") !!)
    parseJsonFromRequest(resultAws)
  }

  def resultsBuissnessQuery(start: Int, end: Int): List[String] = {
    val resultAws = play.api.libs.json.Json.parse(Process(s"./LiveBusinessUser.sh $start $end ${dataCenters("Qa")}") !!)
    //    val resultSkyscape = Json.parse(Process(s"./LiveBusinessUser.sh $start $end ${dataCenters("Skyscape")}") !!)
    parseJsonFromRequest(resultAws)
  }

//  def resultsFrontendVerificationAws(start: Int, end: Int): List[String] = {
//    val result = List(jsonResultFrontendAws(start, end))
//    if (checkFor500(result.head)) {
//      val half = List(jsonResultFrontendAws(start, end / 2), jsonResultFrontendAws(end / 2, end))
//      if (checkFor500(half.head) || checkFor500(half.last)) {
//        List("")
//      } else {
//        parseJsonFromRequest(half.head).++(parseJsonFromRequest(half.last))
//      }
//    } else {
//      parseJsonFromRequest(result.head)
//    }
//  }

//  def resultsFrontendVerificationSkyscape(start: Int, end: Int): List[String] = {
//    val result = List(jsonResultFrontendSkyscape(start, end))
//    if (checkFor500(result(0))) {
//      val half = List(jsonResultFrontendSkyscape(start, end / 2), jsonResultFrontendSkyscape(end / 2, end))
//      if (checkFor500(half(0)) || checkFor500(half(1))) {
//        List("")
//      } else {
//        parseJsonFromRequest(half(0)).++(parseJsonFromRequest(half(1)))
//      }
//    } else {
//      parseJsonFromRequest(result(0))
//    }
//  }

  private def combineDatacentreResults(first: List[String], second: List[String]): List[String] = {
    first.++(second)
  }

//  def resultsBackEndVerificationAws(start: Int, end: Int): List[String] = {
//    val result = List(jsonResultBackendAws(start, end)) // 24 Hours
//    if (checkFor500(result(0))) {
//      val half = List(jsonResultBackendAws(start, end / 2), jsonResultBackendAws((end / 2), end))
//      if (checkFor500(half(0)) || checkFor500(half(1))) {
//        val half2 = List(jsonResultBackendAws(start, end / 3), jsonResultBackendAws(end / 3, (end / 1.5).toInt), jsonResultBackendAws((end / 1.5).toInt, end))
//        if (checkFor500(half2(0)) || checkFor500(half2(1)) || checkFor500(half2(2))) {
//          List("")
//        } else {
//          parseJsonFromRequest(half2(0)).++(parseJsonFromRequest(half2(1))).++(parseJsonFromRequest(half2(2)))
//        }
//      } else {
//        parseJsonFromRequest(half(0)).++(parseJsonFromRequest(half(1)))
//      }
//    } else {
//      val results = parseJsonFromRequest(result(0))
//      results
//    }
//  }

  def findErrors(list: List[String]) = {
    val errorFree = list.filter(p => !p.startsWith("request"))
    errorFree
  }

//  def resultsBackEndVerificationSkyscape(start: Int, end: Int): List[String] = {
//    val result = List(jsonResultBackendSkyscape(start, end)) // 24 Hours
//    if (checkFor500(result(0))) {
//      val half = List(jsonResultBackendSkyscape(start, end / 2), jsonResultBackendSkyscape((end / 2).toInt, end))
//      if (checkFor500(half(0)) || checkFor500(half(1))) {
//        val half2 = List(jsonResultBackendSkyscape(start, end / 3), jsonResultBackendSkyscape(end / 3, (end / 1.5).toInt), jsonResultBackendAws((end / 1.5).toInt, end))
//        if (checkFor500(half2(0)) || checkFor500(half2(1)) || checkFor500(half2(2))) {
//          List("")
//        } else {
//          parseJsonFromRequest(half2(0)).++(parseJsonFromRequest(half2(1))).++(parseJsonFromRequest(half2(2)))
//        }
//      } else {
//        parseJsonFromRequest(half(0)).++(parseJsonFromRequest(half(1)))
//      }
//    } else {
//      val results = parseJsonFromRequest(result(0))
//      results
//    }
//  }

  private def parseJsonFromRequest(json: JsValue) = {
    val list = json \ "hits" \ "hits"

    val resultList = list.as[List[JsObject]].map { x =>
      val obj = (x \ "_source" \ "log").as[String]
      val some = play.api.libs.json.Json.parse(obj).as[JsonClass]
      some.message
    }
    resultList
  }

  private def checkFor500(json: JsValue): Boolean = {
    val hits = json \ "hits" \ "total"
    hits.get.as[Int] >= 500
  }
//
//  private def jsonResultFrontendAws(start: Int, end: Int) = {
//    play.api.libs.json.Json.parse(Process(s"./FrontendVerification.sh $start $end ${dataCenters("Aws")}") !!)
//  }
//
//  private def jsonResultFrontendSkyscape(start: Int, end: Int) = {
//    play.api.libs.json.Json.parse(Process(s"./FrontendVerification.sh $start $end ${dataCenters("Skyscape")}") !!)
//  }
//
//  private def jsonResultBackendAws(start: Int, end: Int) = {
//    play.api.libs.json.Json.parse(Process(s"./BackendVerification.sh $start $end ${dataCenters("Aws")}") !!)
//  }
//
//  private def jsonResultBackendSkyscape(start: Int, end: Int) = {
//    play.api.libs.json.Json.parse(Process(s"./BackendVerification.sh $start $end ${dataCenters("Skyscape")}") !!)
//  }
}
