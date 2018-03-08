/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.{ LocalDate, Period, ZoneId }

import play.api.libs.json.{ JsValue, Json }
import org.joda.time.DateTime
import play.api.Logger
import uk.gov.hmrc.play.http.{ HeaderCarrier, HttpPost }

import scala.concurrent.Future
import scala.sys.process.Process
import scala.util.{ Failure, Success, Try }
import scalaj.http.{ Http, HttpResponse }

class Agents(dataCentre: String, numberOfDays: Int) {

  def getResults: List[String] = {
    splitRequest(0, 24, is500, parseJsonFromRequest, queryResults)
  }

  def queryResults(start: Float, end: Float): JsValue = {
    Logger.debug(s"quering Agents")
    play.api.libs.json.Json.parse(call(start, end, queryJson, dataCentre).body)
  }

  def queryJson(start: Float, end: Float): String = {
    s"""{
      |  "size": 500,
      |  "query": {
      |    "bool": {
      |      "must": [
      |        {
      |          "query_string": {
      |            "analyze_wildcard": true,
      |            "query": "app:eeitt AND NOT app:\\"eeitt-frontend\\" AND \\"registration of agent\\""
      |          }
      |        },
      |        {
      |          "range": {
      |            "@timestamp": {
      |              "gte": ${millis(start, numberOfDays)},
      |              "lte": ${millis(end, numberOfDays)},
      |              "format": "epoch_millis"
      |            }
      |          }
      |        }
      |      ],
      |      "must_not": []
      |    }
      |  }
      |}""".stripMargin
  }

}
