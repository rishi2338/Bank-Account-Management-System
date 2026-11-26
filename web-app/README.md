# Bank Account Management System — Local Web Edition

This folder adds a Java 17 + Spring Boot browser-based edition. The original Java Swing desktop application remains unchanged.

## Features
- Account registration and login with BCrypt password hashing
- Generated 10-digit account numbers
- Balance dashboard, deposit, withdrawal and account-to-account transfers
- Latest 100 transactions
- Responsive browser UI for laptop and phone on the same Wi-Fi
- MySQL persistence

> Educational demonstration only. Do not use real money, real credentials or production banking data. This is not a regulated banking system.

## Requirements
- JDK 17 or newer
- MySQL Server 8.x
- Maven installed (or add the Maven Wrapper)

## 1. Create the database
Run in MySQL Workbench:
```sql
CREATE DATABASE bank_management;
```

## 2. Configure MySQL
Edit `src/main/resources/application.properties` or set environment variables:
- DB_URL (default: jdbc:mysql://localhost:3306/bank_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC)
- DB_USERNAME (default: root)
- DB_PASSWORD (default: blank)

## 3. Run on Windows
Open PowerShell in this folder and run:
```powershell
mvn spring-boot:run
```
Open http://localhost:8080.

## 4. Use from a phone on the same Wi-Fi
1. Connect laptop and phone to the same Wi-Fi.
2. Run `ipconfig` in Windows Command Prompt.
3. Find the active Wi-Fi adapter's IPv4 Address.
4. Open `http://YOUR-IP:8080` on the phone.
5. If Windows Firewall asks, allow Java on Private networks only. Do not expose port 8080 publicly.

The server listens on 0.0.0.0 so local network devices can connect. Keep the laptop on and the application running.

## Troubleshooting
- MySQL connection refused: start MySQL80 service and check credentials.
- Port 8080 busy: stop the other application or change server.port.
- Phone cannot connect: check same Wi-Fi, current laptop IPv4 and Windows Private-network firewall.
- Maven command not found: install Apache Maven and reopen the terminal.

## Security
This is a local academic demo, not production banking software. It does not provide MFA, fraud monitoring, regulatory compliance or hardened public hosting. Do not port-forward this application to the internet.
