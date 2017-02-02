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

import java.time.Instant

import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.json.webtoken.{JsonWebSignature, JsonWebToken}
import models.JsonClass
import play.api.libs.json.{JsObject, JsValue, Json}
import services.{AuthService, GoogleSheetsService}
import uk.gov.hmrc.secure.AsymmetricDecrypter

import scala.sys.process.Process

class WriteInteraction {

  val agentQuery = "app:\"eeitt\" AND NOT app:\"eeitt-frontend\" AND \"registration of agent\""
  val buissnessUserQuery = "app:\"eeitt\" AND NOT app:\"eeitt-frontend\" AND \"registration of business\""
  val dataCenters = Map(
    "Qa" -> "https://kibana-datacentred-sal01-qa.tax.service.gov.uk/elasticsearch/logstash-qa*/_search?pretty",
    "Aws" -> "https://kibana-datacentred-sal01-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty",
    "Skyscape" -> "https://kibana-skyscape-farnborough-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty"
  )

  val authService = new AuthService
  val serviceSpreadSheet = new GoogleSheetsService

  val key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDORb8tgN4XLrXBYtCEmJGrTrrD62gAa/tlUxTAdtBunpMkZMhuX6vzk9JJ7+db2XrxR8eKUX7ErzcokbSEmSiwsbJB0GGTJfhWMavSWpe/29pFfzp767CTVd6AlTGwaTIaUS/MFoNmcPjU//29N5Rm1yWd33sLyPOfwQukpqJXpYBO2GEMCzDz6fvEoxeMGgzFjjRW1ck/t0WmsrJ0CafAdvhTttmHFyJEHdY5JC6MK70ksgFbnyIDQsdnzPEK2Pn6+Ql32Ff5yIS0TE+hNr8p4XtZ6XYvmmWLq8VuEsSH4rQpwXTH902cosnhWi8RBahM2NvHDXKX4K/IWKovb9j3AgMBAAECggEAexoU5ksqQBuYTTlzyC5lgR8TRymOf/Hxrp7Om65M5jetCOM82uIt9MgbkBSktqQsQyLbaykHxsnq2UNbwGbHaewivjRmhzL56jbqnDeEqPPEaAVuGdanIsb0azie1vFw/VPGer5U3SY+2p+MBwjMgDOWN4nQHVBoVMcY88Ke0D7aZNnsfJ5VQ/UL1jZhegKDropryM3wfuRPMzAGPdAzTCSojHdJH44sdj+uW77Uvcz+p6516C5T86becIoCYVtLFXtWc6PQTUbrOeI8CyawD5ehwmjLzfN+Vhnzf5NEDwWmQABUv2dbfan3Cjq4FvBsq9NTMeixAM1BqExQuiFGgQKBgQD06j5DUl+Efvzyhh4eDipNvRTmwla8dyuuYzUPvk9QVL515IDu4EQyDUJ1pGwbINg4BvhV4dQ5+i9vCvBcOcCwK+3X2Ro/GPCeIB5BX/UNosXEx+sGFX+2JnWONN7HmP9YOIoBWbbs6Yop2pQILXeD26Dy4ydb2K8F1IIl+kB5jwKBgQDXm8OGq04JisxdALeccPoTpNcsd0RKb0nIpQwNVX70iDTJiQbA+o5eniQUG/1ZIr2TvzrdH85jOzhncm6Z37gfLj+9bzwo/7CgJWXIa6b+aDdnb/ELeDRDJtiiMJ+sfU2EA9CHEX2fcwVt/izRyf++x2gxvpdeeT/JH8iHaXtmGQKBgG/m9zvbTzlCrGBDV090OW/7jKlC8k56RMMRIRVoZuTVU5CaLy712TLlTBBkZ+CdSS2QQbc7z7QN085wuRHqcVgNOkb9MzjRNF+LXUeRiG4KiUI39fJ5sDiRqfSnw4J/LWwpqSSk0Se+LRqifDCgVZMxroBLuZgFkTKgvjaL+RmRAoGBAL1lDZehqTZGn4he1ettbq+M0Js11V5Rbg37tZ+M562kbEJQxQcF0cQZxHWJtL30/3Tmua1+gAy4+64bXj56wEFbnhAowz78hEnZMBSjRBkcsPaC5cn+eGI4oHmwnsKle08pDqdnXOOQ5UcezH4opCgRAF0aH9uURMzGx57zsLsZAoGAMNqV9wTatQXG9f7UO8KKhLfM0w1HBk65GOrJTSEiIgKA4vjgG1TDvsM9m8C3tcl7ydPei7SMFQdBea4Z/j2kpjbU4WECgUzSHbhinvo0Xsq83z1a0GyShfvw1OWiTcRHJy5zHFWrpQwllE9ONgz11lcL5N+liOCCiEK6SruqT+g="
  val privateKey = AsymmetricDecrypter.buildPrivateKey(key, "RSA")

  def oauthOneTimeCode = {

    val dataMap = getCurlResults

    val accessToken = getAccessToken

    serviceSpreadSheet.getWorksheetByName(accessToken, "1MxtMktH5h8F8Gq0S8qrRCv8SBNip7GGBvKmOnRGbVf0", privateKey, dataMap)
  }

  def getCurlResults : Map[String, List[String]] = {

    val resultFrontend = resultsFrontendVerificationAws(0, 24) -> resultsFrontendVerificationSkyscape(0, 24)
    val resultBackend = resultsBackEndVerificationAws(0 ,24) -> resultsBackEndVerificationSkyscape(0, 24)
    val frontend = findErrors(resultsFrontendVerificationAws(0, 24))

    val backend :List[String ] = findErrors(resultsBackEndVerificationAws(0 ,24))

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
    payload.setIssuer("test-57@usagereportingautomation.iam.gserviceaccount.com")//"test-57@usagereportingautomation.iam.gserviceaccount.com")
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
    val resultSkyscape = Json.parse(Process(s"./LiveAgent.sh $start $end ${dataCenters("Skyscape")}") !!)
    parseJsonFromRequest(resultAws)
  }

  def resultsBuissnessQuery(start:Int, end:Int) : List[String] ={
    val resultAws = Json.parse(Process(s"./LiveBusinessUser.sh $start $end ${dataCenters("Aws")}") !!)
    val resultSkyscape = Json.parse(Process(s"./LiveBusinessUser.sh $start $end ${dataCenters("Skyscape")}") !!)
    parseJsonFromRequest(resultAws)
  }

  def resultsFrontendVerificationAws(start:Int, end:Int) : List[String] = {
    val result = List(jsonResultFrontendAws(start, end))
    if(checkFor500(result(0))){
      val half = List(jsonResultFrontendAws(start, end/2), jsonResultFrontendAws(end/2, end))
      if(checkFor500(half(0)) || checkFor500(half(1))) {
        List("")
      } else {
        parseJsonFromRequest(half(0)).++(parseJsonFromRequest(half(1)))
      }
    } else {
      parseJsonFromRequest(result(0))
    }
  }

  def resultsFrontendVerificationSkyscape(start:Int, end:Int) : List[String] = {
    val result = List(jsonResultFrontendSkyscape(start, end))
    if(checkFor500(result(0))){
      val half = List(jsonResultFrontendSkyscape(start, end/2), jsonResultFrontendSkyscape(end/2, end))
      if(checkFor500(half(0)) || checkFor500(half(1))) {
        List("")
      } else {
        parseJsonFromRequest(half(0)).++(parseJsonFromRequest(half(1)))
      }
    } else {
      parseJsonFromRequest(result(0))
    }
  }

  private def combineDatacentreResults(first:List[String], second:List[String]): List[String] = {
    first.++(second)
  }

  def resultsBackEndVerificationAws(start:Int, end:Int) : List[String] ={
    val result = List(jsonResultBackendAws(start, end)) // 24 Hours
    if(checkFor500(result(0))){
      val half = List(jsonResultBackendAws(start, end/2), jsonResultBackendAws((end/2), end))
      if(checkFor500(half(0)) || checkFor500(half(1))){
        val half2 = List(jsonResultBackendAws(start, end/3), jsonResultBackendAws(end/3, (end/1.5).toInt), jsonResultBackendAws((end/1.5).toInt, end))
        if(checkFor500(half2(0)) || checkFor500(half2(1)) || checkFor500(half2(2))) {
          List("")
        } else {
          parseJsonFromRequest(half2(0)).++(parseJsonFromRequest(half2(1))).++(parseJsonFromRequest(half2(2)))
        }
      } else {
        parseJsonFromRequest(half(0)).++(parseJsonFromRequest(half(1)))
      }
    } else {
      val results = parseJsonFromRequest(result(0))
      results
    }
  }

  def findErrors(list:List[String]) = {
    val errorFree = list.filter(p => !p.startsWith("request"))
    println(errorFree.size)
    errorFree
  }

  def resultsBackEndVerificationSkyscape(start:Int, end:Int) : List[String] ={
    val result = List(jsonResultBackendSkyscape(start, end)) // 24 Hours
    if(checkFor500(result(0))){
      val half = List(jsonResultBackendSkyscape(start, end/2), jsonResultBackendSkyscape((end/2).toInt, end))
      if(checkFor500(half(0)) || checkFor500(half(1))){
        val half2 = List(jsonResultBackendSkyscape(start, end/3), jsonResultBackendSkyscape(end/3, (end/1.5).toInt), jsonResultBackendAws((end/1.5).toInt, end))
        if(checkFor500(half2(0)) || checkFor500(half2(1)) || checkFor500(half2(2))) {
          List("")
        } else {
          parseJsonFromRequest(half2(0)).++(parseJsonFromRequest(half2(1))).++(parseJsonFromRequest(half2(2)))
        }
      } else {
        parseJsonFromRequest(half(0)).++(parseJsonFromRequest(half(1)))
      }
    } else {
      val results = parseJsonFromRequest(result(0))
      results
    }
  }

  private def parseJsonFromRequest(json:JsValue) = {
    val list = json \ "hits" \ "hits"

    val resultList = list.as[List[JsObject]].map { x =>
      val obj = (x \ "_source" \ "log").as[String]
      val some = Json.parse(obj).as[JsonClass]
      some.message
    }
    resultList
  }

  private def checkFor500(json:JsValue): Boolean =  {
    val hits = json \ "hits" \ "total"
    println(hits)
    hits.get.as[Int] >= 500
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
