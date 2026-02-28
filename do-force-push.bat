
@echo off
cd /d d:\ai-agentic
echo === Force pushing main branch... ===
git push -f origin main
echo.
echo === Done! ===
git status
echo.
echo === Checking remote branches... ===
git branch -r
pause
