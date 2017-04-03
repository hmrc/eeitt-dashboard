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

package services

import java.security.PrivateKey
import java.time.{LocalDate, Period}

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import models.GoogleApp
import play.api.libs.json.JsObject

import scala.collection.JavaConverters._
import scala.language.implicitConversions
import scalaz.Scalaz._

class GoogleSheetsService {
//  lazy val loadApp = Json.fromJson[GoogleApp](scala.io.Source.fromFile("src/main/resources/serviceAccount.json").mkString)

  def gDataApiForToken(credential: Credential): Sheets = {

    val httpTransport = new NetHttpTransport
    val jsonFactory = new JacksonFactory

    val service = new Sheets.Builder(httpTransport, jsonFactory, credential)
      .setApplicationName("test")
      .build()

    service
  }

  def print(data: Map[String, List[String]]) : Unit = {

    val uniqueUsers = parseVerificationJsonData(data("Backend"))

    val info = parseJsonData(data("BusinessUsers"))
    val totalBuissnessUsers: Int = info.values.sum
    val dataOfAgents: Int = data("Agents").size
    val date: LocalDate = LocalDate.now.minus(Period.ofDays(2))
    println("DATE: - " + toAnyRef(date.toString))
    println("BUSINESSUSERS: - " + toAnyRef(totalBuissnessUsers))
    println("AGENTS: - " + toAnyRef(dataOfAgents))
    println("AL: - " + toAnyRef(info("'AL'")) + "Succeded : - " + data("AggregatesLevy").size)
    println("AP: - " + toAnyRef(info("'AP'")) + "Succeded : - " + data("AirPassengerDuty").size)
    println("BD: - " + toAnyRef(info("'BD'")) + "Succeded : - " + data("BingoDuty").size)
    println("GD: - " + toAnyRef(info("'GD'")) + "Succeded : - " + data("GamingDutyPayment").size + "Gaming Duty Returns" + data("GamingDuty").size )
    println("IP: - " + toAnyRef(info("'IP'")) + "Succeded : - " + data("InsurancePremiumTax").size)
    println("LD: - " + toAnyRef(info("'LD'")) + "Succeded : - " + data("LotteryDuty").size)
    println("LF: - " + toAnyRef(info("'LF'")) + "Succeded : - " + data("LandFill").size)
    println("FRONTEND: - " + toAnyRef(data("Frontend").size))
    println("BACKEND: - " + toAnyRef(data("Backend").size))
    println("UNIQUEUSERS: - " + toAnyRef(uniqueUsers))
  }

  def populateWorksheetByFileId(accessToken: Credential, fileId: String, data: Map[String, List[String]]) = {

    val service = gDataApiForToken(accessToken)

    val uniqueUsers = parseVerificationJsonData(data("Backend"))

    val info = parseJsonData(data("BusinessUsers"))
    val totalBuissnessUsers: Int = info.values.sum
    val dataOfAgents: Int = data("Agents").size
    val date: LocalDate = LocalDate.now.minus(Period.ofDays(1))

    val values: java.util.List[java.util.List[AnyRef]] = Seq(
      Seq(
        toAnyRef(date.toString),
        toAnyRef(totalBuissnessUsers),
        toAnyRef(dataOfAgents),
        toAnyRef(info("'AL'")),
        toAnyRef(info("'AP'")),
        toAnyRef(info("'BD'")),
        toAnyRef(info("'GD'")),
        toAnyRef(info("'IP'")),
        toAnyRef(info("'LD'")),
        toAnyRef(info("'LF'")),
        toAnyRef(data("Frontend").size),
        toAnyRef(data("Backend").size),
        toAnyRef(uniqueUsers),
        toAnyRef(data("AggregatesLevy").size),
        toAnyRef(data("AirPassengerDuty").size),
        toAnyRef(data("BingoDuty").size),
        toAnyRef(data("GamingDutyPayment").size),
        toAnyRef(data("InsurancePremiumTax").size),
        toAnyRef(data("LotteryDuty").size),
        toAnyRef(data("LandFill").size),
        toAnyRef(data("GamingDuty").size)
      ).asJava
    ).asJava
    val valuerange = new ValueRange
    valuerange.setRange("A1:E1")
    valuerange.setValues(values)
    service
      .spreadsheets()
      .values()
      .append("1aGEhkcU4iekb_KQ0zc5AD6opY_Mo2KxY8a1d03DbdDQ", "A1:E1", valuerange)
      .setValueInputOption("RAW")
      .execute()
  }

  private def toAnyRef[A](value: A): AnyRef = {
    value.asInstanceOf[AnyRef]
  }

  private def parseJsonData(data: List[String]): Map[String, Int] = {
    val groupedData = data.groupBy(o => o.split(" ")(8))
    val map = Map("'AL'" -> 0, "'AP'" -> 0, "'BD'" -> 0, "'GD'" -> 0, "'IP'" -> 0, "'LD'" -> 0, "'LF'" -> 0)
    val dataData: Map[String, Int] = groupedData.map(o => o._1 -> o._2.size)

    dataData |+| map
  }

  private def parseVerificationJsonData(data: List[String]): Int = {
    data.groupBy(o => o.split("groupId")(1).split(",")(0)).size
  }
}
