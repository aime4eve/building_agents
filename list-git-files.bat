
@echo off
cd /d d:\ai-agentic
echo Checking git ls-files...
git ls-files > git-files.txt
echo Files in git:
type git-files.txt
echo.
echo Checking directories...
dir /b
pause
