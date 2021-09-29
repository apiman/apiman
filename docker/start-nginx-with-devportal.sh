#!/bin/sh

# Check if a prefix exists
if [ -n "$BASE_PATH" ]
then
  # If a prefix is set we will replace the base path in index.html before starting the application
  # Replace existing base tag with base tag containing system prefix src e.g. "/pas/devportal/"
  sed -i "s|<base.*>|<base href=\"$BASE_PATH\">|g" "/usr/share/nginx/html/index.html"
fi


# replace all occurrences of placeholders with environment variables
# see https://blog.codecentric.de/en/2019/03/docker-angular-dockerize-app-easily/

#for mainFileName in /usr/share/nginx/html/main*.js
#do
#  envsubst "\$KEYCLOAK_REALM \$API_MGMT_UI_REST_URL \$KEYCLOAK_AUTH_URL \$LOGO_FILE_URL \$ABOUT_LOGO_FILE_URL \$FIRST_LINK \$FIRST_LINK_LABEL \$SECOND_LINK \$SECOND_LINK_LABEL"  < "$mainFileName" > main.tmp
#  mv main.tmp "${mainFileName}"
#done

printf "Starting developer portal:\n"

# start nginx
nginx -g 'daemon off;'
