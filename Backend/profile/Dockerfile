# Specifying Base image
FROM node:alpine

# Adding working directory
WORKDIR /usr/app

# Copying required files
COPY package.json .
RUN npm install
RUN npm audit fix --force
COPY . .

CMD ["npm", "start"]