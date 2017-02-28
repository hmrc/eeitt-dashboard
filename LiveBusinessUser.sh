#!/usr/bin/env bash

DATE="$(date -d "-1 days" -Idate)"

STARTHOURS=$1

ENDHOURS=$2

DATACENTRE=$3

DATE_MILLIS="$(date --date=${DATE} +%s)000"
DATE_START_MILLIS=$(($DATE_MILLIS+${STARTHOURS}*3600000-1))
DATE_END_MILLIS=$(($DATE_MILLIS+${ENDHOURS}*3600000))

curl -X POST -u $VPNU:$VPNPASS -H "kbn-xsrf: kibana" -d '{
  "size": 500,
  "sort": [
    {
      "@timestamp": {
        "order": "desc",
        "unmapped_type": "boolean"
      }
    }
  ],
  "query": {
    "bool": {
      "must": [
        {
          "query_string": {
            "analyze_wildcard": true,
            "query": "app:eeitt AND NOT app:\"eeitt-frontend\" AND \"registration of business\""
          }
        },
        {
          "range": {
            "@timestamp": {
              "gte": 1488153600000,
              "lte": 1488239999999,
              "format": "epoch_millis"
            }
          }
        }
      ],
      "must_not": []
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
    "require_field_match": false,
    "fragment_size": 2147483647
  },
  "_source": {
    "excludes": []
  },
  "aggs": {
    "2": {
      "date_histogram": {
        "field": "@timestamp",
        "interval": "30m",
        "time_zone": "Europe/London",
        "min_doc_count": 1
      }
    }
  },
  "stored_fields": [
    "*"
  ],
  "script_fields": {},
  "docvalue_fields": [
    "@timestamp",
    "received_at",
    "kill_date_time",
    "time",
    "start_time",
    "end_time"
  ]
}' "${DATACENTRE}"