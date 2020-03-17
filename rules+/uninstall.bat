@echo off
setlocal enabledelayedexpansion
for /f "delims==" %%a in ('dir *.jar /b') do (
set "b=%%a"
set "c=!b:~0,11!"
if "!c!"=="p3c-common-" (
   start winrar a -o+ -ep -aprulesets/java %%a origin/ali-pmd.xml
   echo %%a
  )
)
