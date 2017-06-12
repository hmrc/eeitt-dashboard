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

package uk.gov.hmrc.eeittdashboard.services

import java.time.{LocalDate, Period}

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import play.api.libs.json.JsObject

import scala.collection.JavaConverters._
import scala.language.implicitConversions
import scalaz.Scalaz._

class GoogleSheetsService {
//  lazy val loadApp = Json.fromJson[GoogleApp](scala.io.Source.fromFile("src/uk.gov.hmrc.eeittdashboard.main/resources/serviceAccount.json").mkString)

  def gDataApiForToken(credential: Credential): Sheets = {

    val httpTransport = new NetHttpTransport
    val jsonFactory = new JacksonFactory

    val service = new Sheets.Builder(httpTransport, jsonFactory, credential)
      .setApplicationName("test")
      .build()

    service
  }

  def print(data: Map[String, List[String]], num: Map[String, List[JsObject]]) = {

    val uniqueUsers = parseVerificationJsonData(data("Backend"))

    val info = parseJsonData(data("BusinessUsers"))
    val totalBuissnessUsers: Int = info.values.sum
    val numOfAgents: Int = data("Agents").size
    val date: LocalDate = LocalDate.now.minus(Period.ofDays(2))
    println("DATE: - " + stringToAnyRef(date.toString))
    println("BUSINESSUSERS: - " + intToAnyRef(totalBuissnessUsers))
    println("AGENTS: - " + intToAnyRef(numOfAgents))
    println("AL: - " + intToAnyRef(info("'AL'")) + "Succeded : - " + num("AggregatesLevy").size)
    println("AP: - " + intToAnyRef(info("'AP'")) + "Succeded : - " + num("AirPassengerDuty").size)
    println("BD: - " + intToAnyRef(info("'BD'")) + "Succeded : - " + num("BingoDuty").size)
    println("GD: - " + intToAnyRef(info("'GD'")) + "Succeded : - " + num("GamingDutyPayment").size + "Gaming Duty Returns" + num("GamingDuty").size )
    println("IP: - " + intToAnyRef(info("'IP'")) + "Succeded : - " + num("InsurancePremiumTax").size)
    println("LD: - " + intToAnyRef(info("'LD'")) + "Succeded : - " + num("LotteryDuty").size)
    println("LF: - " + intToAnyRef(info("'LF'")) + "Succeded : - " + num("LandFill").size)
    println("FRONTEND: - " + intToAnyRef(data("Frontend").size))
    println("BACKEND: - " + intToAnyRef(data("Backend").size))
    println("UNIQUEUSERS: - " + intToAnyRef(uniqueUsers))
  }

  def populateWorksheetByFileId(accessToken: Credential, fileId: String, data: Map[String, List[String]], num: Map[String, List[JsObject]]) = {

    val service = gDataApiForToken(accessToken)

    val uniqueUsers = parseVerificationJsonData(data("Backend"))

    val info = parseJsonData(data("BusinessUsers"))
    val totalBuissnessUsers: Int = info.values.sum
    val numOfAgents: Int = data("Agents").size
    val date: LocalDate = LocalDate.now.minus(Period.ofDays(1))

    val values: java.util.List[java.util.List[AnyRef]] = Seq(
      Seq(
        stringToAnyRef(date.toString),
        intToAnyRef(totalBuissnessUsers),
        intToAnyRef(numOfAgents),
        intToAnyRef(info("'AL'")),
        intToAnyRef(info("'AP'")),
        intToAnyRef(info("'BD'")),
        intToAnyRef(info("'GD'")),
        intToAnyRef(info("'IP'")),
        intToAnyRef(info("'LD'")),
        intToAnyRef(info("'LF'")),
        intToAnyRef(data("Frontend").size),
        intToAnyRef(data("Backend").size),
        intToAnyRef(uniqueUsers),
        intToAnyRef(num("AggregatesLevy").size),
        intToAnyRef(num("AirPassengerDuty").size),
        intToAnyRef(num("BingoDuty").size),
        intToAnyRef(num("GamingDutyPayment").size),
        intToAnyRef(num("InsurancePremiumTax").size),
        intToAnyRef(num("LotteryDuty").size),
        intToAnyRef(num("LandFill").size),
        intToAnyRef(num("GamingDuty").size)
      ).asJava
    ).asJava
    val valuerange = new ValueRange
    valuerange.setRange("A1:E1")
    valuerange.setValues(values)
    val spreadsheet = service.spreadsheets().values().append("1aGEhkcU4iekb_KQ0zc5AD6opY_Mo2KxY8a1d03DbdDQ", "A1:E1", valuerange).setValueInputOption("RAW").execute() //metafeedUrl, classOf[SpreadsheetEntry])

    spreadsheet
  }

  private def intToAnyRef(int: Int): AnyRef = {
    java.lang.Integer.valueOf(int)
  }

  private def stringToAnyRef(string: String): AnyRef = {
    string
  }

  private def parseJsonData(data: List[String]): Map[String, Int] = {
    val groupedData = data.groupBy(o => o.split(" ")(8))
    val map = Map("'AL'" -> 0, "'AP'" -> 0, "'BD'" -> 0, "'GD'" -> 0, "'IP'" -> 0, "'LD'" -> 0, "'LF'" -> 0)
    val numData: Map[String, Int] = groupedData.map(o => o._1 -> o._2.size)

    numData |+| map
  }

  private def parseVerificationJsonData(data: List[String]): Int = {
    val groupedData = data.groupBy(o => o.split("groupId")(1).split(",")(0))
    groupedData.size
  }
}
