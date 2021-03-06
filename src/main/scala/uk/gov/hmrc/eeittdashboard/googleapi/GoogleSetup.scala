/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.eeittdashboard.googleapi

import java.io.{ File, FileInputStream, InputStream, InputStreamReader }
import java.util.Collections

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder
import com.google.api.client.googleapis.auth.oauth2.{ GoogleAuthorizationCodeFlow, GoogleClientSecrets, GoogleCredential }
import com.google.api.client.googleapis.compute.ComputeCredential
import com.google.api.client.googleapis
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.{ Sheets, SheetsScopes }
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import play.api.libs.json.JsObject
import uk.gov.hmrc.eeittdashboard.curlrequests
import uk.gov.hmrc.eeittdashboard.curlrequests.NumberOfDays
import uk.gov.hmrc.eeittdashboard.services.GoogleSheetsService
import uk.gov.hmrc.eeittdashboard.services.{ AuthService, GoogleSheetsService, tokenUrlBase }

import scalaj.http.Http

object GoogleSetup {

  val authService = new AuthService
  def serviceSpreadSheet(numberOfDays: Int) = new GoogleSheetsService(numberOfDays)

  //  No longer used due to more convient setup found, however could be useful in near future.

  //  def getAccessToken: String = {
  //    val signature = PreconfiguredJWT.createPreConfiguredJWT
  //    authService.buildCredentialServiceAccount(signature).accessToken
  //  }

  //  def authorize = {
  //    authService.computeAuthorise()
  //  }

  //Used to Dependency.
  def writeToSpreadSheet(accessToken: String, results: Map[String, List[String]], numberOfDays: Int) = {
    val credential = authService.passAuthToken(accessToken)
    serviceSpreadSheet(numberOfDays).populateWorksheetByFileId(credential, curlrequests.loadApp.fileId, results)
  }

  def printResults(results: Map[String, List[String]], numberOfDays: Int) = {
    serviceSpreadSheet(numberOfDays).print(results)
  }

  def oauthOneTimeCode(results: Map[String, List[String]], numberOfDays: Int): AppendValuesResponse = {
    val accessToken = authService.authorise()
    serviceSpreadSheet(numberOfDays).populateWorksheetByFileId(accessToken, curlrequests.loadApp.fileId, results)
  }
}
