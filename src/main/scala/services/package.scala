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

import java.net.URL

import com.fasterxml.jackson.databind.{ObjectMapper, PropertyNamingStrategy}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import models.GoogleApp

package object services {

  object Json {
    def mapper = {
      val m = new ObjectMapper() with ScalaObjectMapper
      m.registerModule(DefaultScalaModule)
      m
    }

    def fromJson[T](url: URL)(implicit m: Manifest[T]): T = {
      mapper.readValue[T](url)
    }

    def fromJson[T](value: String, allowUnderscores: Boolean = false)(implicit m: Manifest[T]): T = {
      val mapper = if(allowUnderscores) {
        val m = this.mapper
        m.setPropertyNamingStrategy(
          PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES
        )
        m
      } else this.mapper
      mapper.readValue[T](value)
    }
  }

  lazy val loadApp = Json.fromJson[GoogleApp](this.getClass.getClassLoader().getResource("src/main/serviceAccount.json"))

  val authUrlBase = "https://accounts.google.com/o/oauth2/auth"
  val tokenUrlBase = "https://www.googleapis.com/oauth2/v4/token"

}
