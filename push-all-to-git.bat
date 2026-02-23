
@echo off
cd /d d:\ai-agentic
echo Current directory: %CD%
echo.
echo Checking git status...
git status
echo.
echo Checking remote...
git remote -v
echo.
echo Adding all files...
git add -A
echo.
echo Committing...
git commit -m "Complete project: devops, database, docs, hkt-iot-platform, hkt-iot-web, huakuangtong-agent"
echo.
echo Pushing to GitHub...
git push -u origin master
echo.
echo Done!
pause
