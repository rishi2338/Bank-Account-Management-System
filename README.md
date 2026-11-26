# Bank Account Management System

Java Swing banking application with SQLite.

## Desktop version
Requires JDK 17 and Maven. Run `mvn clean compile exec:java`.

Features: account creation, PIN login, deposits, withdrawals, balance enquiry and transaction history.

## Browser / local Wi-Fi version
A separate Java 17 + Spring Boot web application is available in [web-app](web-app/README.md). It uses MySQL and can be opened at `http://localhost:8080` or from another device on the same Wi-Fi using your laptop's IPv4 address.
