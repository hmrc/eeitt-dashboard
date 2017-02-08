package controllers

import scala.sys.process.Process

/**
  * Created by harrison on 08/02/17.
  */
class BusinessUser(dataCentre: String) {

  def resultsBuissnessQuery(start: Int, end: Int): List[String] = {
    val resultAws = play.api.libs.json.Json.parse(Process(s"./LiveBusinessUser.sh $start $end ${dataCentre}") !!)
    //    val resultSkyscape = Json.parse(Process(s"./LiveBusinessUser.sh $start $end ${dataCenters("Skyscape")}") !!)
    parseJsonFromRequest(resultAws)
  }

  def test(a : Int, b: Int): Unit ={

  }
}
