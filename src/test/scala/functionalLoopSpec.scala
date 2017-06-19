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

import uk.gov.hmrc.play.test.UnitSpec

import scala.annotation.tailrec


class FunctionalLoopSpec extends UnitSpec {
   def get2(start: Int, end: Int, numElements: (Int, Int) => Int, elements: (Int, Int) => List[String], threshold: Int): List[String] = {

     val res = numElements(start, end)
    if (res <= threshold) {
      elements(start, end)

    } else{
      val middle = ((end - start) / 2 + (end - start) % 2)+start

      get2(start, middle, numElements, testFct, threshold) ::: get2(middle, end, numElements, testFct, threshold)

    }
  }

  def testFct(start: Int, end: Int) = if (end - start <= 2) List.fill(9)("") else List.fill(501)("")

  def testFctHalf(start:Int, end: Int) = List.fill(410)("")

  def nums(start: Int, end: Int) = testFct(start, end).size

  def numsHalf(start:Int, end:Int) = testFctHalf(start, end).size

  "this" should {
    "return 144" in {
      get2(0, 24, nums, testFct, 500).size shouldBe 144
    }
  }
}
