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
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import models.JsonClass
import play.api.libs.json.{JsValue, _}
import services.{AuthService, GoogleSheetsService}

import scala.sys.process.Process

object PreconfiguredJWT {

  def createPreConfiguredJWT: String = {
    val jsonFactory = new JacksonFactory
    JsonWebSignature.signUsingRsaSha256(privateKey, jsonFactory, header, payloadForSheets())
  }

  private def header(): JsonWebSignature.Header = {
    val header = new JsonWebSignature.Header
    header.setAlgorithm("RS256")
    header.setType("JWT")
    header
  }

  private def payloadForSheets(): JsonWebToken.Payload = {
    val issueTime = Instant.now.getEpochSecond
    val exp = Instant.now.plusSeconds(600).getEpochSecond
    val payload = new JsonWebToken.Payload
    payload.set("scope", "https://spreadsheets.google.com/feeds/spreadsheets https://www.googleapis.com/auth/spreadsheets https://www.googleapis.com/auth/drive")
    payload.setIssuer(loadApp.clientEmail)
    payload.setAudience("https://www.googleapis.com/oauth2/v4/token")
    payload.setSubject(loadApp.userImpersonation)
    payload.setIssuedAtTimeSeconds(issueTime)
    payload.setExpirationTimeSeconds(exp)
    payload
  }
}

class WriteInteraction {
  val dataCentres = Map(
    "Qa" -> "https://kibana-datacentred-sal01-qa.tax.service.gov.uk/elasticsearch/logstash-qa*/_search?pretty",
    "Aws" -> "https://kibana-datacentred-sal01-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty",
    "Skyscape" -> "https://kibana-skyscape-farnborough-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty"
  )

  val agents = new Agents(dataCentres("Qa"))
  val businessUser = new BusinessUser(dataCentres("Qa"))
  val authService = new AuthService
  val serviceSpreadSheet = new GoogleSheetsService
  val frontendVerification = new FrontEndVerification(dataCentres("Qa"))
  val backendVerification = new BackendVerification(dataCentres("Qa"))
  
  def oauthOneTimeCode: AppendValuesResponse = {

    val dataMap = getCurlResults
    val accessToken = getAccessToken
    serviceSpreadSheet.populateWorksheetByFileId(accessToken, loadApp.fileId, privateKey, dataMap)
  }

  def getCurlResults: Map[String, List[String]] = {
    //    val resultFrontend = resultsFrontendVerificationAws(0, 24) -> resultsFrontendVerificationSkyscape(0, 24)
    //    val resultBackend = resultsBackEndVerificationAws(0, 24) -> resultsBackEndVerificationSkyscape(0, 24)
    //    val frontend = findErrors(resultsFrontendVerificationAws(0, 24))
    //
    //    val backend: List[String] = findErrors(resultsBackEndVerificationAws(0, 24))

    Map(
      "BusinessUsers" -> businessUser.resultsBuissnessQuery(0, 24),
      "Agents" -> agents.resultsAgentQuery(0, 24),
      "Backend" -> backendVerification.resultsBackEndVerificationAws(0, 24),
      "Frontend" -> frontendVerification.resultsFrontendVerification(0, 24)
    )
  }

  def getAccessToken: String = {
    val signature = PreconfiguredJWT.createPreConfiguredJWT
    authService.buildCredentialServiceAccount(signature).accessToken
  }



}
