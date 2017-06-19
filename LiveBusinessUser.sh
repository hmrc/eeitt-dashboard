#!/usr/bin/env bash

DATE="$(date -d "-$4 days" -Idate)"

STARTHOURS=$1

ENDHOURS=$2

DATACENTRE=$3

YESTERDAY=$(expr $4 - 1)

if [ $YESTERDAY == 0 ]
then
DATE_MILLIS="$(date --date=${DATE} +%s)000"
else
DATE_MILLIS="$(date -d "-$YESTERDAY days" --date=${DATE} +%s)000"
fi

DATE_START_MILLIS=$(($DATE_MILLIS+${STARTHOURS}*3600000-1))
DATE_END_MILLIS=$(($DATE_MILLIS+${ENDHOURS}*3600000))

#curl -s -S -X POST -u $VPNU:$VPNPASS -H "kbn-xsrf: kibana" -d '{"size":500,"query":{"bool":{"must":[{"query_string":{"analyze_wildcard":true,"query": "app:eeitt AND NOT app:\"eeitt-frontend\" AND \"registration of business\""}},{"range":{"@timestamp":{"gte":'${DATE_START_MILLIS}',"lte":'${DATE_END_MILLIS}',"format":"epoch_millis"}}}],"must_not":[]}}}' "${DATACENTRE}"

curl -s -S -X POST -H "kbn-xsrf: kibana" -d '{"size":500,"query":{"bool":{"must":[{"query_string":{"analyze_wildcard":true,"query": "app:eeitt AND NOT app:\"eeitt-frontend\" AND \"registration of business\""}},{"range":{"@timestamp":{"gte":'${DATE_START_MILLIS}',"lte":'${DATE_END_MILLIS}',"format":"epoch_millis"}}}],"must_not":[]}}}' "${DATACENTRE}"