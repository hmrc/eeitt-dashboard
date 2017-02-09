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

import models.{GoogleApp, JsonClass}
import play.api.libs.json.{JsObject, JsValue}
import uk.gov.hmrc.secure.AsymmetricDecrypter

/**
  * Created by harrison on 08/02/17.
  */
package object controllers {
  val key: String = loadApp.privateKey
  lazy val loadApp = services.Json.fromJson[GoogleApp](scala.io.Source.fromFile("src/main/resources/serviceAccount.json").mkString)
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

  def parseJsonFromRequest(json: JsValue) = {
    val list = json \ "hits" \ "hits"

    list.as[List[JsObject]].map { x =>
      (x \ "_source" \ "log").validate[String].map {
        case y =>
          val some = play.api.libs.json.Json.parse(y).as[JsonClass]
          some.message
        case _ => ""
      }.get
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

  def compareDataCentreResults(first: List[String], second: List[String]): Boolean = {
    first.size == second.size
  }
}
