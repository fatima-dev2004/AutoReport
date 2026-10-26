@echo off
echo ========================================
echo Demarrage du Backend Reporting
echo ========================================
echo.

cd /d "%~dp0"

echo Utilisation de la base de donnees H2 (en memoire)
echo.
echo Demarrage de l'application Spring Boot...
echo L'application sera accessible sur http://localhost:8080
echo Console H2: http://localhost:8080/h2-console
echo.
call mvn spring-boot:run -Dspring-boot.run.profiles=h2

pause
