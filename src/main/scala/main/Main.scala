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

package main

import akka.actor.Status.Success
import curlrequests.{CurlByDatabase, SuccessfulSubmissions}
import googleapi.GoogleSetup
import models.{DataCentre, LotteryDuty, QA, SkyScape}

object Main extends App {
//  val inst = new CurlByDatabase(QA) //QA - Qa database
//  val curlResults = inst.getCurlResults
//  val successResults = inst.getSuccessResults
//
//  GoogleSetup.printCurlResults(curlResults, successResults)

  val skyscape = new CurlByDatabase(SkyScape) //SkyScape - SkyScape database
  val curlResultsSkyScape = skyscape.getCurlResults
  val successResultsSkyScape = skyscape.getSuccessResults

  val dataCentre = new CurlByDatabase(DataCentre) //DateCentre - DataCentre database
  val curlResultsDataCentre = dataCentre.getCurlResults
  val successResultsDataCentre = dataCentre.getSuccessResults

  if(curlrequests.compareDataCentreResults(curlResultsDataCentre, curlResultsDataCentre)){
    GoogleSetup.printCurlResults(curlResultsDataCentre, successResultsDataCentre)
  } else {
    println("DATACENTRES WERE NOT EQUAL POTENTIAL ERROR")
  }

//  GoogleSetup.oauthOneTimeCode(curlResults)
}
