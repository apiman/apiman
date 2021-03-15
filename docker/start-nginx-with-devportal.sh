#!/bin/sh

# Check if a prefix exists
if [ -n "$SYSTEM_PREFIX" ]
then
  # If a prefix is set we will replace the base path in index.html before starting the application
  # Replace existing base tag with base tag containing system prefix src e.g. "/pas/devportal/"
  sed -i "s|<base.*>|<base href=\"/$SYSTEM_PREFIX/devportal/\">|g" "/usr/share/nginx/html/index.html"
fi


# Check if custom css is needed
if [ -n "$CSS_FILE_URL" ]
then
  printf "Using custom css url: %s\n" "$CSS_FILE_URL"
  sed -i "s|<!--CustomCSS-->|<!--CSS-->\n  <link rel=\"stylesheet\" href=\"$CSS_FILE_URL\">|g" "/usr/share/nginx/html/index.html"
fi


# Check if custom favicon is needed
if [ -n "$FAVICON_FILE_URL" ]
then
  printf "Using custom favicon url: %s\n" "$FAVICON_FILE_URL"
fi
sed -i "s|FAVICON_FILE_URL|${FAVICON_FILE_URL:-favicon.ico}|g" "/usr/share/nginx/html/index.html"


# Set the default logo
if [ -n "$LOGO_FILE_URL" ]
then
  printf "Using custom img url: %s\n" "$LOGO_FILE_URL"
else
  export LOGO_FILE_URL="assets/logo-header.png"
fi


# replace all occurences of placeholders with environment variables
# see https://blog.codecentric.de/en/2019/03/docker-angular-dockerize-app-easily/

for mainFileName in /usr/share/nginx/html/main*.js
do
  envsubst "\$KEYCLOAK_REALM \$API_MGMT_UI_REST_URL \$KEYCLOAK_AUTH_URL \$LOGO_FILE_URL"  < "$mainFileName" > main.tmp
  mv main.tmp "${mainFileName}"
done

printf "Starting developer portal:\n"

# start nginx
nginx -g 'daemon off;'
