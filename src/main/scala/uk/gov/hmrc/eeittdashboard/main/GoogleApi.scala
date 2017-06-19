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

import akka.actor.Status.Success
import play.api.Logger
import pureconfig.loadConfigOrThrow
import uk.gov.hmrc.eeittdashboard.curlrequests
import uk.gov.hmrc.eeittdashboard.curlrequests.{CurlByDatabase, NumberOfDays, SuccessfulSubmissions}
import uk.gov.hmrc.eeittdashboard.googleapi.GoogleSetup
import uk.gov.hmrc.eeittdashboard.models.{DataCentre, QA, SkyScape}

//sbt "run-uk.gov.hmrc.eeittdashboard.main uk.gov.hmrc.eeittdashboard.main.GoogleApi"

object GoogleApi extends App {

  //Logger.info(loadConfigOrThrow[NumberOfDays]("numberofdays").days.toString)
//  val skyscape = new CurlByDatabase(SkyScape) //SkyScape - SkyScape database
//  Logger.info("Getting SkyScape Results")
//  val curlResultsSkyScape = skyscape.getResults
//  Logger.info("Getting DataCentre Results")
//  val dataCentre = new CurlByDatabase(DataCentre) //DateCentre - DataCentre database
//  val curlResultsDataCentre = dataCentre.getResults
  val qa = new CurlByDatabase(QA)
  val results = qa.getResults
  if(args.isEmpty) {
//    if (curlrequests.compareDataCentreResults(curlResultsDataCentre, curlResultsDataCentre)) {
      Logger.info("Installed application flow : - ")
    GoogleSetup.oauthOneTimeCode(results)
//    } else {
//      println("DATACENTRES WERE NOT EQUAL POTENTIAL ERROR")
//    }
  } else {
    Logger.info("Admin Console flow : - ")
    GoogleSetup.writeToSpreadSheet(args(0), results)
  }
//  GoogleSetup.oauthOneTimeCode(curlResults)
}
