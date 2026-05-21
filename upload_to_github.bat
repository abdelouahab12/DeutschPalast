@echo off
echo ===================================================
echo   Uploading DeutschPalast to GitHub
echo ===================================================
cd /d "%~dp0"
git init
git add .
git update-index --chmod=+x gradlew >nul 2>&1
git commit -m "Fix Material 3 outlinedCardBorder compatibility and add lifecycle-viewmodel-ktx"
git branch -M main
git remote remove origin >nul 2>&1
git remote add origin https://github.com/abdelouahab12/DeutschPalast.git
echo.
echo Attempting to push to GitHub...
git push -u origin main
echo ===================================================
if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Code uploaded successfully!
) else (
    echo [ERROR] Failed to push code. Please ensure you are logged in to Git or have set up your credentials.
)
pause
