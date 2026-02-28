
@echo off
cd /d d:\ai-agentic
echo === Current Git Status ===
git status
echo.
echo === Remote Repositories ===
git remote -v
echo.
echo === Branches ===
git branch -a
echo.
echo === Last 3 Commits ===
git log --oneline -3
echo.
echo === Trying to push main branch... ===
git push -u origin main
pause
