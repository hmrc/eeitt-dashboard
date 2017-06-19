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

import play.api.libs.json.JsValue

import scala.sys.process.Process

class BackendVerification(dataCentre: String) extends Curl {

  def getResults : List[String]= {
    filterErrors(splitRequest(0, 24, is500, parseJsonFromRequest, queryResults))
  }

  def queryResults(start: Int, end: Int) : JsValue = {
    play.api.libs.json.Json.parse(Process(s"./BackendVerification.sh $start $end $dataCentre $numberOfDays").!!)
  }

}