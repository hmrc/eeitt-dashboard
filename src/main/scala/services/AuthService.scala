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

  def buildCredentialServiceAccount(string: String):TokenResponseSA = {
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
}
