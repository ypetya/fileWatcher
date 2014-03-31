package tools;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.logging.Logger;

public class Factory {
    
    Factory(){
    }

    Thread createThread(Runnable runnable) {
        return new Thread(runnable);
    }

    FileSystem getFileSystem() {
        return FileSystems.getDefault();
    }
    
    Logger createLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}
