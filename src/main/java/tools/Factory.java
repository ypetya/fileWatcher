package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import tools.jmx.JmxMonitorRegistry;

public class Factory {

    Factory() {
    }

    Thread createThread(Runnable runnable) {
        return new Thread(runnable);
    }

    FileSystem getFileSystem() {
        return FileSystems.getDefault();
    }

    public Logger createLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    Registry createRegistry(WatchService watcher) {
        return new Registry(this, watcher, new HashMap<WatchKey, Path>());
    }

    void walkFileTree(Path rootAll, FileVisitor visitor) throws IOException {
        Files.walkFileTree(rootAll, visitor);
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    boolean isDirectory(Path child) {
        return Files.isDirectory(child, NOFOLLOW_LINKS);
    }

    private String getExtension(Path path) {
        String extension = "";
        String pathStr = path.toFile().getName();
        int index = pathStr.lastIndexOf(".");
        if (index > -1 && index < pathStr.length() - 1) {
            extension = pathStr.substring(index + 1);
        } else {
            extension += pathStr;
        }
        return extension;
    }

    private List<String> getEnvironmentVars(Path changedFile) throws IOException {
        List<String> arr = new ArrayList<>();

        arr.add("WATCHED_DIR=" + changedFile.getParent().toFile().getCanonicalPath());
        arr.add("WATCHED_FILE=" + changedFile.getFileName().toString());
        arr.add("WATCHED_EXTENSION=" + getExtension(changedFile));
        for (Entry e : System.getProperties().entrySet()) {
            arr.add((String) e.getKey() + "=" + (String) e.getValue());
        }
        return arr;
    }

    void execute(String command, Path watchRootDirectory, Path changedFile) throws IOException {
        Process pr = Runtime.getRuntime().exec(command,
                getEnvironmentVars(changedFile).toArray(new String[]{}),
                watchRootDirectory.toFile());
        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = null;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
    }

    private JmxMonitorRegistry jmx;

    void registerMBeans(Registry registry) {
        jmx = new JmxMonitorRegistry(this, registry);
        jmx.register();
    }
}
