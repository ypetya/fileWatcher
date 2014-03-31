package tools;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileWatcher implements Runnable {

    private Path root;
    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final Factory factory;
    private final FileSystem fileSystem;

    FileWatcher(Factory factory) throws IOException {
        this.factory = factory;
        logger = factory.createLogger(this.getClass());
        fileSystem = factory.getFileSystem();
        watcher = fileSystem.newWatchService();
        keys = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void run() {
        if (registerWatchers(root)) {
            try {
                while (true) {
                    WatchKey key = watcher.take();

                    if (checkWatchKeyIsADirectory(key)) {
                        handleEvents(key);
                        cleanUp(key);
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Exiting watch loop.", ex);
            }
        }
    }

    boolean checkWatchKeyIsADirectory(WatchKey key) {
        Path dir = keys.get(key);
        if (dir == null) {
            logger.severe(String.format("Watchkey not found! %s", key));
            return false;
        }
        return true;
    }

    void cleanUp(WatchKey key) {
        // reset key and remove from set if directory no longer accessible
        if (!key.reset()) {
            keys.remove(key);

            // 
            if (keys.isEmpty()) {
                throw new RuntimeException("All directories are inaccessible");
            }
        }
    }

    void setupByCommandLineArguments(String[] args) {
        String pathToWatch = ".";
        if (args != null && args.length > 0) {
            pathToWatch = args[0];
        }
        root = fileSystem.getPath(pathToWatch);
    }

    void startWatching() throws InterruptedException {
        Thread t = factory.createThread(this);
        t.start();
        // won't return
        t.join();
    }

    private void register(Path rootAll) throws IOException {
        Files.walkFileTree(rootAll, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                keys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    boolean registerWatchers(Path subDir) {
        try {
            logger.info(String.format("Start watching %s", subDir.toString()));
            register(subDir);
            return true;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Exception at registering directory watchers.", ex);
        }
        return false;
    }

    void handleEvents(WatchKey key) {
        Path dir = keys.get(key);
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind kind = event.kind();

            // TBD - provide example of how OVERFLOW event is handled
            if (kind == OVERFLOW) {
                continue;
            }

            // Context for directory entry event is the file name of entry
            WatchEvent<Path> ev = cast(event);
            Path name = ev.context();
            Path child = dir.resolve(name);

            logger.info(String.format("%s: %s\n", event.kind().name(), child));

            // if directory is created, and watching recursively, then
            // register it and its sub-directories
            if (kind == ENTRY_CREATE) {
                if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                    registerWatchers(child);
                }
            }
        }
    }

    Path getRoot() {
        return root;
    }

    WatchService getWatcher() {
        return watcher;
    }

    Map<WatchKey, Path> getKeys() {
        return keys;
    }

    FileSystem getFileSystem() {
        return fileSystem;
    }
    
    Factory getFactory() {
        return factory;
    }

    Logger getLogger() {
        return logger;
    }
}
