#!/usr/bin/env bash

DATE="$(date -d "-$5 days" -Idate)"

STARTHOURS=$1

ENDHOURS=$2

FORM=$3

DATACENTRE=$4

YESTERDAY=$(expr $5 - 1)

if [ $YESTERDAY == 0 ]
then
DATE_MILLIS="$(date --date=${DATE} +%s)000"
else
DATE_MILLIS="$(date -d "-$YESTERDAY days" --date=${DATE} +%s)000"
fi

DATE_START_MILLIS=$(($DATE_MILLIS+${STARTHOURS}*3600000-1))
DATE_END_MILLIS=$(($DATE_MILLIS+${ENDHOURS}*3600000))

curl -s -S -X POST -u $VPNU:$VPNPASS -H "kbn-xsrf: kibana"  -d '{
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
            "query": "type:\"nginx access_json\" AND request:forms AND request:submission AND request:receipt AND request:\"'${FORM}'\"",
            "analyze_wildcard": true
          }
        },
        {
          "range": {
            "@timestamp": {
              "gte": '${DATE_START_MILLIS}',
              "lte": '${DATE_END_MILLIS}',
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
    "start_time",
    "end_time",
    "time",
    "streime"
  ]
}' "${DATACENTRE}"