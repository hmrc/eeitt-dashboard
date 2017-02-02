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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.netaporter.uri.dsl._
import models._

import scalaj.http.{HttpResponse, _}

class AuthService {

  def userUri: String = {
    val url = authUrlBase ?
      ("response_type" -> "code") &
      ("client_id"-> "1070447028534-ncaksv124efuphqnn1iorvd15er7h32f.apps.googleusercontent.com") &
      ("redirect_uri" -> "http://localhost:9000/googlereporting/result") &
      ("scope" -> "https://spreadsheets.google.com/feeds/spreadsheets https://www.googleapis.com/auth/spreadsheets") &
      ("state" -> "beekeeper!") &
      ("login_hint" -> "daniel.connelly@digital.hmrc.gov.uk") &
      ("include_granted_scopes" -> "true")
    url.toString
  }

  def buildCredentialServiceAccount(string: String):TokenResponseSA = {
    println("Inside BuildCredentialServiceAccount")
    val response: HttpResponse[String] = Http(tokenUrlBase).postForm(Seq(
      "grant_type" -> "urn:ietf:params:oauth:grant-type:jwt-bearer",
      "assertion" -> string
    )).asString

    response.code match {
      case 200 => Json.fromJson[TokenResponseSA](response.body, true)
      case 401 => buildCredentialServiceAccount(string)
      case _ => throw new Exception("OAuth Failed with code %d: %s".format(response.code, response.body))
    }
  }

  def buildCredentialClient(code: String): GoogleCredentialC = {
    val response: HttpResponse[String] = Http(tokenUrlBase).postForm(Seq(
      "code" -> code,
      "client_id" -> "1070447028534-ncaksv124efuphqnn1iorvd15er7h32f.apps.googleusercontent.com",
      "client_secret" -> "o5fK6stoVWXx7TZvOjUf2X3Z",
      "redirect_uri" -> "http://localhost:9000/googlereporting/result",
      "grant_type" -> "authorization_code",
      "access_type" -> "offline"
    )).asString

    val tokenData = response.code match {
      case 200 => Json.fromJson[TokenResponseC](response.body, true)
      case _ => throw new Exception("OAuth Failed with code %d: %s".format(response.code, response.body))
    }

    GoogleCredentialC(
      None,
      1,
      tokenData.accessToken,
      tokenData.refreshToken
    )
  }

  def refreshCredential(googleCredential: GoogleCredentialC): GoogleCredentialC = {
//    val creds = loadApp //see above
    val response: HttpResponse[String] = Http(tokenUrlBase).postForm(Seq(
      "refresh_token" -> "1/7GoptcivVmjDRCKZzKgJtznwvGPJt2cg49Av1HNopGQ",
      "client_id" -> "1070447028534-ncaksv124efuphqnn1iorvd15er7h32f.apps.googleusercontent.com",
      "client_secret" -> "o5fK6stoVWXx7TZvOjUf2X3Z",
      "grant_type" -> "refresh_token"
    )).asString

    val tokenData = response.code match {
      case 200 => Json.fromJson[RefreshTokenResponse](response.body, true)
      case _ => throw new Exception("OAuth Failed with code %d: %s".format(response.code, response.body))
    }

    googleCredential.copy(accessToken = tokenData.accessToken)
  }

}
