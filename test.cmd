@echo off
REM This is a test cmd file for windows
REM This file will be executed by file watcher on every file change in the project root
REM
REM start file watchig by the command:
REM ./filewatch.sh -c "cmd /C test.cmd"
REM
REM run it from an msysgit git bash

echo Watched file : %WATCHED_FILE%
echo in directory : %WATCHED_DIR%
echo watched extension : %WATCHED_EXTENSION%

REM Filewatcher also sets System properties :
REM echo sun.boot.class.path : %sun.boot.class.path%
