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

import java.time.{LocalDate, Period}

import models._
import play.api.libs.json.JsObject

class CurlByDatabase(environment: Environment) {

  val date = LocalDate.now.minus(Period.ofDays(1)).toString.replace("-", ".")
  val dataCentres = Map(
    "Qa" -> "https://kibana-datacentred-sal01-qa.tax.service.gov.uk/elasticsearch/logstash-qa*/_search?pretty",
    "DataCentre" -> s"https://kibana-datacentred-sal01-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty",
    "SkyScape" -> s"https://kibana-skyscape-farnborough-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty"
  )

  val agents = new Agents(dataCentres(environment.value))
  val businessUser = new BusinessUser(dataCentres(environment.value))
  val frontendVerification = new FrontEndVerification(dataCentres(environment.value))
  val backendVerification = new BackendVerification(dataCentres(environment.value))

  val lotteryDuty = new SuccessfulSubmissions(LotteryDuty, dataCentres(environment.value))
  val gamingDuty = new SuccessfulSubmissions(GamingDuty, dataCentres(environment.value))
  val gamingDutyPayment = new SuccessfulSubmissions(GamingDutyPayment, dataCentres(environment.value))
  val airPassengerDuty = new SuccessfulSubmissions(AirPassengerDuty, dataCentres(environment.value))
  val landFill = new SuccessfulSubmissions(LandFill, dataCentres(environment.value))
  val aggregateLevy = new SuccessfulSubmissions(AggregateLevy, dataCentres(environment.value))
  val bingoDuty = new SuccessfulSubmissions(BingoDuty, dataCentres(environment.value))
  val insurancePremiumTax = new SuccessfulSubmissions(InsurancePremiumTax, dataCentres(environment.value))

  def getSuccessResults : Map[String, List[JsObject]] = {
    Map(
      "LotteryDuty" -> lotteryDuty.getResults,
      "GamingDuty" -> gamingDuty.getResults,
      "GamingDutyPayment" -> gamingDutyPayment.getResults,
      "AirPassengerDuty" -> airPassengerDuty.getResults,
      "LandFill" -> landFill.getResults,
      "AggregatesLevy" -> aggregateLevy.getResults,
      "BingoDuty" -> bingoDuty.getResults,
      "InsurancePremiumTax" -> insurancePremiumTax.getResults
    )
  }

  def getCurlResults: Map[String, List[String]] = {
    Map(
      "BusinessUsers" -> businessUser.getBusinessResults,
      "Agents" -> agents.getResults,
      "Backend" -> backendVerification.getResults,
      "Frontend" -> frontendVerification.getFrontendResults
    )
  }
}
