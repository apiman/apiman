#!/bin/sh

# replace all occurences of placeholders with environment variables
# see https://blog.codecentric.de/en/2019/03/docker-angular-dockerize-app-easily/

for mainFileName in /usr/share/nginx/html/main*.js
do
  envsubst "\$KEYCLOAK_REALM \$API_MGMT_UI_REST_URL \$KEYCLOAK_AUTH_URL"  < "$mainFileName" > main.tmp
  mv main.tmp "${mainFileName}"
done

# Check if a prefix exists
if [ -n "$SYSTEM_PREFIX" ]
then
  # If a prefix is set we will replace the base path in index.html before starting the application
  # Replace existing base tag with base tag containing system prefix src e.g. "/pas/devportal/"
  sed -i "s/<base.*>/<base href=\"\/$SYSTEM_PREFIX\/devportal\/\">/g" "/usr/share/nginx/html/index.html"
fi

echo "Starting devportal"

# start nginx
nginx -g 'daemon off;'
