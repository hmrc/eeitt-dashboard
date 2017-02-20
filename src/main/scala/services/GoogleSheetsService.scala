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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import models.GoogleApp

import scala.collection.JavaConverters._
import scala.language.implicitConversions
import scalaz.Scalaz._

class GoogleSheetsService {
  lazy val loadApp = Json.fromJson[GoogleApp](scala.io.Source.fromFile("src/main/resources/serviceAccount.json").mkString)
  def gDataApiForToken(accessToken: String, privatekey : PrivateKey):Sheets = {

    val httpTransport = new NetHttpTransport
    val jsonFactory = new JacksonFactory

    val credential = new GoogleCredential.Builder()
      .setJsonFactory(jsonFactory)
      .setTransport(httpTransport)
      .setServiceAccountId(loadApp.clientEmail)
      .setServiceAccountPrivateKey(privatekey)
        .setServiceAccountUser(loadApp.userImpersonation)
      .setServiceAccountScopes(Seq("https://spreadsheets.google.com/feeds/spreadsheets/", "https://www.googleapis.com/auth/spreadsheets").asJava)
      .build()
    credential.setAccessToken(accessToken)

    val service = new Sheets.Builder(httpTransport,jsonFactory, credential)
      .setApplicationName("test")
      .build()

    service
  }

  def print(data : Map[String, List[String]]) = {

    val uniqueUsers = parseVerificationJsonData(data("Backend"))

    val info = parseJsonData(data("BusinessUsers"))
    val totalBuissnessUsers : Int = info.values.sum
    val numOfAgents : Int = data("Agents").size
    val date :LocalDate = LocalDate.now.minus(Period.ofDays(1))
    println("DATE: - "+stringToAnyRef(date.toString))
    println("BUSINESSUSERS: - "+intToAnyRef(totalBuissnessUsers))
    println("AGENTS: - "+intToAnyRef(numOfAgents))
    println("AL: - "+intToAnyRef(info("'AL'")))
    println("AP: - "+intToAnyRef(info("'AP'")))
    println("BD: - "+intToAnyRef(info("'BD'")))
    println("GD: - "+intToAnyRef(info("'GD'")))
    println("IP: - "+intToAnyRef(info("'IP'")))
    println("LD: - "+intToAnyRef(info("'LD'")))
    println("LF: - "+intToAnyRef(info("'LF'")))
    println("FRONTEND: - "+intToAnyRef(data("Frontend").size))
    println("BACKEND: - "+intToAnyRef(data("Backend").size))
    println("UNIQUEUSERS: - "+intToAnyRef(uniqueUsers))
  }

  def populateWorksheetByFileId(accessToken: String, fileId: String, privateKey: PrivateKey, data : Map[String, List[String]]) = {

    val service = gDataApiForToken(accessToken, privateKey)

    val uniqueUsers = parseVerificationJsonData(data("Backend"))

    val info = parseJsonData(data("BusinessUsers"))
    val totalBuissnessUsers : Int = info.values.sum
    val numOfAgents : Int = data("Agents").size
    val date :LocalDate = LocalDate.now.minus(Period.ofDays(1))

    val values : java.util.List[java.util.List[AnyRef]] = Seq(
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
        intToAnyRef(uniqueUsers)
      ).asJava
    ).asJava
    val valuerange = new ValueRange
    valuerange.setRange("A1:E1")
    valuerange.setValues(values)
    val spreadsheet = service.spreadsheets().values().append(fileId,"A1:E1", valuerange).setValueInputOption("RAW").execute()//metafeedUrl, classOf[SpreadsheetEntry])

    spreadsheet
  }

  private def intToAnyRef(int: Int): AnyRef = {
    java.lang.Integer.valueOf(int)
  }

  private def stringToAnyRef(string: String): AnyRef = {
    string
  }

  private def parseJsonData(data: List[String]) : Map[String, Int]= {
    val groupedData = data.groupBy(o => o.split(" ")(8))
    val map = Map("'AL'" -> 0, "'AP'" -> 0, "'BD'" -> 0, "'GD'" -> 0, "'IP'" -> 0, "'LD'" -> 0, "'LF'" -> 0)
    val numData : Map[String, Int] = groupedData.map(o => o._1 -> o._2.size)

    numData |+| map
  }

  private def parseVerificationJsonData(data:List[String]) : Int = {
    val groupedData = data.groupBy(o => o.split("groupId")(1).split(",")(0))
    groupedData.size
  }
}
