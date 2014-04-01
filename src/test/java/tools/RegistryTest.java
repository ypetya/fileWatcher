package tools;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RegistryTest {

    Registry instance;
    // mock objects
    @SuppressWarnings("NonConstantLogger")
    Logger logger;
    Factory factory;

    WatchService watcher;
    HashMap<WatchKey, Path> keys;

    @Before
    public void setUp() throws IOException {
        logger = mock(Logger.class);
        factory = mock(Factory.class);
        watcher = mock(WatchService.class);
        keys = new HashMap<>();

        when(factory.createLogger(any(Class.class))).thenReturn(logger);
        doNothing().when(factory).walkFileTree(any(Path.class), any(SimpleFileVisitor.class));

        instance = new Registry(factory, watcher, keys);
    }

    @Test
    public void testGetLoggerShouldReturnTheLogger() {
        assertEquals(logger, instance.getLogger());
    }

    @Test
    public void testGetKeysShouldReturnSet() {
        Object o = instance.getKeys();
        assertNotNull(o);
        assertTrue(o instanceof Set);
    }

    @Test
    public void testGetWatcherShouldReturnTheWatcher() {
        assertEquals(watcher, instance.getWatcher());
    }

    @Test
    public void testRegisterWatchersShouldReturnTrue() throws IOException {
        Path path = mock(Path.class);
        assertTrue(instance.registerWatchers(path));
        verify(factory).walkFileTree(eq(path), any(FileVisitor.class));
    }

    @Test
    public void testRegisterWatchersShouldReturnFalseOnError() throws IOException {
        Path path = mock(Path.class);
        doThrow(IOException.class).when(factory).walkFileTree(eq(path), any(FileVisitor.class));
        assertFalse(instance.registerWatchers(path));
    }

    @Test
    public void testCheckHasADirectoryShouldReturnFalseIfNotExists() {
        WatchKey key = mock(WatchKey.class);
        assertFalse(instance.checkHasADirectory(key));
    }

    @Test
    public void testCheckHasADirectoryShouldReturnTrueIfExists() {
        WatchKey key = mock(WatchKey.class);
        keys.put(key, mock(Path.class));
        assertTrue(instance.checkHasADirectory(key));
    }

    @Test
    public void testGet() {
        WatchKey key = mock(WatchKey.class);
        Path value = mock(Path.class);
        keys.put(key, value);
        assertEquals(value, instance.get(key));
    }

    @Test
    public void testRemove() {
        WatchKey key = mock(WatchKey.class);
        keys.put(key, mock(Path.class));
        instance.remove(key);
        assertNull(keys.get(key));
    }

    @Test
    public void testPreVisitDirectoryShouldRegisterWatcher() throws IOException {
        Path dir = mock(Path.class);
        WatchKey key = mock(WatchKey.class);
        when(dir.register(any(WatchService.class), (WatchEvent.Kind) anyVararg())).thenReturn(key);
        FileVisitResult ret = instance.preVisitDirectory(dir, null);
        verify(dir).register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        assertNotNull(keys.get(key));
        assertEquals(ret, FileVisitResult.CONTINUE);
    }
}
