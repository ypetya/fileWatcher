# FileWatcher

## What is this?

This is a command-line utility.
FileWatcher monitors a filesystem recursively and will run the specified command
 on file or directory changes.

## What was the main purpose of this tool?

I have done some experiments with autamtic maven junit execution.

## What kind of command should I use ?

The command runs in the file's parent directory.
FileWatcher sets the $WATCHED_DIR and $WATCHED_FILE environment variables on execution.
The easiest way is to setup a bash file to execute.

## How to build?

```shell
mvn assembly:single
```

## How to run?

```shell
java -jar target/filewatcher.jar <-d directory> <-c command>

java -jar target/filewatcher.jar -c "echo \"dir: $WATCHED_DIR file:$WATCHED_FILE\""
java -jar target/filewatcher.jar -c "cmd /C echo Watched directory : %WATCHED_DIR% Watched file : %WATCHED_FILE%"
./filewatch.sh -c "cmd /C echo %WATCHED_FILE%"
./filewatch.sh -c "cmd /C echo %WATCHED_EXTENSION%"
./filewatch.sh -c "cmd /C test.cmd"
# do not do this if you have a load of files > 10k :
./filewatch.sh -c "cmd /C test.cmd" -d "C:\DEV" --skipDirectories ".git,.svn"
```

## Are there dependencies?

FileWatcher uses java7's nio FileVisitor and WatchService, so you have to use it
at least java7.
