@echo off
REM ======================================================================
REM  File: deploy-iis.bat
REM  Project: Smart Solar Microgrid Trading System (SE4040)
REM  Description: Publishes the Web API and hosts it on IIS
REM               (site SmartSolarApi, port 5235, No Managed Code pool).
REM  Usage: Right-click this file -> Run as administrator.
REM  Location: deployment\iis\deploy-iis.bat
REM ======================================================================
setlocal

set "SITE=SmartSolarApi"
set "PORT=5235"
set "PUBLISH_DIR=C:\inetpub\SmartSolarApi"
set "API_DIR=%~dp0..\..\backend\SmartSolarMicrogrid.Api"
set "APPCMD=%windir%\system32\inetsrv\appcmd.exe"
set "ANCM=%ProgramFiles%\IIS\Asp.Net Core Module\V2\aspnetcorev2.dll"

echo.
echo ===== Smart Solar Microgrid - IIS deployment =====
echo.

REM ---- 1. Must run as administrator ----
net session >nul 2>&1
if errorlevel 1 goto :noadmin

REM ---- 2. Prerequisites ----
if not exist "%APPCMD%" goto :noiis
echo [OK] IIS is installed
if not exist "%ANCM%" goto :nobundle
echo [OK] ASP.NET Core Hosting Bundle is installed
where dotnet >nul 2>&1
if errorlevel 1 goto :nodotnet
echo [OK] .NET SDK found
if not exist "%API_DIR%\SmartSolarMicrogrid.Api.csproj" goto :noproject
echo [OK] API project found

REM ---- 3. Stop the site and pool if they already exist ----
"%APPCMD%" list site "%SITE%" >nul 2>&1
if not errorlevel 1 "%APPCMD%" stop site "%SITE%" >nul 2>&1
"%APPCMD%" list apppool "%SITE%" >nul 2>&1
if not errorlevel 1 "%APPCMD%" stop apppool "%SITE%" >nul 2>&1

REM ---- 4. Publish the API ----
echo.
echo Publishing the API to %PUBLISH_DIR% ...
dotnet publish "%API_DIR%\SmartSolarMicrogrid.Api.csproj" -c Release -o "%PUBLISH_DIR%"
if errorlevel 1 goto :publishfail
echo [OK] Publish succeeded

REM ---- 5. MongoDB settings (server only, never committed) ----
if exist "%PUBLISH_DIR%\appsettings.Production.json" goto :settingsok
echo.
echo appsettings.Production.json not found. It holds the MongoDB connection string.
set /p "SS_CONN=Paste the MongoDB connection string and press Enter: "
if not defined SS_CONN goto :noconn
powershell -NoProfile -Command "$s=@{SmartSolarMicrogridDatabase=@{ConnectionString=$env:SS_CONN;DatabaseName='SmartSolarMicrogrid'}}; $s | ConvertTo-Json | Set-Content -Encoding UTF8 '%PUBLISH_DIR%\appsettings.Production.json'"
if errorlevel 1 goto :settingsfail
:settingsok
echo [OK] appsettings.Production.json present

REM ---- 6. Application pool: No Managed Code ----
"%APPCMD%" list apppool "%SITE%" >nul 2>&1
if errorlevel 1 "%APPCMD%" add apppool /name:"%SITE%" >nul
"%APPCMD%" set apppool "%SITE%" /managedRuntimeVersion:"" >nul
echo [OK] Application pool %SITE% set to No Managed Code

REM ---- 7. IIS site on port 5235 ----
"%APPCMD%" list site "%SITE%" >nul 2>&1
if errorlevel 1 "%APPCMD%" add site /name:"%SITE%" /physicalPath:"%PUBLISH_DIR%" /bindings:http/*:%PORT%: >nul
"%APPCMD%" set app "%SITE%/" /applicationPool:"%SITE%" >nul
echo [OK] IIS site %SITE% on port %PORT%

REM ---- 8. Firewall rule so other devices can reach the API ----
netsh advfirewall firewall show rule name="SmartSolar API" >nul 2>&1
if errorlevel 1 netsh advfirewall firewall add rule name="SmartSolar API" dir=in action=allow protocol=TCP localport=%PORT% >nul
echo [OK] Firewall port %PORT% open

REM ---- 9. Start everything ----
"%APPCMD%" start apppool "%SITE%" >nul 2>&1
"%APPCMD%" start site "%SITE%" >nul 2>&1
echo [OK] Site started

REM ---- 10. Test the API ----
echo.
echo Testing http://localhost:%PORT%/api/stations (first call can take a few seconds) ...
powershell -NoProfile -Command "try { $r = Invoke-WebRequest -UseBasicParsing -TimeoutSec 60 'http://localhost:%PORT%/api/stations'; if ($r.Content -match 'success.{1,3}true') { exit 0 } else { exit 2 } } catch { exit 1 }"
if errorlevel 1 goto :testfail
echo [OK] API responded with data from MongoDB
echo.
echo ===== DEPLOYMENT SUCCESSFUL =====
echo API URL: http://localhost:%PORT%/api/stations
echo Now run check-iis.bat to verify everything.
goto :end

:noadmin
echo [FAIL] Please right-click deploy-iis.bat and choose "Run as administrator".
goto :end
:noiis
echo [FAIL] IIS is not installed.
echo        Turn Windows features on or off - tick Internet Information Services - OK.
goto :end
:nobundle
echo [FAIL] ASP.NET Core Hosting Bundle is not installed.
echo        Download "Hosting Bundle" for ASP.NET Core Runtime 10.x from
echo        https://dotnet.microsoft.com/download/dotnet/10.0 then run this file again.
goto :end
:nodotnet
echo [FAIL] .NET SDK not found. Install the .NET 10 SDK and try again.
goto :end
:noproject
echo [FAIL] API project not found at %API_DIR%
echo        Keep this file in deployment\iis inside the project folder.
goto :end
:publishfail
echo [FAIL] dotnet publish failed. Read the errors above.
goto :end
:noconn
echo [FAIL] No connection string entered. Run the file again.
goto :end
:settingsfail
echo [FAIL] Could not create appsettings.Production.json.
goto :end
:testfail
echo [FAIL] The API did not return data.
echo        HTTP 500.30 usually means MongoDB is unreachable: check the connection string
echo        in %PUBLISH_DIR%\appsettings.Production.json and MongoDB Atlas Network Access.
goto :end

:end
echo.
pause
endlocal