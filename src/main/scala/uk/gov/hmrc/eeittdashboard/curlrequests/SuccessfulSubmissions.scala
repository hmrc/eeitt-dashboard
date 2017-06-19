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

package uk.gov.hmrc.eeittdashboard.curlrequests

import uk.gov.hmrc.eeittdashboard.models.{Environment, Form, LogLineContents}
import play.api.Logger
import play.api.libs.json._

import scala.sys.process.Process

class SuccessfulSubmissions(form: Form, dataCentre : String) extends Curl {

  def getResults: List[String] = {
    Logger.debug(s"Getting successful submission for ${form.value}")
    splitRequest(0, 24, is500, parseJsonFromRequestSuccessfulSubmissions, queryResults)
  }

  def queryResults(start : Int, end : Int): JsValue = {
    Logger.debug(s"quering $dataCentre for successful submissions with parameters : - Start = $start, End = $end Form = ${form.value}")
    play.api.libs.json.Json.parse(Process(s"./Success.sh $start $end  ${form.value} $dataCentre $numberOfDays").!! )
  }

  def parseJsonFromRequestSuccessfulSubmissions(json: JsValue) : List[String] = {
    (json \ "hits" \ "hits").validate[List[JsObject]] match {
      case JsSuccess(x, _) =>
        x match {
          case Nil => Nil
          case listOfObjects => listOfObjects.flatMap{ singleObject =>
            (singleObject \ "_source" \ "status").validate[Int] match {
              case JsSuccess(status, _) =>
                Logger.debug(s"there was a successful hit for ${form.value}")
                List(status.toString)
              case JsError(err) =>
                Logger.debug(s"There wasn't a successful hit for ${form.value}")
                List()
            }
          }
        }
      case JsError(err) =>
        Logger.debug(err.toString)
        throw new IllegalArgumentException("no hits inside the logs returned, perhaps date/times are wrong")
    }
  }
}
