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

package uk.gov.hmrc.eeittdashboard.curlrequests

import play.api.Logger
import play.api.libs.json.{ JsObject, JsValue }

import scala.sys.process.Process

class FrontEndVerification(dataCentre: String) {

  def getResults: List[String] = {
    filterErrors(splitRequest(0, 24, is500, parseJsonFromRequest, queryResults))
  }

  def queryResults(start: Float, end: Float): JsValue = {
    Logger.debug(s"quering FrontendVerification")
    play.api.libs.json.Json.parse(call(start, end, queryJson, dataCentre).body)
  }

  def queryJson(start: Float, end: Float) = {
    s"""{
      |  "size": 500,
      |  "query": {
      |    "bool": {
      |      "must": [
      |        {
      |          "query_string": {
      |            "analyze_wildcard": true,
      |            "query": "app:\\"eeitt-frontend\\" AND verification"
      |          }
      |        },
      |        {
      |          "range": {
      |            "@timestamp": {
      |              "gte": ${millis(start)},
      |              "lte": ${millis(end)},
      |              "format": "epoch_millis"
      |            }
      |          }
      |        }
      |      ],
      |      "must_not": []
      |    }
      |  }
      |}""".stripMargin
    s"""{
       |  "version": true,
       |  "size": 500,
       |  "sort": [
       |    {
       |      "@timestamp": {
       |        "order": "desc",
       |        "unmapped_type": "boolean"
       |      }
       |    }
       |  ],
       |  "query": {
       |    "bool": {
       |      "must": [
       |        {
       |          "query_string": {
       |            "query": "app:\\"eeitt-frontend\\" AND verification AND NOT level:ERROR",
       |            "analyze_wildcard": true
       |          }
       |        },
       |        {
       |          "range": {
       |            "@timestamp": {
       |              "gte": "${millis(start)}",
       |        "lte": "${millis(end)}",
       |        "format": "epoch_millis"
       |        }
       |      }
       |      }
       |      ],
       |      "must_not": []
       |    }
       |  },
       |  "_source": {
       |    "excludes": []
       |  },
       |  "aggs": {
       |    "2": {
       |      "date_histogram": {
       |        "field": "@timestamp",
       |        "interval": "30m",
       |        "time_zone": "Europe/London",
       |        "min_doc_count": 1
       |      }
       |    }
       |  },
       |  "stored_fields": [
       |    "*"
       |  ],
       |  "script_fields": {},
       |  "docvalue_fields": [
       |    "@timestamp",
       |    "received_at",
       |    "ste",
       |    "time"
       |  ],
       |  "highlight": {
       |    "pre_tags": [
       |      "@kibana-highlighted-field@"
       |    ],
       |    "post_tags": [
       |      "@/kibana-highlighted-field@"
       |    ],
       |    "fields": {
       |      "*": {
       |        "highlight_query": {
       |          "bool": {
       |            "must": [
       |              {
       |                "query_string": {
       |                  "query": "app:\\"eeitt-frontend\\" AND verification AND NOT level:ERROR",
       |                  "analyze_wildcard": true,
       |                  "all_fields": true
       |                }
       |              },
       |              {
       |                "range": {
       |                  "@timestamp": {
       |                    "gte": "${millis(start)}",
       |              "lte": "${millis(end)}",
       |              "format": "epoch_millis"
       |              }
       |            }
       |            }
       |            ],
       |            "must_not": []
       |          }
       |        }
       |      }
       |    },
       |    "fragment_size": 2147483647
       |  }
       |}""".stripMargin
    """{
      |  "version": true,
      |  "size": 500,
      |  "sort": [
      |    {
      |      "@timestamp": {
      |        "order": "desc",
      |        "unmapped_type": "boolean"
      |      }
      |    }
      |  ],
      |  "query": {
      |    "bool": {
      |      "must": [
      |        {
      |          "query_string": {
      |            "query": "app:\"eeitt-frontend\" AND verification AND NOT level:ERROR",
      |            "analyze_wildcard": true
      |          }
      |        },
      |        {
      |          "range": {
      |            "@timestamp": {
      |              "gte": 1506898800000,
      |              "lte": 1506985199999,
      |              "format": "epoch_millis"
      |            }
      |          }
      |        }
      |      ],
      |      "must_not": []
      |    }
      |  },
      |  "_source": {
      |    "excludes": []
      |  },
      |  "aggs": {
      |    "2": {
      |      "date_histogram": {
      |        "field": "@timestamp",
      |        "interval": "30m",
      |        "time_zone": "Europe/London",
      |        "min_doc_count": 1
      |      }
      |    }
      |  },
      |  "stored_fields": [
      |    "*"
      |  ],
      |  "script_fields": {},
      |  "docvalue_fields": [
      |    "@timestamp",
      |    "received_at",
      |    "ste",
      |    "time"
      |  ],
      |  "highlight": {
      |    "pre_tags": [
      |      "@kibana-highlighted-field@"
      |    ],
      |    "post_tags": [
      |      "@/kibana-highlighted-field@"
      |    ],
      |    "fields": {
      |      "*": {
      |        "highlight_query": {
      |          "bool": {
      |            "must": [
      |              {
      |                "query_string": {
      |                  "query": "app:\"eeitt-frontend\" AND verification AND NOT level:ERROR",
      |                  "analyze_wildcard": true,
      |                  "all_fields": true
      |                }
      |              },
      |              {
      |                "range": {
      |                  "@timestamp": {
      |                    "gte": 1506898800000,
      |                    "lte": 1506985199999,
      |                    "format": "epoch_millis"
      |                  }
      |                }
      |              }
      |            ],
      |            "must_not": []
      |          }
      |        }
      |      }
      |    },
      |    "fragment_size": 2147483647
      |  }
      |}""".stripMargin
  }

}
