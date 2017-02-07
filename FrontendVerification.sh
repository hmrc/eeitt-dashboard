#!/usr/bin/env bash

DATE="$(date -d "yesterday" -Idate)"

STARTHOURS=$1

ENDHOURS=$2

DATACENTRE=$3

DATE_MILLIS="$(date --date=${DATE} +%s)000"
DATE_START_MILLIS=$(($DATE_MILLIS+${STARTHOURS}*3600000-1))
DATE_END_MILLIS=$(($DATE_MILLIS+${ENDHOURS}*3600000))

curl -X POST -u $VPNU:$VPNPASS -H "kbn-xsrf: kibana"  -d '{
"size":500,
"query":{
"bool":{
"must":[{
"query_string":{
"analyze_wildcard":true,"query":"app:\"eeitt-frontend\" AND verification"
}
},
{
"range":{
"@timestamp":{
"gte":'${DATE_START_MILLIS}',
"lte":'${DATE_END_MILLIS}',"format":"epoch_millis"}}}],"must_not":[]}}
}' "${DATACENTRE}"