FROM node:16.17.1 as base
MAINTAINER florian.volk@scheer-group.com

# Start with npm tasks
WORKDIR /usr/src/app

COPY package*.json ./

RUN npm ci
#copy the rest of the project into the image
COPY . .

RUN npm run build-production

# Stage 2
FROM nginx:1.23.1-alpine
MAINTAINER florian.volk@scheer-group.com
EXPOSE 80 443

# copy files in the ngninx server folder
COPY --from=base /usr/src/app/dist/devportal /usr/share/nginx/html
# copy config to different folder to run envsubst before starting
COPY --from=base /usr/src/app/dist/devportal/assets/config.json5 /usr/share/devportal/assets/config.json5

#copy ssl keys
COPY docker/tls.crt /etc/ssl/certs/tls.crt
COPY docker/tls.key /etc/ssl/private/tls.key

#copy nginx configuration
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
#copy start script
COPY docker/start-nginx-with-devportal.sh /start-nginx-with-devportal.sh

ENTRYPOINT ["sh", "start-nginx-with-devportal.sh"]
