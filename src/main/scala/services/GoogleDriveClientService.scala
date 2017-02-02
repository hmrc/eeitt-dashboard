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

package services

import java.security.PrivateKey

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import models.DriveResource

import collection.JavaConverters._

class GoogleDriveClientService {

  def googleDriveApiForToken(accessToken: String, privatekey: PrivateKey): Drive = {

    val httpTransport = new NetHttpTransport
    val jsonFactory = new JacksonFactory

    val scopes : java.util.Collection[String] = Seq("https://spreadsheets.google.com/feeds/spreadsheets/", "https://www.googleapis.com/auth/spreadsheets", "https://www.googleapis.com/auth/drive").asJava

    val credential = new GoogleCredential.Builder()
      .setJsonFactory(jsonFactory)
      .setTransport(httpTransport)
        .setClientSecrets("1070447028534-ncaksv124efuphqnn1iorvd15er7h32f.apps.googleusercontent.com", "o5fK6stoVWXx7TZvOjUf2X3Z")
      .build()
    credential.setAccessToken(accessToken)
    //Create a new authorized API client
    new Drive.Builder(httpTransport, jsonFactory, credential).build()
  }

//  def createSpreadsheetOnDrive(accessToken: String, nameOfFile: String, privateKey: PrivateKey) = {
//
//    val service = googleDriveApiForToken(accessToken, privateKey)
//    val body = new File
//    body.setMimeType("application/vnd.google-apps.spreadsheet")
//    val docType = "spreadsheet"
//    body.setTitle(nameOfFile)
//    val file = service.files.insert(body).execute
//    DriveResource(file.getAlternateLink, file.getTitle(), file.getThumbnailLink())
//  }
}
