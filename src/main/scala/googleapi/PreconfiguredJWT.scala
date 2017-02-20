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

import java.time.Instant

import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.json.webtoken.{JsonWebSignature, JsonWebToken}

/**
  * Created by daniel-connelly on 09/02/17.
  */
object PreconfiguredJWT {

  def createPreConfiguredJWT: String = {
    val jsonFactory = new JacksonFactory
    JsonWebSignature.signUsingRsaSha256(curlrequests.privateKey, jsonFactory, header, payloadForSheets)
  }

  private def header: JsonWebSignature.Header = {
    val header = new JsonWebSignature.Header
    header.setAlgorithm("RS256")
    header.setType("JWT")
    header
  }

  private def payloadForSheets: JsonWebToken.Payload = {
    val issueTime = Instant.now.getEpochSecond
    val exp = Instant.now.plusSeconds(600).getEpochSecond

    val payload = new JsonWebToken.Payload
    payload.set("scope", "https://spreadsheets.google.com/feeds/spreadsheets https://www.googleapis.com/auth/spreadsheets https://www.googleapis.com/auth/drive")
    payload.setIssuer(curlrequests.loadApp.clientEmail)
    payload.setAudience("https://www.googleapis.com/oauth2/v4/token")
    payload.setSubject(curlrequests.loadApp.userImpersonation)
    payload.setIssuedAtTimeSeconds(issueTime)
    payload.setExpirationTimeSeconds(exp)

    payload
  }
}
