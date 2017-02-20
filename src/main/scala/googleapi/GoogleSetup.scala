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

import com.google.api.services.sheets.v4.model.AppendValuesResponse
import play.api.libs.json.JsObject
import services.{AuthService, GoogleSheetsService}

object GoogleSetup {
  val authService = new AuthService
  val serviceSpreadSheet = new GoogleSheetsService

  def getAccessToken: String = {
    val signature = PreconfiguredJWT.createPreConfiguredJWT
    authService.buildCredentialServiceAccount(signature).accessToken
  }

  def printCurlResults(curlResults : Map[String, List[String]], successResults : Map[String, List[JsObject]]) = {
    serviceSpreadSheet.print(curlResults,successResults)
  }

  def oauthOneTimeCode(curlResults: Map[String, List[String]], successResults : Map[String, List[JsObject]]): AppendValuesResponse = {

    val accessToken = getAccessToken
    serviceSpreadSheet.populateWorksheetByFileId(accessToken, curlrequests.loadApp.fileId, curlrequests.privateKey, curlResults, successResults)
  }
}
