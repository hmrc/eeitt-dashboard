#!/usr/bin/env bash
DATE="$(date -d "yesterday" -Idate)"

STARTHOURS=$1

ENDHOURS=$2

DATE_MILLIS="$(date --date=${DATE} +%s)000"
DATE_START_MILLIS=$(($DATE_MILLIS+${STARTHOURS}*3600000-1))
DATE_END_MILLIS=$(($DATE_MILLIS+${ENDHOURS}*3600000))
curl -X POST -H "kbn-xsrf: kibana"  -d '{
"size":500,
"query":{
"bool":{
"must":[{
"query_string":{
"analyze_wildcard":true,"query": "app:eeitt AND NOT app:\"eeitt-frontend\" AND \"registration of agent\""
}
},
{
"range":{
"@timestamp":{
"gte":'${DATE_START_MILLIS}',
"lte":'${DATE_END_MILLIS}',"format":"epoch_millis"}}}],"must_not":[]}}
}' "https://kibana-datacentred-sal01-qa.tax.service.gov.uk/elasticsearch/logstash-qa*/_search?pretty"