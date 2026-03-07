FROM node:18-alpine

# Install Angular CLI and TypeScript globally — no local installation needed
RUN npm install -g @angular/cli@17 typescript

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

EXPOSE 4200

CMD ["ng", "serve", "--host", "0.0.0.0", "--poll", "2000", "--configuration", "production"]
