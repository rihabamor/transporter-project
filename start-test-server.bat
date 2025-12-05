@echo off
echo.
echo ============================================
echo  WebSocket Test Page Server
echo ============================================
echo.
echo Starting HTTP server on port 8000...
echo.
echo Once started, open your browser to:
echo   http://localhost:8000/websocket-test.html
echo.
echo Press Ctrl+C to stop the server
echo.
echo ============================================
echo.

REM Check if Python is available
where python >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Using Python HTTP Server...
    python -m http.server 8000
) else (
    REM Check if Node.js is available
    where node >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo Using Node.js HTTP Server...
        npx http-server -p 8000
    ) else (
        echo ERROR: Neither Python nor Node.js found!
        echo.
        echo Please install Python or Node.js, or use VS Code Live Server extension.
        echo.
        pause
    )
)
