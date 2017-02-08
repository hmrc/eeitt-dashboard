package controllers

import scala.sys.process.Process

/**
  * Created by harrison on 08/02/17.
  */
class Agents (dataCentre: String){
  def resultsAgentQuery(start: Int, end: Int): List[String] = {
    val resultAws = play.api.libs.json.Json.parse(Process(s"./LiveAgent.sh $start $end ${dataCentre}") !!)
    //    val resultSkyscape = Json.parse(Process(s"./LiveAgent.sh $start $end ${dataCenters("Skyscape")}") !!)
    parseJsonFromRequest(resultAws)
  }

  def test(a : Int, b: Int): Unit ={

  }
}
