@echo off
REM ======================================================================
REM  File: check-iis.bat
REM  Project: Smart Solar Microgrid Trading System (SE4040)
REM  Description: Checks the IIS deployment of the Web API and prints
REM               PASS / FAIL for each requirement. Changes nothing.
REM  Usage: Right-click this file -> Run as administrator.
REM  Location: deployment\iis\check-iis.bat
REM ======================================================================
setlocal

set "SITE=SmartSolarApi"
set "PORT=5235"
set "PUBLISH_DIR=C:\inetpub\SmartSolarApi"
set "APPCMD=%windir%\system32\inetsrv\appcmd.exe"
set "ANCM=%ProgramFiles%\IIS\Asp.Net Core Module\V2\aspnetcorev2.dll"
set /a PASS=0
set /a FAIL=0

echo.
echo ===== Smart Solar Microgrid - IIS deployment check =====
echo.

net session >nul 2>&1
if errorlevel 1 (
  echo Please right-click check-iis.bat and choose "Run as administrator".
  goto :end
)

if exist "%APPCMD%" (call :pass "IIS is installed") else (call :fail "IIS is installed")
if exist "%ANCM%" (call :pass "ASP.NET Core Hosting Bundle installed") else (call :fail "ASP.NET Core Hosting Bundle installed")
if exist "%PUBLISH_DIR%\SmartSolarMicrogrid.Api.dll" (call :pass "Published API dll exists") else (call :fail "Published API dll exists")
if exist "%PUBLISH_DIR%\web.config" (call :pass "web.config exists") else (call :fail "web.config exists")
if exist "%PUBLISH_DIR%\appsettings.Production.json" (call :pass "appsettings.Production.json exists") else (call :fail "appsettings.Production.json exists")

if not exist "%APPCMD%" goto :summary

REM ---- IIS site and pool ----
set "STATE="
for /f "delims=" %%A in ('%APPCMD% list site "%SITE%" /text:state 2^>nul') do set "STATE=%%A"
call :result "IIS site %SITE% is Started (now: %STATE%)" "%STATE%" "Started"

set "BIND="
for /f "delims=" %%A in ('%APPCMD% list site "%SITE%" /text:bindings 2^>nul') do set "BIND=%%A"
echo %BIND% | find ":%PORT%:" >nul
if errorlevel 1 (call :fail "Site binding uses port %PORT% (now: %BIND%)") else (call :pass "Site binding uses port %PORT%")

set "POOLSTATE="
for /f "delims=" %%A in ('%APPCMD% list apppool "%SITE%" /text:state 2^>nul') do set "POOLSTATE=%%A"
call :result "Application pool %SITE% is Started (now: %POOLSTATE%)" "%POOLSTATE%" "Started"

set "CLR=none"
for /f "delims=" %%A in ('%APPCMD% list apppool "%SITE%" /text:managedRuntimeVersion 2^>nul') do set "CLR=%%A"
if "%POOLSTATE%"=="" (
  call :fail "Application pool %SITE% exists"
) else (
  if "%CLR%"=="none" (call :pass "Application pool uses No Managed Code") else (call :fail "Application pool uses No Managed Code (now: %CLR%)")
)

REM ---- Firewall ----
netsh advfirewall firewall show rule name="SmartSolar API" >nul 2>&1
if errorlevel 1 (call :fail "Firewall rule for port %PORT%") else (call :pass "Firewall rule for port %PORT%")

REM ---- Live API test (also proves MongoDB works) ----
echo.
echo Calling http://localhost:%PORT%/api/stations ...
powershell -NoProfile -Command "try { $r = Invoke-WebRequest -UseBasicParsing -TimeoutSec 60 'http://localhost:%PORT%/api/stations'; if ($r.Content -match 'success.{1,3}true') { exit 0 } else { exit 2 } } catch { exit 1 }"
if errorlevel 1 (call :fail "API returns station data from MongoDB") else (call :pass "API returns station data from MongoDB")

:summary
echo.
echo ----------------------------------------------
echo  PASSED: %PASS%    FAILED: %FAIL%
if %FAIL%==0 (
  echo  RESULT: Deployment is correct.
) else (
  echo  RESULT: Fix the FAIL items, or run deploy-iis.bat.
)
echo ----------------------------------------------
echo.
echo Network address for other devices / Android phone:
for /f "tokens=2 delims=:" %%A in ('ipconfig ^| findstr /c:"IPv4"') do for /f "tokens=*" %%B in ("%%A") do echo   http://%%B:%PORT%/api/stations
goto :end

REM ---- helpers ----
:result
if /i "%~2"=="%~3" (call :pass %1) else (call :fail %1)
exit /b
:pass
echo [PASS] %~1
set /a PASS+=1
exit /b
:fail
echo [FAIL] %~1
set /a FAIL+=1
exit /b

:end
echo.
pause
endlocal