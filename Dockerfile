FROM node:10.16.2-alpine as node

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm install
#copy the rest of the project into the image
COPY . .

RUN npm run build-production

# Stage 2
FROM nginx:1.13.12-alpine

COPY --from=node /usr/src/app/dist/apiman-dev-portal /usr/share/nginx/html

#copy ssl keys
COPY docker/tls.crt /etc/ssl/certs/tls.crt
COPY docker/tls.key /etc/ssl/private/tls.key

#copy nginx configuration
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

#replace all occurences of placeholders with environment variables
#(see https://blog.codecentric.de/en/2019/03/docker-angular-dockerize-app-easily/)

RUN echo "for mainFileName in /usr/share/nginx/html/main*.js ;\
            do \
              envsubst '\$APIMAN_UI_REST_URL \$KEYCLOAK_AUTH_URL ' < \$mainFileName > main.tmp ;\
              mv main.tmp \${mainFileName} ;\
            done \
            && nginx -g 'daemon off;'" > run.sh

ENTRYPOINT ["sh", "run.sh"]
