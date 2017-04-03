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

import curlrequests._
import googleapi.GoogleSetup
import models.{DataCentre, SkyScape}
import play.api.Logger

//sbt "run-main main.Print

object Print extends App {

  val skyscape = new CurlByDatabase(SkyScape)
  Logger.info("Getting SkyScape Results")
  val resultsSkyScape : Map[String, List[String]] = skyscape.getResults

  val dataCentre = new CurlByDatabase(DataCentre)
  Logger.info("Getting DataCentre Results")
  val resultsDataCentre : Map[String, List[String]] = dataCentre.getResults

  val isDataCentresEqual : Boolean = compareDataCentreResults(resultsDataCentre, resultsSkyScape)

  Logger.debug("Are the databases returns Equal results : - "+isDataCentresEqual)
  if(curlrequests.compareDataCentreResults(resultsDataCentre, resultsDataCentre)){
    GoogleSetup.printResults(resultsDataCentre)
  } else {
    Logger.error("DATACENTRES WERE NOT EQUAL POTENTIAL ERROR")
  }
}
