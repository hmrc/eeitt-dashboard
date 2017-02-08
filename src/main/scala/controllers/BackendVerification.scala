package controllers

import scala.sys.process.Process

/**
  * Created by harrison on 08/02/17.
  */
class BackendVerification(dataCentre: String){
    def resultsBackEndVerificationAws(start: Int, end: Int): List[String] = {
      val result = List(jsonResultBackendAws(start, end)) // 24 Hours
      if (checkFor500(result(0))) {
        val half = List(jsonResultBackendAws(start, end / 2), jsonResultBackendAws((end / 2), end))
        if (checkFor500(half(0)) || checkFor500(half(1))) {
          val half2 = List(jsonResultBackendAws(start, end / 3), jsonResultBackendAws(end / 3, (end / 1.5).toInt), jsonResultBackendAws((end / 1.5).toInt, end))
          if (checkFor500(half2(0)) || checkFor500(half2(1)) || checkFor500(half2(2))) {
            List("")
          } else {
            parseJsonFromRequest(half2(0)).++(parseJsonFromRequest(half2(1))).++(parseJsonFromRequest(half2(2)))
          }
        } else {
          parseJsonFromRequest(half(0)).++(parseJsonFromRequest(half(1)))
        }
      } else {
        val results = parseJsonFromRequest(result(0))
        results
      }
    }
    private def jsonResultBackendAws(start: Int, end: Int) = {
      play.api.libs.json.Json.parse(Process(s"./BackendVerification.sh $start $end ${dataCentre}") !!)
    }

  def test(a : Int, b: Int): Unit ={

  }
}
