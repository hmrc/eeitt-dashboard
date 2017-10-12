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

package uk.gov.hmrc.eeittdashboard

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

import java.security.PrivateKey
import java.time.{ LocalDate, Period, ZoneId }

import models.{ Credentials, GoogleApp, LogLineContents, NumberOfDays }
import play.api.Logger
import play.api.libs.json._
import pureconfig.loadConfigOrThrow
import uk.gov.hmrc.secure.AsymmetricDecrypter

import scalaj.http.{ Http, HttpOptions }

package object curlrequests {

  lazy val loadApp = services.Json.fromJson[GoogleApp](scala.io.Source.fromFile("src/main/resources/serviceAccount.json").mkString)

  case class AuthCredentials(username: String, password: String)
  val credentials = loadConfigOrThrow[AuthCredentials]("auth")
  //  val key: String = loadApp.privateKey
  //  val privateKey: PrivateKey = AsymmetricDecrypter.buildPrivateKey(key, "RSA")

  def call(start: Float, end: Float, json: (Float, Float) => String, url: String) = {
    Http(url)
      .header("kbn-xsrf", "kibana")
      .header("Content-Type", "application/json")
      .auth(credentials.username, credentials.password)
      .options(HttpOptions.readTimeout(1000000000))
      .postData(json(start, end)).asString
  }

  def millis(int: Float, numberOfDays: Int): Long = {
    val b: Long = (int * 3600000).toLong
    val x: Long = (LocalDate.now.minus(Period.ofDays(numberOfDays)).atStartOfDay(ZoneId.of("Europe/London")).toEpochSecond * 1000).toLong + b
    println(x + "THIS IS X")
    x
  }

  def splitRequest(start: Float, end: Float, numElements: (JsValue) => Int, elements: (JsValue) => List[String], result: (Float, Float) => JsValue): List[String] = {

    val res = result(start, end)
    Logger.debug("RES: Split request" + Json.prettyPrint(res))
    if (numElements(res) <= 500) {
      elements(res)

    } else {
      val b = (end - start) % 2
      println("this is B : " + b)
      val middle: Float = ((end - start) / 2) + start

      splitRequest(start, middle, numElements, elements, result) ::: splitRequest(middle, end, numElements, elements, result)

    }
  }

  def parseJsonFromRequest(json: JsValue): List[String] = {
    val list = json \ "hits" \ "hits"

    list.validate[List[JsObject]] match {
      case JsSuccess(x, _) =>
        x.map { b =>
          (b \ "_source" \ "log").validate[String] match {
            case JsSuccess(y, _) =>
              val some = play.api.libs.json.Json.parse(y).as[LogLineContents]
              some.message
            case JsError(err) =>
              Logger.logger.error(err.toString)
              throw new IllegalArgumentException("no strings inside the individual logs meaning pulling wrong data out of kibana")
          }
        }
      case JsError(err) =>
        Logger.logger.error(err.toString)
        throw new IllegalArgumentException("No hits inside the logs returned, perhaps date/times are wrong")
    }
  }

  def is500(json: JsValue): Int = {
    (json \ "hits" \ "total").get.as[Int]
  }

  def filterErrors(list: List[String]): List[String] = {
    list.filter(p => !p.startsWith("request"))
  }

  def compareDataCentreResults(first: Map[String, List[String]], second: Map[String, List[String]]): Boolean = {
    val firstList: List[Int] = first.values.map(x => x.size).toList
    val secondList: List[Int] = second.values.map(y => y.size).toList
    firstList.sum == secondList.sum
  }
}

