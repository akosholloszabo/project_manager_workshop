@echo off
setlocal EnableDelayedExpansion

set "REPO_ROOT=%~dp0.."
set "SERVER_BIN=%REPO_ROOT%\server\build\install\server\bin\server.bat"
if not exist "%SERVER_BIN%" (
  echo Server binary not found at %SERVER_BIN%; attempting build...
  pushd "%REPO_ROOT%" >nul
  if errorlevel 1 (
    echo Failed to access %REPO_ROOT% on disk.
  ) else (
    call "gradlew.bat" :server:installDist
    popd >nul
  )
)
if exist "%SERVER_BIN%" (
  call "%SERVER_BIN%" %*
) else (
  echo Server binary still missing after build. Aborting.
)
