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

package uk.gov.hmrc.eeittdashboard.main

import uk.gov.hmrc.eeittdashboard.curlrequests.CurlByDatabase
import uk.gov.hmrc.eeittdashboard.googleapi.GoogleSetup
import uk.gov.hmrc.eeittdashboard.models.QA
import uk.gov.hmrc.eeittdashboard.services.GoogleSheetsService
import uk.gov.hmrc.eeittdashboard.services.{ AuthService, GoogleSheetsService }

//sbt "run-uk.gov.hmrc.eeittdashboard.main uk.gov.hmrc.eeittdashboard.main.Test"
object Test extends App {

  val inst = new AuthService
  val sheets = new GoogleSheetsService
  //    val inst = new CurlByDatabase(QA) //QA - Qa database
  //    val curlResults = inst.getCurlResults
  //    val successResults = inst.getSuccessResults
  //
  //    GoogleSetup.printCurlResults(curlResults, successResults)
  println(sheets.gDataApiForToken(inst.authorise()))
}
