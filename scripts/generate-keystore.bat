@echo off
setlocal EnableDelayedExpansion

if "%~1"=="" (
  echo Keystore path must be provided
  exit /b 1
)
set "KEYSTORE_PATH=%~1"
if defined SERVER_SSL_KEY_ALIAS (
  set "KEY_ALIAS=%SERVER_SSL_KEY_ALIAS%"
) else (
  set "KEY_ALIAS=server"
)
if defined SERVER_SSL_KEYSTORE_PASSWORD (
  set "STORE_PASSWORD=%SERVER_SSL_KEYSTORE_PASSWORD%"
) else (
  set "STORE_PASSWORD=changeit"
)
if defined SERVER_SSL_VALIDITY_DAYS (
  set "VALIDITY=%SERVER_SSL_VALIDITY_DAYS%"
) else (
  set "VALIDITY=3650"
)
for %%F in ("%KEYSTORE_PATH%") do set "KEYSTORE_DIR=%%~dpF"
if not exist "!KEYSTORE_DIR!" mkdir "!KEYSTORE_DIR!"
if exist "%KEYSTORE_PATH%" (
  echo Keystore already exists at %KEYSTORE_PATH%
  exit /b 0
)

keytool -genkeypair ^
  -alias "%KEY_ALIAS%" ^
  -keyalg RSA ^
  -keysize 2048 ^
  -storetype PKCS12 ^
  -keystore "%KEYSTORE_PATH%" ^
  -storepass "%STORE_PASSWORD%" ^
  -keypass "%STORE_PASSWORD%" ^
  -dname "CN=localhost, OU=Project Manager, O=Example Inc., L=Budapest, ST=Budapest, C=HU" ^
  -validity "%VALIDITY%"

endlocal
