package tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class registers the WatchKeys and the containing parent directory path
 * in a map. Can be instantiated via the constructor or the Factory. Registry
 * uses the factory object to walk the FileTree and create the logger.
 */
public class Registry extends SimpleFileVisitor<Path> {

    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final Factory factory;
    private String[] skipDirectories;

    public Registry(Factory factory, WatchService watcher, Map<WatchKey, Path> keys) {
        this.factory = factory;
        logger = factory.createLogger(this.getClass());
        this.watcher = watcher;
        this.keys = keys;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (skipDirectory(dir)) {
            return FileVisitResult.SKIP_SIBLINGS;
        }
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
        return FileVisitResult.CONTINUE;
    }
    
    public Set<WatchKey> getKeys() {
        return keys.keySet();
    }

    public String[] getSkipDirectories() {
        return skipDirectories;
    }

    public void setSkipDirectories(String[] skipDirectories) {
        this.skipDirectories = skipDirectories;
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

    boolean checkHasADirectory(WatchKey key) {
        Path dir = keys.get(key);
        if (dir == null) {
            logger.severe(String.format("Watchkey not found! %s", key));
            return false;
        }
        return true;
    }

    Path get(WatchKey key) {
        return keys.get(key);
    }

    void remove(WatchKey key) {
        keys.remove(key);
    }

    private void register(Path rootAll) throws IOException {
        factory.walkFileTree(rootAll, this);
    }

    Logger getLogger() {
        return logger;
    }

    WatchService getWatcher() {
        return watcher;
    }

    boolean skipDirectory(Path dir) {
        if (skipDirectories != null && skipDirectories.length > 0) {
            File f = dir.toFile();
            if (f.isDirectory()) {
                for (String skipDir : skipDirectories) {
                    if (skipDir.equals(f.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
