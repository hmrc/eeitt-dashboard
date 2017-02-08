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
import models.JsonClass
import play.api.libs.json.{JsObject, JsValue, Json}
import services.{AuthService, GoogleSheetsService}
import uk.gov.hmrc.secure.AsymmetricDecrypter

import scala.sys.process.Process

class WriteInteraction {

  val dataCenters = Map(
    "Qa" -> "https://kibana-datacentred-sal01-qa.tax.service.gov.uk/elasticsearch/logstash-qa*/_search?pretty",
    "Aws" -> "https://kibana-datacentred-sal01-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty",
    "Skyscape" -> "https://kibana-skyscape-farnborough-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty"
  )

  val authService = new AuthService
  val serviceSpreadSheet = new GoogleSheetsService

  val key : String = sys.env("PRIVATEKEY")

  val privateKey : PrivateKey = AsymmetricDecrypter.buildPrivateKey(key, "RSA")

  def oauthOneTimeCode : AppendValuesResponse = {

    val dataMap = getCurlResults

    val accessToken = getAccessToken

    serviceSpreadSheet.getWorksheetByName(accessToken, sys.env("FILEID"), privateKey, dataMap)
  }

  def getCurlResults : Map[String, List[String]] = {

//    val resultFrontend = resultsFrontendVerificationAws(0, 24) -> resultsFrontendVerificationSkyscape(0, 24)
//    val resultBackend = resultsBackEndVerificationAws(0 ,24) -> resultsBackEndVerificationSkyscape(0, 24)
    val frontend = findErrors(getFrontendAws(0, 24, checkFor500, parseJsonFromRequest, 500))
//    val frontend = findErrors(resultsFrontendVerificationAws(0, 24))

    val backend :List[String ] = findErrors(getBackendAws(0 ,24, checkFor500, parseJsonFromRequest, 500))
//    val backend = findErrors(resultsBackEndVerificationAws(0, 24))

    Map(
      "BusinessUsers" -> resultsBuissnessQuery(0, 24),
      "Agents" -> resultsAgentQuery(0, 24),
      "Backend" -> backend,
      "Frontend" -> frontend
    )
  }

  def compareDataCentreResults(first:List[String], second:List[String]) : Boolean = {
    first.size == second.size
  }

  def createPreConfiguredJWT : String = {
    val issueTime = Instant.now.getEpochSecond
    val exp = Instant.now.plusSeconds(600).getEpochSecond

    val header = new JsonWebSignature.Header
    header.setAlgorithm("RS256")
    header.setType("JWT")

    val payload = new JsonWebToken.Payload
    payload.set("scope", "https://spreadsheets.google.com/feeds/spreadsheets https://www.googleapis.com/auth/spreadsheets https://www.googleapis.com/auth/drive")
    payload.setIssuer("test-57@usagereportingautomation.iam.gserviceaccount.com")
    payload.setAudience("https://www.googleapis.com/oauth2/v4/token")
    payload.setSubject("daniel.connelly@digital.hmrc.gov.uk")
    payload.setIssuedAtTimeSeconds(issueTime)
    payload.setExpirationTimeSeconds(exp)

    val jsonFactory = new JacksonFactory

    JsonWebSignature.signUsingRsaSha256(privateKey,jsonFactory, header, payload)
  }

  def getAccessToken : String = {
    val signature = createPreConfiguredJWT
    authService.buildCredentialServiceAccount(signature).accessToken
  }

  def resultsAgentQuery(start:Int, end:Int) : List[String] ={
    val resultAws = Json.parse(Process(s"./LiveAgent.sh $start $end ${dataCenters("Aws")}") !!)
//    val resultSkyscape = Json.parse(Process(s"./LiveAgent.sh $start $end ${dataCenters("Skyscape")}") !!)
    parseJsonFromRequest(resultAws)
  }

  def resultsBuissnessQuery(start:Int, end:Int) : List[String] ={
    val resultAws = Json.parse(Process(s"./LiveBusinessUser.sh $start $end ${dataCenters("Aws")}") !!)
//    val resultSkyscape = Json.parse(Process(s"./LiveBusinessUser.sh $start $end ${dataCenters("Skyscape")}") !!)
    parseJsonFromRequest(resultAws)
  }

//  def resultsFrontendVerificationAws(start:Int, end:Int) : List[String] = {
//    val result = List(jsonResultFrontendAws(start, end))
//    if(checkFor500(result.head)){
//      val half = List(jsonResultFrontendAws(start, end/2), jsonResultFrontendAws(end/2, end))
//      if(checkFor500(half(0)) || checkFor500(half(1))){
//        val half2 = List(jsonResultFrontendSkyscape(start, end/3), jsonResultFrontendAws(end/3, (end/1.5).toInt), jsonResultFrontendAws((end/1.5).toInt, end))
//        if(checkFor500(half2(0)) || checkFor500(half2(1)) || checkFor500(half2(2))) {
//          val half3 = List(jsonResultFrontendAws(start, end/4), jsonResultFrontendAws(end/4, (end/4)*2), jsonResultFrontendAws((end/4)*2, (end/4)*3), jsonResultFrontendAws((end/4)*3, (end)))
//          parseJsonFromRequest(half3(0)).++( parseJsonFromRequest(half3(1))).++( parseJsonFromRequest(half3(2))).++( parseJsonFromRequest(half3(3)))
//        } else {
//          parseJsonFromRequest(half2(0)).++(parseJsonFromRequest(half2(1))).++(parseJsonFromRequest(half2(2)))
//        }
//      } else {
//        parseJsonFromRequest(half(0)).++(parseJsonFromRequest(half(1)))
//      }
//    } else {
//      parseJsonFromRequest(result.head)
//    }
//  }

//  def resultsFrontendVerificationSkyscape(start:Int, end:Int) : List[String] = {
//    val result = List(jsonResultFrontendSkyscape(start, end))
//    if(checkFor500(result(0))){
//      val half = List(jsonResultFrontendSkyscape(start, end/2), jsonResultFrontendSkyscape(end/2, end))
//      if(checkFor500(half(0)) || checkFor500(half(1))){
//        val half2 = List(jsonResultFrontendSkyscape(start, end/3), jsonResultFrontendSkyscape(end/3, (end/1.5).toInt), jsonResultFrontendSkyscape((end/1.5).toInt, end))
//        if(checkFor500(half2(0)) || checkFor500(half2(1)) || checkFor500(half2(2))) {
//          val half3 = List(jsonResultFrontendSkyscape(start, end/4), jsonResultFrontendSkyscape(end/4, (end/4)*2), jsonResultFrontendSkyscape((end/4)*2, (end/4)*3), jsonResultFrontendSkyscape((end/4)*3, (end)))
//          parseJsonFromRequest(half3(0)).++( parseJsonFromRequest(half3(1))).++( parseJsonFromRequest(half3(2))).++( parseJsonFromRequest(half3(3)))
//        } else {
//          parseJsonFromRequest(half2(0)).++(parseJsonFromRequest(half2(1))).++(parseJsonFromRequest(half2(2)))
//        }
//      } else {
//        parseJsonFromRequest(half(0)).++(parseJsonFromRequest(half(1)))
//      }
//    } else {
//      parseJsonFromRequest(result(0))
//    }
//  }

  private def combineDatacentreResults(first:List[String], second:List[String]): List[String] = {
    first.++(second)
  }

//  def resultsBackEndVerificationAws(start:Int, end:Int) : List[String] ={
//    val result = List(jsonResultBackendAws(start, end)) // 24 Hours
//    if(checkFor500(result(0))){
//      val half = List(jsonResultBackendAws(start, end/2), jsonResultBackendAws((end/2), end))
//      if(checkFor500(half(0)) || checkFor500(half(1))){
//        val half2 = List(jsonResultBackendAws(start, end/3), jsonResultBackendAws(end/3, (end/1.5).toInt), jsonResultBackendAws((end/1.5).toInt, end))
//        if(checkFor500(half2(0)) || checkFor500(half2(1)) || checkFor500(half2(2))) {
//          val half3 = List(jsonResultBackendAws(start, end/4), jsonResultBackendAws(end/4, (end/4)*2), jsonResultBackendAws((end/4)*2, (end/4)*3), jsonResultBackendAws((end/4)*3, (end)))
//          parseJsonFromRequest(half3(0)).++( parseJsonFromRequest(half3(1))).++( parseJsonFromRequest(half3(2))).++( parseJsonFromRequest(half3(3)))
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


  def getBackendAws(start: Int, end: Int, numElements: (JsValue) => Int, elements: (JsValue) => List[String], threshold:Int): List[String] = {

    val res = jsonResultBackendAws(start, end)
    if (numElements(res) <= threshold) elements(res) else {

      val middle = (end-start)/2 + (end-start)%2

      getBackendAws( start, middle, numElements, elements, threshold) ::: getBackendAws( middle, end, numElements, elements, threshold)

    }
  }

  def getBackendSkyscape(start: Int, end: Int, numElements: (JsValue) => Int, elements: (JsValue) => List[String], threshold:Int): List[String] = {

    val res = jsonResultBackendAws(start, end)
    if (numElements(res) <= threshold) elements(res) else {

      val middle = (end-start)/2 + (end-start)%2

      getBackendSkyscape(start, middle, numElements, elements, threshold) ::: getBackendSkyscape( middle, end, numElements, elements, threshold)

    }
  }

  def getFrontendAws(start: Int, end: Int, numElements: (JsValue) => Int, elements: (JsValue) => List[String], threshold:Int): List[String] = {

    val res = jsonResultFrontendAws(start, end)
    if (numElements(res) <= threshold) elements(res) else {

      val middle = (end-start)/2 + (end-start)%2
      println((end-start)/2)
      println(middle)
      println((end-start)%2)

      getFrontendAws( start, middle, numElements, elements, threshold) ::: getFrontendAws( middle, end, numElements, elements, threshold)

    }
  }

  def getFrontendSkyscape(start: Int, end: Int, numElements: (JsValue) => Int, elements: (JsValue) => List[String], threshold:Int): List[String] = {

    val res = jsonResultBackendAws(start, end)
    if (numElements(res) <= threshold) elements(res) else {

      val middle = (end-start)/2 + (end-start)%2

      getFrontendSkyscape(start, middle, numElements, elements, threshold) ::: getFrontendSkyscape(middle, end, numElements, elements, threshold)

    }
  }


  def testFct(start: Int, end: Int) = if (end-start <= 2) List.fill(9)("") else List.fill(10)("")



  def findErrors(list:List[String]) = {
    val errorFree = list.filter(p => !p.startsWith("request"))
    errorFree
  }

//  def resultsBackEndVerificationSkyscape(start:Int, end:Int) : List[String] ={
//    val result = List(jsonResultBackendSkyscape(start, end)) // 24 Hours
//    if(checkFor500(result(0))){
//      val half = List(jsonResultBackendSkyscape(start, end/2), jsonResultBackendSkyscape((end/2).toInt, end))
//      if(checkFor500(half(0)) || checkFor500(half(1))){
//        val half2 = List(jsonResultBackendSkyscape(start, end/3), jsonResultBackendSkyscape(end/3, (end/1.5).toInt), jsonResultBackendAws((end/1.5).toInt, end))
//        if(checkFor500(half2(0)) || checkFor500(half2(1)) || checkFor500(half2(2))) {
//          val half3 = List(jsonResultBackendSkyscape(start, end/4), jsonResultBackendSkyscape(end/4, (end/4)*2), jsonResultBackendSkyscape((end/4)*2, (end/4)*3), jsonResultBackendSkyscape((end/4)*3, (end)))
//          parseJsonFromRequest(half3(0)).++( parseJsonFromRequest(half3(1))).++( parseJsonFromRequest(half3(2))).++( parseJsonFromRequest(half3(3)))
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

  private def parseJsonFromRequest(json:JsValue) = {
    val list = json \ "hits" \ "hits"

    val resultList = list.as[List[JsObject]].map { x =>
      val obj = (x \ "_source" \ "log").as[String]
      val some = Json.parse(obj).as[JsonClass]
      some.message
    }
    resultList
  }

  private def checkFor500(json:JsValue): Int =  {
    val hits = json \ "hits" \ "total"
    println(hits)
    hits.get.as[Int]
  }

  private def jsonResultFrontendAws(start:Int, end:Int) = {
    Json.parse(Process(s"./FrontendVerification.sh $start $end ${dataCenters("Aws")}") !!)
  }

  private def jsonResultFrontendSkyscape(start:Int, end:Int) = {
    Json.parse(Process(s"./FrontendVerification.sh $start $end ${dataCenters("Skyscape")}") !!)
  }

  private def jsonResultBackendAws(start:Int, end:Int) = {
    Json.parse(Process(s"./BackendVerification.sh $start $end ${dataCenters("Aws")}") !!)
  }

  private def jsonResultBackendSkyscape(start:Int, end:Int) = {
    Json.parse(Process(s"./BackendVerification.sh $start $end ${dataCenters("Skyscape")}") !!)
  }
}
