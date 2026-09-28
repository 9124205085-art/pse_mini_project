@echo off
setlocal
cd /d "%~dp0"

call :find_java
if not defined JAVA_HOME (
  echo Java JDK 17 or later is required.
  echo Install Eclipse Temurin 17, then run this file again.
  exit /b 1
)

if not exist "lib\sqlite-jdbc-3.46.1.3.jar" (
  echo Downloading SQLite JDBC driver...
  if not exist lib mkdir lib
  powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.3/sqlite-jdbc-3.46.1.3.jar' -OutFile 'lib\sqlite-jdbc-3.46.1.3.jar'"
  if not exist "lib\sqlite-jdbc-3.46.1.3.jar" (
    echo Could not download sqlite-jdbc. Check your internet connection.
    exit /b 1
  )
)

if not exist out mkdir out
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -cp "lib\sqlite-jdbc-3.46.1.3.jar" -d out src\passport\*.java
if errorlevel 1 (
  echo Compile failed.
  exit /b 1
)
echo Compiled successfully.
exit /b 0

:find_java
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" goto :eof
if exist "%~dp0tools\jdk\bin\javac.exe" (
  set "JAVA_HOME=%~dp0tools\jdk"
  goto :eof
)
for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do (
  if exist "%%D\bin\javac.exe" (
    set "JAVA_HOME=%%D"
    goto :eof
  )
)
for /d %%D in ("C:\Program Files\Java\jdk*") do (
  if exist "%%D\bin\javac.exe" (
    set "JAVA_HOME=%%D"
    goto :eof
  )
)
where javac >nul 2>nul
if not errorlevel 1 (
  for /f "delims=" %%I in ('where javac') do (
    set "JAVAC_PATH=%%I"
    goto :from_path
  )
)
goto :eof

:from_path
for %%I in ("%JAVAC_PATH%") do set "JAVA_BIN=%%~dpI"
for %%I in ("%JAVA_BIN%\..") do set "JAVA_HOME=%%~fI"
goto :eof
