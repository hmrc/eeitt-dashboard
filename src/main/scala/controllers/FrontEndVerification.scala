package controllers

import scala.sys.process.Process

/**
  * Created by harrison on 08/02/17.
  */
class FrontEndVerification(dataCentre: String) {
    def resultsFrontendVerification(start: Int, end: Int): List[String] = {
      val result = List(jsonResultFrontend(start, end))
      if (checkFor500(result.head)) {
        val half = List(jsonResultFrontend(start, end / 2), jsonResultFrontend(end / 2, end))
        if (checkFor500(half.head) || checkFor500(half.last)) {
          List("")
        } else {
          parseJsonFromRequest(half.head).++(parseJsonFromRequest(half.last))
        }
      } else {
        parseJsonFromRequest(result.head)
      }
    }

    private def jsonResultFrontend(start: Int, end: Int) = {
      play.api.libs.json.Json.parse(Process(s"./FrontendVerification.sh $start $end ${dataCentre}") !!)
    }

  def test(a : Int, b: Int): Unit ={

  }
}
