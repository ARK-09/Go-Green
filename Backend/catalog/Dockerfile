# Specifying Base image
FROM node:alpine

# Adding working directory
WORKDIR /usr/app

# Copying required files
COPY package.json .
RUN npm install
COPY . .

CMD ["npm", "start"]