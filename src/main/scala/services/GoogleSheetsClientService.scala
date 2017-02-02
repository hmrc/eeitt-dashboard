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

import java.net.URL
import java.time.LocalDate

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.{Spreadsheet, ValueRange}
import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet.{CellEntry, CellFeed, SpreadsheetEntry, WorksheetEntry}
import models.GoogleApp

import scala.collection.JavaConverters._

// For altering sheets we need to use the gData services

class GoogleSheetsClientService {

//  lazy val app = Json.fromJson[GoogleApp](this.getClass.getClassLoader.getResource("google_secrets.json"))

  private val baseUrl = Sheets.DEFAULT_BASE_URL
  private val sheetsFeedBase = "https://spreadsheets.google.com/feeds/spreadsheets/"

  private val maybe = "https://sheets.googleapis.com/"

  import scala.collection.JavaConversions._

  def gDataApiForToken(accessToken: String):Sheets = {

    val httpTransport = new NetHttpTransport
    val jsonFactory = new JacksonFactory

    val credential = new GoogleCredential.Builder()
      .setJsonFactory(jsonFactory)
      .setTransport(httpTransport)
      .setClientSecrets("1070447028534-ncaksv124efuphqnn1iorvd15er7h32f.apps.googleusercontent.com", "o5fK6stoVWXx7TZvOjUf2X3Z")
      .build()
    credential.setAccessToken(accessToken)

    val service = new Sheets.Builder(httpTransport, jsonFactory, credential)
      .setApplicationName("test")
      .build()
//    service.setHeader("Authorization", "Bearer " + accessToken)
//
//    service.setOAuth2Credentials(credential)

    service
  }

  def getWorksheetByName(creds:String, fileId: String, data : List[String])  = {

    val service = gDataApiForToken(creds) //see above

    val metafeedUrl = new URL(maybe+fileId)

    val anyref : AnyRef = "bob"

    val info = parseJsonData(data)
    //outer list means the data while inner lists mean rows, the values inside the list are the columns
    val somelist : java.util.List[java.util.List[AnyRef]] = Seq(
      Seq(
        toAnyRef(LocalDate.now.toString),
        toAnyRef(info("'AL'").toString),
        toAnyRef(info("'BD'").toString),
        toAnyRef(info("'GD'").toString),
        toAnyRef(info("'LD'").toString),
        toAnyRef(info("'LF'").toString),
        toAnyRef(info("'IP'").toString),
        toAnyRef(info("'AP'").toString),
        "Frontend",
        "BackEnd",
        "TotalUnique"
      ).asJava).asJava
    val valuerange = new ValueRange
    valuerange.setRange("A1:E1")
    valuerange.setValues(somelist)
    val spreadsheet = service.spreadsheets().values().append(fileId,"A1:E1", valuerange).setValueInputOption("RAW").execute()//metafeedUrl, classOf[SpreadsheetEntry])

    spreadsheet
//    spreadsheet.getWorksheets.find(_.getTitle.getPlainText.equals(worksheetName)).head
  }

  def toAnyRef(string: String): AnyRef = {
    string
  }

  def parseJsonData(data: List[String]) : Map[String, Int]= {
    val groupedData = data.groupBy(o => o.split(" ")(8))
    val numData : Map[String, Int] = groupedData.map(o => o._1 -> o._2.size)
    println(numData)
    numData
  }

//  def testWrite(creds: String, worksheetEntry: WorksheetEntry, whatToWrite: String) = {
//
//    val service = gDataApiForToken(creds) //see above
//
//    val cellFeedUrl = worksheetEntry.getCellFeedUrl()
//    val cellFeed = service.getFeed(cellFeedUrl, classOf[CellFeed])
//
//    val cellEntry = new CellEntry(1, 1, whatToWrite)
//    cellFeed.insert(cellEntry)
//  }

}
