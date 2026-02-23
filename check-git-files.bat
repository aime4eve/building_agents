
@echo off
cd /d d:\ai-agentic
echo === Git Log (Last 3 commits) ===
git log --oneline -3
echo.
echo === Git Files in Repository ===
git ls-files | head -100
echo.
echo === Git Status ===
git status
pause
