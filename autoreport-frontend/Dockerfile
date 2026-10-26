# Dockerfile multi-stage pour le frontend (Vite/React)
# Stage 1 : build
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .

# VITE_API_BASE_URL et VITE_GEMINI_API_KEY sont injectees au build (Vite les
# embarque en dur dans le bundle JS, elles ne sont donc pas modifiables au
# runtime sans rebuild). Passees via docker-compose (voir docker-compose.yml).
ARG VITE_API_BASE_URL
ARG VITE_GEMINI_API_KEY
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_GEMINI_API_KEY=${VITE_GEMINI_API_KEY}

RUN npm run build

# Stage 2 : nginx pour servir les fichiers statiques
FROM nginx:1.27-alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
