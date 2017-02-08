import java.security.PrivateKey

import models.{GoogleApp, JsonClass}
import play.api.libs.json.{JsObject, JsValue}
import uk.gov.hmrc.secure.AsymmetricDecrypter

/**
  * Created by harrison on 08/02/17.
  */
package object controllers {
  val key: String = loadApp.privateKey
  lazy val loadApp = services.Json.fromJson[GoogleApp](scala.io.Source.fromFile("src/main/resources/serviceAccount.json").mkString)
  val privateKey: PrivateKey = AsymmetricDecrypter.buildPrivateKey(key, "RSA")

  def parseJsonFromRequest(json: JsValue) = {
    val list = json \ "hits" \ "hits"

    list.as[List[JsObject]].map { x =>
      (x \ "_source" \ "log").validate[String].map {
        case y =>
          val some = play.api.libs.json.Json.parse(y).as[JsonClass]
          some.message
        case _ => ""
      }.get
    }
  }

  def checkFor500(json: JsValue): Boolean = {
    val hits = json \ "hits" \ "total"
    hits.get.as[Int] >= 500
  }
}
