package tools;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.logging.Logger;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileWatcherTest {

    // unit to be tested
    FileWatcher instance;
    // mock objects
    @SuppressWarnings("NonConstantLogger")
    Logger logger;
    Factory factory;
    FileSystem fileSystem;
    Thread thread;
    WatchService watcher;

    @Before
    public void setUp() throws IOException {
        logger = mock(Logger.class);
        factory = mock(Factory.class);
        fileSystem = mock(FileSystem.class);
        thread = mock(Thread.class);
        watcher = mock(WatchService.class);

        when(factory.createLogger(any(Class.class))).thenReturn(logger);
        when(fileSystem.newWatchService()).thenReturn(watcher);
        when(factory.getFileSystem()).thenReturn(fileSystem);
        when(factory.createThread(any(Runnable.class))).thenReturn(thread);

        instance = new FileWatcher(factory);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetFactoryShouldReturnTheFactory() {
        assertEquals(factory, instance.getFactory());
    }

    @Test
    public void testGetLoggerShouldReturnTheLogger() {
        assertEquals(logger, instance.getLogger());
    }

    @Test
    public void testGetFileSystemShouldReturnTheDefaultFs() {
        assertEquals(fileSystem, instance.getFileSystem());
    }

    @Test
    public void testGetWatcherShouldReturnTheWatcher() {
        assertEquals(watcher, instance.getWatcher());
    }
    
    @Test
    public void testGetRootShouldReturnNullAfterCreated() {
        assertNull(instance.getRoot());
    }
    
    @Test
    public void testGetKeysShouldReturnHashMap() {
        Object o = instance.getKeys();
        assertNotNull(o);
        assertEquals(HashMap.class, o.getClass() );
    }
}
