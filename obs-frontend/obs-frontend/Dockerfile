# Stage 1: Build the Angular application
FROM node:18-alpine AS builder

RUN npm install -g @angular/cli@17 typescript

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

RUN ng build --configuration production

# Stage 2: Serve with nginx
FROM nginx:alpine

COPY --from=builder /app/dist/obs-frontend/browser /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
