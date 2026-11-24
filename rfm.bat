@echo off
setlocal EnableExtensions DisableDelayedExpansion

if not defined REALLYFASTMODE_CLI_PORT set "REALLYFASTMODE_CLI_PORT=18775"
if "%~1"=="" goto check_connection

set "rfmJar=%~dp0target\ReallyFastMode.jar"
if not exist "%rfmJar%" set "rfmJar=%~dp0ReallyFastMode.jar"
if not exist "%rfmJar%" (
    >&2 echo ReallyFastMode.jar is missing. Build the mod or place the JAR beside this script.
    exit /b 2
)

java "-Dreallyfastmode.cli.port=%REALLYFASTMODE_CLI_PORT%" -jar "%rfmJar%" %*
exit /b %errorlevel%

:check_connection
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -NonInteractive -Command ^
    "$port = 0; if (-not [int]::TryParse($env:REALLYFASTMODE_CLI_PORT, [ref]$port) -or $port -lt 1 -or $port -gt 65535) { [Console]::Error.WriteLine('REALLYFASTMODE_CLI_PORT must be an integer from 1 to 65535.'); exit 2 };" ^
    "$client = New-Object Net.Sockets.TcpClient; try { $client.Connect('127.0.0.1', $port); Write-Output ('CLI port is open on 127.0.0.1:{0}.' -f $port) }" ^
    "catch { [Console]::Error.WriteLine(('Could not reach 127.0.0.1:{0}. Check that Slay the Spire is running with ReallyFastMode.' -f $port)); exit 2 } finally { $client.Dispose() }"
exit /b %errorlevel%
