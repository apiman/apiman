#!/bin/sh

# replace all occurences of placeholders with environment variables
# see https://blog.codecentric.de/en/2019/03/docker-angular-dockerize-app-easily/

for mainFileName in /usr/share/nginx/html/main*.js
do
  envsubst "\$APIMAN_UI_REST_URL \$KEYCLOAK_AUTH_URL"  < "$mainFileName" > main.tmp
  mv main.tmp "${mainFileName}"
done
nginx -g 'daemon off;'
