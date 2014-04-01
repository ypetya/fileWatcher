@echo off
REM This is a test cmd file for windows
REM This file will be executed by file watcher on every file change in the project root
REM
REM start file watchig by the command:
REM ./filewatch.sh -c "run_jsdoc.cmd" -d <javascript project dir> --skipDirectories .git
REM
REM run it from an msysgit git bash

if %WATCHED_EXTENSION%==js (
	echo Change detected : %WATCHED_DIR%\%WATCHED_FILE%
	D:\DEV\opt\nodejs\node.exe D:\DEV\projects\node_modules\jsdoc\jsdoc.js %WATCHED_DIR%\%WATCHED_FILE% -d D:\tmp\ --verbose
	echo finished
)

REM Filewatcher also sets System properties :
REM echo sun.boot.class.path : %sun.boot.class.path%
