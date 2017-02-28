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

package googleapi

import java.io.{File, FileInputStream, InputStream, InputStreamReader}
import java.util.Collections

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets, GoogleCredential}
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
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import play.api.libs.json.JsObject
import services.{AuthService, GoogleSheetsService, tokenUrlBase}

import scalaj.http.Http

object GoogleSetup {
  val authService = new AuthService
  val serviceSpreadSheet = new GoogleSheetsService

//  No longer used due to more convient setup found, however could be useful in near future.

//  def getAccessToken: String = {
//    val signature = PreconfiguredJWT.createPreConfiguredJWT
//    authService.buildCredentialServiceAccount(signature).accessToken
//  }

//  def authorize = {
//    authService.computeAuthorise()
//  }

  def printCurlResults(curlResults : Map[String, List[String]], successResults : Map[String, List[JsObject]]) = {
    serviceSpreadSheet.print(curlResults,successResults)
  }

  def oauthOneTimeCode(curlResults: Map[String, List[String]], successResults : Map[String, List[JsObject]]): AppendValuesResponse = {
    val accessToken = authService.authorise()
    serviceSpreadSheet.populateWorksheetByFileId(accessToken, curlrequests.loadApp.fileId, curlResults, successResults)
  }
}
