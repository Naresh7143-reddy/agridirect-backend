@echo off
REM AgriDirect Backend Local Startup (Supabase DB, port 8090)
echo Starting AgriDirect backend on port 8090 with local profile...
mvnw.cmd spring-boot:run -DskipTests "-Dspring-boot.run.profiles=local"
