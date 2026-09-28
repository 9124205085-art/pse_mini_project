@echo off
setlocal
cd /d "%~dp0"
call compile.bat
if errorlevel 1 exit /b 1

if not defined JAVA_HOME (
  for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do (
    if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
  )
)
if not defined JAVA_HOME if exist "tools\jdk\bin\java.exe" set "JAVA_HOME=%~dp0tools\jdk"
if not defined JAVA_HOME (
  echo Java was not found. Install JDK 17 and run this file again.
  exit /b 1
)

"%JAVA_HOME%\bin\java.exe" -cp "out;lib\sqlite-jdbc-3.46.1.3.jar" passport.Main
