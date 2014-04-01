package tools;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;
import org.junit.*;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.any;

public class FileWatcherTest {

    // unit to be tested
    FileWatcher instance;
    // mock objects
    @SuppressWarnings("NonConstantLogger")
    Logger logger;
    Factory factory;
    Path root;
    FileSystem fileSystem;
    Thread thread;
    WatchService watcher;
    Registry registry;

    @Before
    public void setUp() throws IOException {
        logger = mock(Logger.class);
        factory = mock(Factory.class);
        fileSystem = mock(FileSystem.class);
        thread = mock(Thread.class);
        watcher = mock(WatchService.class);
        registry = mock(Registry.class);

        root = mock(Path.class);

        when(factory.createLogger(any(Class.class))).thenReturn(logger);
        when(fileSystem.newWatchService()).thenReturn(watcher);
        when(factory.getFileSystem()).thenReturn(fileSystem);
        when(factory.createThread(any(Runnable.class))).thenReturn(thread);
        when(factory.createRegistry(watcher)).thenReturn(registry);
        doNothing().when(factory).execute(any(String.class), any(Path.class),
                any(Path.class));
        doNothing().when(factory).registerMBeans(registry);

        instance = new FileWatcher(factory);
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
    public void testGetRootShouldReturnNullAfterCreated() {
        assertNull(instance.getRoot());
    }

    @Test
    public void testGetCommandToExecuteShouldReturnNullAfterCreated() {
        assertNull(instance.getCommandToExecute());
    }

    @Test
    public void testGetWatcherShouldReturnTheWatcher() {
        assertEquals(watcher, instance.getWatcher());
    }

    @Test
    public void testRunShouldRegisterWatchers() {
        when(registry.registerWatchers(null)).thenReturn(false);
        instance.run();
        verify(registry).registerWatchers(null);
    }

    @Test
    public void testRunShouldCallTakeFromWatchSevice() throws InterruptedException {
        when(registry.registerWatchers(null)).thenReturn(true);
        // TODO : exception stops the endless loop...
        when(watcher.take()).thenThrow(IOException.class);
        instance.run();
        verify(registry).registerWatchers(null);
        verify(watcher).take();
    }

    @Test
    public void testRunShouldCheckDirectoryExistsFalse() throws InterruptedException {
        WatchKey key = mock(WatchKey.class);
        List<WatchEvent<?>> emptyList = new ArrayList<>();
        when(key.pollEvents()).thenReturn(emptyList);
        when(registry.registerWatchers(null)).thenReturn(true);
        // TODO : exception stops the endless loop.
        when(watcher.take()).thenReturn(key).thenThrow(InterruptedException.class);
        when(registry.checkHasADirectory(key)).thenReturn(false);
        FileWatcher spy = spy(instance);
        doNothing().when(spy).handleEvents(key);
        doNothing().when(spy).cleanUp(key);

        spy.run();

        verify(watcher, times(2)).take();
        verify(spy, never()).handleEvents(key);
        verify(spy, never()).cleanUp(key);
    }

    @Test
    public void testRunShouldCheckDirectoryExistsTrue() throws InterruptedException {
        WatchKey key = mock(WatchKey.class);
        List<WatchEvent<?>> emptyList = new ArrayList<>();
        when(key.pollEvents()).thenReturn(emptyList);
        when(registry.registerWatchers(null)).thenReturn(true);
        // TODO : exception stops the endless loop...
        when(watcher.take()).thenReturn(key).thenThrow(InterruptedException.class);;
        when(registry.checkHasADirectory(key)).thenReturn(true);
        FileWatcher spy = spy(instance);
        doNothing().when(spy).handleEvents(key);
        doNothing().when(spy).cleanUp(key);

        spy.run();

        verify(watcher, times(2)).take();
        verify(spy).handleEvents(key);
        verify(spy).cleanUp(key);
    }

    @Test
    public void testCleanUpShouldResetTheKey() {
        WatchKey key = mock(WatchKey.class);
        when(key.reset()).thenReturn(true);
        doNothing().when(registry).remove(any(WatchKey.class));

        instance.cleanUp(key);

        verify(registry, never()).remove(any(WatchKey.class));
        verify(key).reset();
    }

    @Test(expected = IllegalStateException.class)
    public void testCleanUpShouldThrowAnExceptionIfNoEntries() {
        WatchKey key = mock(WatchKey.class);
        when(key.reset()).thenReturn(false);
        doNothing().when(registry).remove(any(WatchKey.class));
        Set<WatchKey> emptySet = new HashSet<>();
        when(registry.getKeys()).thenReturn(emptySet);

        instance.cleanUp(key);
    }

    @Test
    public void testSetupByCommandLineArguments() {
        String[] args = {"testDir"};
        Path path = mock(Path.class);
        when(fileSystem.getPath("testDir")).thenReturn(path);

        instance.setupByCommandLineArguments(args);

        assertEquals(path, instance.getRoot());
    }

    @Test
    public void testSetupByCommandLineArgumentsEmpty() {
        String[] args = {};
        Path path = mock(Path.class);
        when(fileSystem.getPath(".")).thenReturn(path);

        instance.setupByCommandLineArguments(args);

        assertEquals(path, instance.getRoot());
    }

    @Test
    public void testSetupByCommandLineArgumentsShouldParseArgs() {
        String[] args = {"-d", "testDir", "-c", "command"};

        Path path = mock(Path.class);
        when(fileSystem.getPath("testDir")).thenReturn(path);

        instance.setupByCommandLineArguments(args);

        assertEquals(path, instance.getRoot());
        assertEquals("command", instance.getCommandToExecute());
    }

    @Test
    public void testSetupByCommandLineArgumentsShouldParseSingleArgs() {
        String[] args = {"-c", "command"};

        Path path = mock(Path.class);
        when(fileSystem.getPath(".")).thenReturn(path);

        instance.setupByCommandLineArguments(args);

        assertEquals(path, instance.getRoot());
        assertEquals("command", instance.getCommandToExecute());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetupByCommandLineArgumentsShouldThrowExceptionIfInvalid() {
        String[] args = {"-d", "testDir", "-c_invalid"};

        Path path = mock(Path.class);
        when(fileSystem.getPath("testDir")).thenReturn(path);

        instance.setupByCommandLineArguments(args);
    }

    @Test
    public void testStartWatchingShouldStartABackgroundThread() throws InterruptedException {

        instance.startWatching();

        verify(factory).createThread(instance);
        verify(thread).start();
        // TODO final method can not be stubbed
        //verify(thread).join();
    }

    @Test
    public void testHandleEventsShouldCallPollEvents() {
        WatchKey key = mock(WatchKey.class);
        List<WatchEvent<?>> emptyList = new ArrayList<>();
        doReturn(emptyList).when(key).pollEvents();

        instance.handleEvents(key);

        verify(key).pollEvents();
    }

    @Test
    public void testHandleEventsShouldSkipOverflowEvent() {
        WatchKey key = mock(WatchKey.class);
        List<WatchEvent<?>> eventList = new ArrayList<>();
        WatchEvent<Object> we = mock(WatchEvent.class);
        when(we.kind()).thenReturn(OVERFLOW);
        eventList.add(we);
        doReturn(eventList).when(key).pollEvents();
        Path dir = mock(Path.class);
        when(registry.get(key)).thenReturn(dir);

        instance.handleEvents(key);

        verify(dir, never()).resolve(any(Path.class));
    }

    @Test
    public void testHandleEventsShouldRegisterWatchersOnADirectoryCreateEvent() {
        WatchKey key = mock(WatchKey.class);
        List<WatchEvent<?>> eventList = new ArrayList<>();
        WatchEvent<Path> we = mock(WatchEvent.class);
        when(we.kind()).thenReturn(ENTRY_CREATE);
        eventList.add(we);
        doReturn(eventList).when(key).pollEvents();
        Path dir = mock(Path.class);
        when(registry.get(key)).thenReturn(dir);
        when(dir.resolve(any(Path.class))).thenReturn(dir);
        when(factory.isDirectory(dir)).thenReturn(true);
        when(registry.registerWatchers(dir)).thenReturn(true);

        instance.handleEvents(key);

        verify(dir).resolve(any(Path.class));
        verify(factory).isDirectory(dir);
        verify(registry).registerWatchers(dir);
    }

    @Test
    public void testHandleEventsShouldExecuteCommandOnModify() throws IOException {
        WatchKey key = mock(WatchKey.class);
        List<WatchEvent<?>> eventList = new ArrayList<>();
        WatchEvent<Path> we = mock(WatchEvent.class);
        when(we.kind()).thenReturn(ENTRY_MODIFY);
        eventList.add(we);
        doReturn(eventList).when(key).pollEvents();
        Path dir = mock(Path.class);
        when(registry.get(key)).thenReturn(dir);
        when(dir.resolve(any(Path.class))).thenReturn(dir);
        when(fileSystem.getPath(eq("."))).thenReturn(dir);

        String[] args = {"-c", "command"};

        instance.setupByCommandLineArguments(args);
        instance.handleEvents(key);

        verify(factory).execute(eq("command"), eq(dir), eq(dir));
    }
}
