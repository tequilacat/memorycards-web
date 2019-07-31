@echo off

set db=%~dp0..\mongodb

if not exist "%db%" mkdir "%db%"

start cmd /c G:\dev\Tools\mongodb-win32-x86_64-2008plus-ssl-4.0.10\bin\mongod.exe --dbpath %db%
