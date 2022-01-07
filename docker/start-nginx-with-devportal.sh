#!/bin/sh

#
# Copyright 2022 Scheer PAS Schweiz AG
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  imitations under the License.
#


# check if a variable exists
if [ -n "$BASE_PATH" ]
then
  # replace existing base tag with base tag containing system prefix src e.g. "/pas/devportal/"
  sed -i "s|<base.*>|<base href=\"$BASE_PATH\">|g" "/usr/share/nginx/html/index.html"
fi


# replace all occurrences of placeholders with environment variables
envsubst < /usr/share/devportal/assets/config.json5 > /tmp/config.json5.tmp
cp /tmp/config.json5.tmp /usr/share/nginx/html/assets/config.json5


# start nginx
printf "Starting developer portal:\n"
nginx -g 'daemon off;'
