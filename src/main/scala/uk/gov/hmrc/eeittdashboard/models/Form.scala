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

package uk.gov.hmrc.eeittdashboard.models

case class Form(value: String)
object LotteryDuty extends Form("promoters-monthly-lottery-duty-return")
object BingoDuty extends Form("bingo-duty-promoters-monthly-return")
object LandFill extends Form("landfill-tax-return")
object AggregateLevy extends Form("aggregates-levy-return")
object GamingDuty extends Form("gaming-duty-payment-on-account")
object AirPassengerDuty extends Form("air-passenger-duty-return")
object InsurancePremiumTax extends Form("insurance-premium-tax-return")
object GamingDutyPayment extends Form("gaming-duty-return")
