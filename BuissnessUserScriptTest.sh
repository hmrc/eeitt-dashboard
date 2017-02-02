#!/usr/bin/env bash

DATE="$(date -d "yesterday" -Idate)"

STARTHOURS=$1

ENDHOURS=$2

DATE_MILLIS="$(date --date=${DATE} +%s)000"
DATE_START_MILLIS=$(($DATE_MILLIS+${STARTHOURS}*3600000-1))
DATE_END_MILLIS=$(($DATE_MILLIS+${ENDHOURS}*3600000))

curl -X GET -H "kbn-xsrf-token: kibana" -H "Cache-Control: no-cache" -H "Postman-Token: acfcc66f-769b-26c5-669e-68363cad3179" -d '{
  "query": {
    "filtered": {
      "query": {
        "query_string": {
          "query": "app:\"eeitt\" AND NOT app:\"eeitt-frontend\" AND \"registration of business\"",
          "analyze_wildcard": true
        }
      },
      "filter": {
        "bool": {
          "must": [
            {
              "range": {
                "@timestamp": {
                  "gte": '${DATE_START_MILLIS}',
                  "lte": '${DATE_END_MILLIS}'
                }
              }
            }
          ],
          "must_not": []
        }
      }
    }
  },
  "highlight": {
    "pre_tags": [
      "@kibana-highlighted-field@"
    ],
    "post_tags": [
      "@/kibana-highlighted-field@"
    ],
    "fields": {
      "*": {}
    },
    "fragment_size": 2147483647
  },
  "size": 500,
  "sort": [
    {
      "@timestamp": {
        "order": "desc",
        "unmapped_type": "boolean"
      }
    }
  ],
  "aggs": {
    "2": {
      "date_histogram": {
        "field": "@timestamp",
        "interval": "30m",
        "pre_zone": "+00:00",
        "pre_zone_adjust_large_interval": true,
        "min_doc_count": 0,
        "extended_bounds": {
          "min": 1485708712504,
          "max": 1485795112504
        }
      }
    }
  },
  "fields": [
    "*",
    "_source"
  ],
  "script_fields": {},
  "fielddata_fields": [
    "@timestamp",
    "received_at",
    "start_time",
    "end_time",
    "time",
    "kill_date_time"
  ]
}' "https://kibana-datacentred-sal01-qa.tax.service.gov.uk/elasticsearch/logstash-qa*/_search?pretty"

