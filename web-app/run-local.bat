@echo off
title Northstar Bank - Local Web App
cd /d "%~dp0"
echo Starting Northstar Bank...
echo Open on this laptop: http://localhost:8080
echo On another device use your laptop IPv4 address: http://YOUR-LAPTOP-IP:8080
echo.
call mvnw.cmd spring-boot:run
pause
