#!/bin/sh
#curl --header "content-type: text/xml" -d @$1 http://localhost:1010/ws
#curl --header "content-type: text/xml" -d @$1 http://localhost:8031/ws
curl --header "content-type: text/xml" -d @$1 http://localhost:891/admin/ws
#curl --header "content-type: text/xml" -d @$1 http://test.api.glytoucan.org/admin/ws
