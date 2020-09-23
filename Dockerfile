FROM node:12.18.0 as base

# Start with npm tasks
WORKDIR /usr/src/app

COPY package*.json ./

RUN npm install
#copy the rest of the project into the image
COPY . .

RUN npm run build-production

# Stage 2
FROM nginx:1.19.2-alpine

COPY --from=base /usr/src/app/dist/api-mgmt-dev-portal /usr/share/nginx/html

#copy ssl keys
COPY docker/tls.crt /etc/ssl/certs/tls.crt
COPY docker/tls.key /etc/ssl/private/tls.key

#copy nginx configuration
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
#copy start script
COPY docker/start-nginx-with-devportal.sh /start-nginx-with-devportal.sh

ENTRYPOINT ["sh", "start-nginx-with-devportal.sh"]
