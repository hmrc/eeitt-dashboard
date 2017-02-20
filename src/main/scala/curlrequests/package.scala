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

import models.{GoogleApp, LogLineContents}
import play.api.Logger
import play.api.libs.json.{JsError, JsObject, JsSuccess, JsValue}
import uk.gov.hmrc.secure.AsymmetricDecrypter

/**
  * Created by harrison on 08/02/17.
  */
package object curlrequests {

  lazy val loadApp = services.Json.fromJson[GoogleApp](scala.io.Source.fromFile("src/main/resources/serviceAccount.json").mkString)
  val key: String = loadApp.privateKey
  val privateKey: PrivateKey = AsymmetricDecrypter.buildPrivateKey(key, "RSA")

  def get2(start: Int, end: Int, numElements: (JsValue) => Int, elements: (JsValue) => List[String], result: (Int, Int) => JsValue): List[String] = {

    val res = result(start, end)
    if (numElements(res) <= 500) {
      elements(res)

    } else{
      val middle = ((end - start) / 2 + (end - start) % 2)+start

      get2(start, middle, numElements, elements, result) ::: get2(middle, end, numElements, elements, result)

    }
  }

  def parseJsonFromRequest(json: JsValue) : List[String] = {
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

  def checkFor500(json: JsValue): Int = {
    val hits = json \ "hits" \ "total"
    hits.get.as[Int]
  }

  def findErrors(list: List[String]) = {
    val errorFree = list.filter(p => !p.startsWith("request"))
    errorFree
  }

  def compareDataCentreResults(first: Map[String, List[String]], second: Map[String, List[String]]): Boolean = {
    val firstList : List[Int] = first.values.map(x => x.size).toList
    val secondList : List[Int] = second.values.map(y => y.size).toList
    firstList.sum == secondList.sum
  }
}
