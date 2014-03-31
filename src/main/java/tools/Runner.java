package tools;

import java.io.IOException;

public class Runner {
    static final Factory factory = new Factory();
    public static void main(String[] args) throws IOException, InterruptedException{
        FileWatcher fw = new FileWatcher(factory);
        fw.setupByCommandLineArguments(args);
        fw.startWatching();
    }
}
