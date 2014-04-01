package tools;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileWatcher implements Runnable {

    private Path root;
    private String commandToExecute;
    private String[] skipDirectories;

    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;
    private final WatchService watcher;

    private final Factory factory;
    private final FileSystem fileSystem;
    private final Registry registry;

    FileWatcher(Factory factory) throws IOException {
        this.factory = factory;
        logger = factory.createLogger(this.getClass());
        fileSystem = factory.getFileSystem();
        watcher = fileSystem.newWatchService();
        registry = factory.createRegistry(watcher);
        factory.registerMBeans(registry);
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void run() {
        if (registry.registerWatchers(root)) {
            try {
                while (true) {
                    WatchKey key = watcher.take();

                    if (registry.checkHasADirectory(key)) {
                        handleEvents(key);
                        cleanUp(key);
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Exiting watch loop.", ex);
            }
        }
    }

    void handleEvents(WatchKey key) {
        Path dir = registry.get(key);
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind kind = event.kind();

            // TBD - provide example of how OVERFLOW event is handled
            if (kind == OVERFLOW) {
                continue;
            }

            // Context for directory entry event is the file name of entry
            WatchEvent<Path> ev = Factory.cast(event);
            Path name = ev.context();
            Path child = dir.resolve(name);

            logger.info(String.format("%s: %s\n", event.kind().name(), child));
            if (commandToExecute != null) {
                try {
                    factory.execute(commandToExecute, root, child);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Could not execute command : "
                            + commandToExecute, ex);
                }
            }

            // if directory is created, and watching recursively, then
            // register it and its sub-directories
            if (kind == ENTRY_CREATE) {
                if (factory.isDirectory(child)) {
                    registry.registerWatchers(child);
                }
            }
        }
    }

    void cleanUp(WatchKey key) {
        // reset key and remove from set if directory no longer accessible
        if (!key.reset()) {
            registry.remove(key);
            if (registry.getKeys().isEmpty()) {
                throw new IllegalStateException("All directories are inaccessible");
            }
        }
    }

    void setupByCommandLineArguments(String[] args) {
        String pathToWatch = ".";
        if (args != null) {
            if (args.length == 1) {
                pathToWatch = args[0];
            } else {
                LinkedList<String> largs = new LinkedList(Arrays.asList(args));
                while (!largs.isEmpty()) {
                    switch (largs.remove()) {
                        case "-d":
                            pathToWatch = largs.remove();
                            break;
                        case "-c":
                            commandToExecute = largs.remove();
                            logger.info("Command to execute : " + commandToExecute);
                            break;
                        case "--skipDirectories":
                            skipDirectories = largs.remove().split(",");
                            logger.info("Skip Directories : " + skipDirectories);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid switch!");
                    }
                }
            }
        }
        root = fileSystem.getPath(pathToWatch);
    }

    void startWatching() throws InterruptedException {
        Thread t = factory.createThread(this);
        t.start();
        // won't return
        t.join();
    }

    Path getRoot() {
        return root;
    }

    String getCommandToExecute() {
        return commandToExecute;
    }

    Logger getLogger() {
        return logger;
    }

    WatchService getWatcher() {
        return watcher;
    }

    FileSystem getFileSystem() {
        return fileSystem;
    }

    Factory getFactory() {
        return factory;
    }
}
