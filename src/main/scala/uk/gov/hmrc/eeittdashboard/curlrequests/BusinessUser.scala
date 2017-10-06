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

import cats.data.Validated
import cats.data.Validated.Valid
import play.api.Logger
import play.api.libs.json.JsValue

import scala.sys.process.Process

class BusinessUser(dataCentre: String) {

  def getResults: List[String] = {
    splitRequest(0, 24, is500, parseJsonFromRequest, queryResults)
  }

  def queryResults(start: Float, end: Float): JsValue = {
    Logger.debug(s"quering BusinessUsers")
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
      |            "query": "app:eeitt AND NOT app:\\"eeitt-frontend\\" AND \\"registration of business\\""
      |          }
      |        },
      |        {
      |          "range": {
      |            "@timestamp": {
      |              "gte": ${millis(start)},
      |              "lte": ${millis(end)},
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