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

package uk.gov.hmrc.eeittdashboard

import java.io.FileNotFoundException

import com.fasterxml.jackson.core.JsonParseException
import models.GoogleApp
import uk.gov.hmrc.eeittdashboard.services.Json
import uk.gov.hmrc.play.test.UnitSpec


class GoogleSheetsServiceSpec extends UnitSpec {
  "Parsing Json data in for the Auth token" should {
    "Succeed with valid Json" in {
      val validJson =
        """{
        "privateKey": "bob",
        "clientEmail": "test-test@test.iam.gserviceaccount.com",
        "fileId": "0121test",
        "userImpersonation": "test@test.co.uk"}"""
      noException should be thrownBy Json.fromJson[GoogleApp](validJson)
    }

    "Fail with invalid Json" in {
      val invalidJson =
        """{
        : "bob",
        "client_email": "test-test@test.iam.gserviceaccount.com",
        "file_id": "0121test",
        "user_impersonation": "test@test.co.uk"}"""
      a[JsonParseException] shouldBe thrownBy(Json.fromJson[GoogleApp](invalidJson))
    }
  }

  "Reading in the file with the Json data in it" should {
    "Succeed with a file exists if file is found" in {
     noException should be thrownBy scala.io.Source.fromFile("src/test/resources/serviceAccountTest.json")
    }
    "Fail with a no file found exception" in {
      a [FileNotFoundException] should be thrownBy scala.io.Source.fromFile("src/test/resoces/serviceAccountTest.json")
    }
  }
}
