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

package uk.gov.hmrc.eeittdashboard.models

sealed trait Form { val value:String }

case class iForm(value: String )extends Form
object LotteryDuty extends iForm("promoters-monthly-lottery-duty-return")
object BingoDuty extends iForm("bingo-duty-promoters-monthly-return")
object LandFill extends iForm("landfill-tax-return")
object AggregateLevy extends iForm("aggregates-levy-return")
object OldGamingDutyPaymentOnAccount extends iForm("gaming-duty-payment-on-account")
object AirPassengerDuty extends iForm("air-passenger-duty-return")
object InsurancePremiumTax extends iForm("insurance-premium-tax-return")
object GamingDuty extends iForm("gaming-duty-return")

case class gForm(value: String ) extends Form

object GamingDutyPaymentOnAccount extends gForm("gd94-gaming-duty-payment-on-account")
object CorporateInterestAppointCompany extends gForm("corporate-interest-appoint-company")
object CorporateInterestRevokeCompany extends gForm("corporate-interest-revoke-company")
object GasAsRoadFuel extends gForm("CE930-gas-as-road-fuel")
object Biofuels extends gForm("HO930-biofuels")


