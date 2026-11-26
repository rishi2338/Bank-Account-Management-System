# Spring Boot web application — Render deployment

This repository keeps the original Java desktop project and adds the Northstar Spring Boot browser application in the compressed source archive `northstar-web-app.tar.gz.b64`.

## Deploy on Render

1. In Render, create **New → Web Service** and connect this repository.
2. Select branch **render-deploy**.
3. Choose **Docker** as the runtime.
4. Set Dockerfile Path to `./Dockerfile`.
5. Select the Free instance if available, then deploy.

## Database configuration

The web app needs a remotely reachable MySQL database. Add these environment variables in Render:

- `DB_URL`: JDBC URL for the hosted MySQL database, including database name and connection options.
- `DB_USERNAME`: database username.
- `DB_PASSWORD`: database password.

Do not use `localhost` for the hosted database URL: inside Render, localhost means the Render container itself. Do not commit credentials to GitHub.

The app defaults to port 8080 and reads Render's `PORT` variable through the Docker start command.

## Local run

Download and decode `northstar-web-app.tar.gz.b64` to `northstar-web-app.tar.gz`, extract it, then open the `web-app` directory and run `mvn spring-boot:run` after configuring `DB_USERNAME`, `DB_PASSWORD`, and optionally `DB_URL`.

## Demo limitation

This is an educational banking simulation, not a real bank. Deposits, withdrawals, transfers and loan approvals are simulated. It does not connect to UPI, NEFT, RTGS, payment gateways, KYC providers or real bank accounts.
