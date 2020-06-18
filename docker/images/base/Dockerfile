FROM node:12.18.0

# Start with npm tasks
WORKDIR /usr/src/app

COPY package*.json ./

RUN npm install
#copy the rest of the project into the image
COPY . .
