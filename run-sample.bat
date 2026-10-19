@echo off
cd /d "%~dp0"
if not exist out mkdir out
javac -d out src\*.java
if errorlevel 1 (
  echo Compile failed. Make sure the JDK is installed.
  pause
  exit /b
)
java -Dtriqueue.dataDir=sample-data -cp out MainGUI
