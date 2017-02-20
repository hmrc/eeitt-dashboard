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

package curlrequests

import models.{Env, Form, LogLineContents}
import play.api.Logger
import play.api.libs.json.{JsError, JsObject, JsSuccess, JsValue}

import scala.sys.process.Process

class SuccessfulSubmissions(form: Form, dataCentre : String) {

  def getSuccessResults = {
    parseJsonFromRequestSuccessfulSubmissions(resultsSuccessQuery(0, 24))
  }

  def resultsSuccessQuery(start : Int, end : Int) = {
    play.api.libs.json.Json.parse(Process(s"./Success.sh $start $end  ${form.value} $dataCentre") !! )
  }

  def parseJsonFromRequestSuccessfulSubmissions(json: JsValue) : List[JsObject] = {
    val list = json \ "hits" \ "hits"

    list.validate[List[JsObject]] match {
      case JsSuccess(x, _) => x
      case JsError(err) =>
        Logger.logger.error(err.toString)
        throw new IllegalArgumentException("No hits inside the logs returned, perhaps date/times are wrong")
    }
  }
}
