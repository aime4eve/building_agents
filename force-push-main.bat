
@echo off
cd /d d:\ai-agentic
echo WARNING: This will FORCE PUSH and OVERWRITE the remote main branch!
echo.
echo Local commits that will be pushed:
git log origin/main..main --oneline
echo.
echo Are you sure you want to continue? (Press Ctrl+C to cancel, any key to continue)
pause
echo.
echo Force pushing main branch...
git push -f origin main
echo.
echo Done!
git status
pause
