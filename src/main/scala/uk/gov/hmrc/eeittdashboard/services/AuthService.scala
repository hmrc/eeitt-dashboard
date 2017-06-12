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

package uk.gov.hmrc.eeittdashboard.services

import java.io.File

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.SheetsScopes

class AuthService {

  val AppName = "EEITT_LOGGING"

  val DATA_STORE_DIR = new File("sheets.googleapis.com-java-quickstart")


  val JSON_FACTORY = JacksonFactory.getDefaultInstance
  val SCOPES = SheetsScopes.all()

  var HTTP_TRANSPORT: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
  var DATA_STORE_FACTORY : FileDataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR)

  //this method is to test outwith domain.

//  def buildServiceAccountCredential() = {
//    val credential = new GoogleCredential.Builder()
//      .setTransport(HTTP_TRANSPORT)
//      .setJsonFactory(JSON_FACTORY)
//      .setServiceAccountId(uk.gov.hmrc.eeittdashboard.curlrequests.loadApp.clientEmail)
//      .setServiceAccountPrivateKey(uk.gov.hmrc.eeittdashboard.curlrequests.privateKey)
//      .setServiceAccountScopes(SheetsScopes.all())
//      .build()
//    credential.refreshToken()
//
//    credential.getAccessToken
//
//    val permission = new Permission()
//    permission.setEmailAddress("daniel.connelly@digital.hmrc.gov.uk")
//    permission.setType("user")
//    permission.setRole("writer")
//    val drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
//      .setApplicationName("test")
//      .build()
//
//    val service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
//      .setApplicationName("test")
//      .build()
//
//    val spreadsheeets = service.spreadsheets().create(new com.google.api.uk.gov.hmrc.eeittdashboard.services.sheets.v4.model.Spreadsheet).execute()
//
//    drive.permissions().create("1u1uZk9DLzx-gfEfZVYJ43KJzniTrkDQlq5W2SijZy60", permission).execute()
//    //    spreadsheeets.getSpreadsheetId
//  }

  def authorise() = {
    val in = scala.io.Source.fromFile("src/uk.gov.hmrc.eeittdashboard.main/resources/servicedata.json").reader()
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, in)
    val flow = new Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
      .setDataStoreFactory(DATA_STORE_FACTORY)
      .setAccessType("offline")
      .build()
    new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")
  }

//  Not used but could be quite useful for other projects.

//  def buildCredentialServiceAccount(string: String): TokenResponse = {
//    _buildCredentialServiceAccount(string, 0)
//  }
//
//  def _buildCredentialServiceAccount(string: String, numRetry: Int): TokenResponse = {
//    val response: HttpResponse[String] = Http(tokenUrlBase).postForm(Seq(
//      "grant_type" -> "urn:ietf:params:oauth:grant-type:jwt-bearer",
//      "assertion" -> string
//    )).asString
//
//    (response.code, numRetry) match {
//      case (200, _) => Json.fromJson[TokenResponse](response.body, true)
//      case (401, n) if (n < 5) => _buildCredentialServiceAccount(string, numRetry+1)
//      case _ => throw new Exception("OAuth Failed with code %d: %s".format(response.code, response.body))
//    }
//  }
}
