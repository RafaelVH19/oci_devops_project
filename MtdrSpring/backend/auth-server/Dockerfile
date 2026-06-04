FROM node:22-bookworm-slim

WORKDIR /app

# better-sqlite3 compiles native bindings
RUN apt-get update \
  && apt-get install -y --no-install-recommends python3 make g++ \
  && rm -rf /var/lib/apt/lists/*

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

RUN sed -i 's/\r$//' docker-entrypoint.sh && chmod +x docker-entrypoint.sh

EXPOSE 3001

ENV AUTH_SERVER_PORT=3001

ENTRYPOINT ["sh", "docker-entrypoint.sh"]
