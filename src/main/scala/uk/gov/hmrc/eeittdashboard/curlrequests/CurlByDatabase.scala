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

import java.time.{ LocalDate, Period }

import uk.gov.hmrc.eeittdashboard.models._
import play.api.libs.json.{ JsObject, Json }

class CurlByDatabase(environment: Environment, numberOfDays: Int) {

  val date: String = LocalDate.now.minus(Period.ofDays(1)).toString.replace("-", ".")
  val dataCentres: Map[String, String] = Map(
    "Qa" -> "https://kibana-datacentred-sal01-qa.tax.service.gov.uk/elasticsearch/logstash-qa*/_search",
    "DataCentre" -> s"https://kibana.tools.production.tax.service.gov.uk/elasticsearch/logstash*/_search",
    //    "DataCentre" -> s"https://kibana.tools.production.tax.service.gov.uk/_search?q=message:elasticsearch&size=0&terminate_after=1&pretty",
    "SkyScape" -> s"https://kibana-skyscape-farnborough-staging.tax.service.gov.uk/elasticsearch/logstash-production*/_search"
  )

  val agents: Agents = new Agents(dataCentres(environment.value), numberOfDays)
  val businessUser: BusinessUser = new BusinessUser(dataCentres(environment.value), numberOfDays)
  val frontendVerification: FrontEndVerification = new FrontEndVerification(dataCentres(environment.value), numberOfDays)
  val backendVerification: BackendVerification = new BackendVerification(dataCentres(environment.value), numberOfDays)

  val lotteryDuty: SuccessfulSubmissions = new SuccessfulSubmissions(LotteryDuty, dataCentres(environment.value), numberOfDays)
  val oldGamingDutyPaymentOnAccount: SuccessfulSubmissions = new SuccessfulSubmissions(OldGamingDutyPaymentOnAccount, dataCentres(environment.value), numberOfDays)
  val gamingDuty: SuccessfulSubmissions = new SuccessfulSubmissions(GamingDuty, dataCentres(environment.value), numberOfDays)
  val airPassengerDuty = new SuccessfulSubmissions(AirPassengerDuty, dataCentres(environment.value), numberOfDays)
  val landFill: SuccessfulSubmissions = new SuccessfulSubmissions(LandFill, dataCentres(environment.value), numberOfDays)
  val aggregateLevy: SuccessfulSubmissions = new SuccessfulSubmissions(AggregateLevy, dataCentres(environment.value), numberOfDays)
  val bingoDuty: SuccessfulSubmissions = new SuccessfulSubmissions(BingoDuty, dataCentres(environment.value), numberOfDays)
  val insurancePremiumTax: SuccessfulSubmissions = new SuccessfulSubmissions(InsurancePremiumTax, dataCentres(environment.value), numberOfDays)
  val gamingDutyPaymentOnAccount: SuccessfulSubmissions = new SuccessfulSubmissions(GamingDutyPaymentOnAccount, dataCentres(environment.value), numberOfDays)
  val corporateInterestAppointCompany: SuccessfulSubmissions = new SuccessfulSubmissions(CorporateInterestAppointCompany, dataCentres(environment.value), numberOfDays)
  val corporateInterestRevokeCompany: SuccessfulSubmissions = new SuccessfulSubmissions(CorporateInterestRevokeCompany, dataCentres(environment.value), numberOfDays)
  val gasAsRoadFuel: SuccessfulSubmissions = new SuccessfulSubmissions(GasAsRoadFuel, dataCentres(environment.value), numberOfDays)
  val biofuels: SuccessfulSubmissions = new SuccessfulSubmissions(Biofuels, dataCentres(environment.value), numberOfDays)

  def getResults: Map[String, List[String]] = {
    //    println(Json.prettyPrint(airPassengerDuty.getResults.head))
    Map(
      "LotteryDuty" -> lotteryDuty.getResults,
      "OldGamingDutyPaymentOnAccount" -> oldGamingDutyPaymentOnAccount.getResults,
      "GamingDuty" -> gamingDuty.getResults,
      "AirPassengerDuty" -> airPassengerDuty.getResults,
      "LandFill" -> landFill.getResults,
      "AggregatesLevy" -> aggregateLevy.getResults,
      "BingoDuty" -> bingoDuty.getResults,
      "InsurancePremiumTax" -> insurancePremiumTax.getResults,
      "GamingDutyPaymentOnAccount" -> gamingDutyPaymentOnAccount.getResults,
      "CorporateInterestAppointCompany" -> corporateInterestAppointCompany.getResults,
      "CorporateInterestRevokeCompany" -> corporateInterestRevokeCompany.getResults,
      "GasAsRoadFuel" -> gasAsRoadFuel.getResults,
      "Biofuels" -> biofuels.getResults,
      "BusinessUsers" -> businessUser.getResults,
      "Agents" -> agents.getResults,
      "Backend" -> backendVerification.getResults,
      "Frontend" -> frontendVerification.getResults
    )
  }
}
