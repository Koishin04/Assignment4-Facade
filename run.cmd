@echo off
cd /d "%~dp0"
call mvn clean compile exec:java