@echo off
rem ============================================================================
rem  Dameng DM8 migration verification runner  (Windows cmd)   v2
rem
rem  Purpose: execute backend-scaffold/src/main/resources/db/migration/dameng/
rem           V*.sql against a REAL DM8 instance, in numeric version order.
rem
rem  Why not Flyway: official Flyway distributions do NOT ship a Dameng module.
rem
rem  !! RUN AS ADMINISTRATOR !!  (normal cmd gives "Access is denied" here)
rem
rem  v2 changes (after the first real run):
rem   - Encoding fix: disql reads scripts with the OS code page (GBK) by
rem     default, while our SQL files are UTF-8 -> Chinese became mojibake and
rem     swallowed closing quotes. We now drive disql through a generated master
rem     script that does `SET CHAR_CODE UTF8` (official setting for the `start`
rem     command) plus `SET LOCAL_CODE UTF8` so the log itself is UTF-8 too.
rem   - `SET DEFINE OFF` so a stray `&` in SQL text cannot trigger substitution.
rem   - A `select 'MARKER_Vn ...' from dual;` is emitted before each file so the
rem     log can be segmented per migration file.
rem
rem  NOTE: messages here are ASCII-only (cmd parses .cmd in the OEM code page).
rem  NOTE: the password is never stored in this file - type it at the prompt.
rem ============================================================================
setlocal
chcp 65001 >nul

rem ------------------------------- CONFIG ------------------------------------
set "DM_HOME=D:\dameng"
set "DM_HOST=localhost"
set "DM_PORT=5236"
set "DM_USER=SYSDBA"
rem CLEAN=1: first drop EVERY table created by the dameng migrations, so the
rem whole chain runs from an EMPTY schema. Needed because re-running migrations
rem on a dirty schema produces fake errors (object already exists / duplicate
rem key / column already exists). Set CLEAN=0 to keep existing data.
rem Can be overridden from the environment:  set CLEAN=0  before running.
rem (On a FRESH instance the schema is already empty -> use CLEAN=0, otherwise
rem  dameng-drop-all.sql emits 140 harmless "invalid table name" errors.)
if not defined CLEAN set "CLEAN=1"
rem ---------------------------------------------------------------------------

if not defined DM_PWD set /p "DM_PWD=Enter password for %DM_USER%: "
if not defined DM_PWD (
  echo [ERROR] no password given.
  exit /b 1
)

set "MIG_DIR=%~dp0..\src\main\resources\db\migration\dameng"
set "LOG=%~dp0..\..\dameng-migrate.log"
set "MASTER=%TEMP%\dm_run_all.sql"
set "CONN=%DM_USER%/%DM_PWD%@%DM_HOST%:%DM_PORT%"

echo.
echo === Dameng DM8 migration verification (v2) ===
echo   connect : %DM_USER%@%DM_HOST%:%DM_PORT%
echo   scripts : %MIG_DIR%
echo   log     : %LOG%
echo.

if not exist "%DM_HOME%\bin\disql.exe" (
  echo [ERROR] disql not found: %DM_HOME%\bin\disql.exe
  exit /b 1
)

rem ---- build the master script (CHAR_CODE must be set BEFORE the starts) -----
break>"%MASTER%"
>>"%MASTER%" echo SET CHAR_CODE UTF8
>>"%MASTER%" echo SET LOCAL_CODE UTF8
>>"%MASTER%" echo SET DEFINE OFF
>>"%MASTER%" echo SET ECHO ON
>>"%MASTER%" echo SET FEEDBACK OFF
>>"%MASTER%" echo SET TIMING OFF
>>"%MASTER%" echo SET PAGESIZE 0
if "%CLEAN%"=="1" (
  if exist "%~dp0dameng-drop-all.sql" (
    echo [WARN] CLEAN=1 - all tables will be DROPPED before running.
    >>"%MASTER%" echo start "%~dp0dameng-drop-all.sql"
  ) else (
    echo [WARN] CLEAN=1 but drop script not found: %~dp0dameng-drop-all.sql
  )
)
for /L %%i in (1,1,99) do (
  for %%f in ("%MIG_DIR%\V%%i__*.sql") do (
    if exist "%%f" (
      >>"%MASTER%" echo select 'MARKER_V%%i %%~nxf' from dual;
      >>"%MASTER%" echo start "%%f"
    )
  )
)
>>"%MASTER%" echo exit

echo [RUN] executing via generated master script: %MASTER%
"%DM_HOME%\bin\disql.exe" "%CONN%" < "%MASTER%" > "%LOG%" 2>&1
if errorlevel 1 (
  echo [ERROR] disql failed, see %LOG%
  exit /b 1
)

echo.
echo === done ===
echo Log: %LOG%
echo Now scan the log for MARKER_V lines and DM error codes:
echo   findstr /c:"MARKER_V" "%LOG%"
echo   findstr /c:"[-" "%LOG%"
endlocal
