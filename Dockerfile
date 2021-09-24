FROM node:14.17.6 as base
MAINTAINER florian.volk@scheer-group.com

# Start with npm tasks
WORKDIR /usr/src/app

COPY package*.json ./

RUN npm ci
#copy the rest of the project into the image
COPY . .

RUN npm run build-production

# Stage 2
FROM nginx:1.21.3-alpine
MAINTAINER florian.volk@scheer-group.com
EXPOSE 80 443

# one of pas-service|pas-app|database|xuml-service|python-service|other
LABEL com.scheer-pas.container-type="pas-app"

COPY --from=base /usr/src/app/dist/*/* /usr/share/nginx/html/

#copy ssl keys
COPY docker/tls.crt /etc/ssl/certs/tls.crt
COPY docker/tls.key /etc/ssl/private/tls.key

#copy nginx configuration
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
#copy start script
COPY docker/start-nginx-with-devportal.sh /start-nginx-with-devportal.sh

ENTRYPOINT ["sh", "start-nginx-with-devportal.sh"]
