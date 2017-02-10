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

package curlrequests

import models.Env

class CurlByDatabase(environment: Env) {

  val dataCentres = Map(
    "Qa" -> "https://kibana-datacentred-sal01-qa.tax.service.gov.uk/elasticsearch/logstash-qa*/_search?pretty",
    "DataCentre" -> "https://kibana-datacentred-sal01-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty",
    "SkyScape" -> "https://kibana-skyscape-farnborough-production.tax.service.gov.uk/elasticsearch/logstash-production*/_search?pretty"
  )

  val agents = new Agents(dataCentres(environment.value))
  val businessUser = new BusinessUser(dataCentres(environment.value))
  val frontendVerification = new FrontEndVerification(dataCentres(environment.value))
  val backendVerification = new BackendVerification(dataCentres(environment.value))

  def getCurlResults: Map[String, List[String]] = {
    Map(
      "BusinessUsers" -> businessUser.getBusinessResults,
      "Agents" -> agents.getAgentResults,
      "Backend" -> backendVerification.getBackendResults,
      "Frontend" -> frontendVerification.getFrontendResults
    )
  }
}
