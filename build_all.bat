@echo off
set JAVA_HOME=C:\Users\mojis\AppData\Local\Programs\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
set ADB=C:\Users\mojis\AppData\Local\Android\Sdk\platform-tools\adb.exe

echo === Java version ===
java -version
echo.

echo === Connected devices ===
%ADB% devices
echo.

echo ========================================
echo  Building Claude app...
echo ========================================
cd /d C:\proj\weather-ai-compare\projects\weather-claude
call gradlew.bat installDebug
if %errorlevel% neq 0 (
    echo [FAIL] Claude build failed
) else (
    echo [OK] Claude installed
)

echo.
echo ========================================
echo  Building Codex app...
echo ========================================
cd /d C:\proj\weather-ai-compare\projects\weather-codex
call gradlew.bat installDebug
if %errorlevel% neq 0 (
    echo [FAIL] Codex build failed
) else (
    echo [OK] Codex installed
)

echo.
echo ========================================
echo  Building Gemini app...
echo ========================================
cd /d C:\proj\weather-ai-compare\projects\weather-gemini
call gradlew.bat installDebug
if %errorlevel% neq 0 (
    echo [FAIL] Gemini build failed
) else (
    echo [OK] Gemini installed
)

echo.
echo ========================================
echo  All done. Installed apps:
echo ========================================
%ADB% shell pm list packages | findstr weathernow
