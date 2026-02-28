
@echo off
cd /d d:\ai-agentic
echo Current branch:
git branch
echo.
echo Creating and switching to main branch...
git checkout -b main master
echo.
echo Pushing main branch to GitHub...
git push -u origin main
echo.
echo Deleting local master branch...
git branch -d master
echo.
echo Deleting remote master branch...
git push origin --delete master
echo.
echo Setting main as default branch...
git branch --set-upstream-to=origin/main main
echo.
echo Done!
git branch -a
pause
