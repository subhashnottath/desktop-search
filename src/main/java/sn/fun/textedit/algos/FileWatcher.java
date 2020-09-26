package sn.fun.textedit.algos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sn.fun.textedit.data.FileDoc;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class FileWatcher {
    private final FileIndexer fileIndexer;
    private final WatchService watchService;

    public FileWatcher(FileIndexer fileIndexer,
                       @Value("${FileWatcher.filePaths}") String[] filePaths) throws IOException {
        this.fileIndexer = fileIndexer;
        this.watchService = FileSystems.getDefault().newWatchService();
        Set<String> currentFileSet = new HashSet<>();
        for (String dirPath : filePaths) {
            processDir(currentFileSet, dirPath);
        }
        fileIndexer.cleanDeleted(currentFileSet);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::watcher);
    }

    void watcher() {
        WatchKey key;
        while (true) {
            try {
                if ((key = watchService.take()) == null) break;
                processEvents(key);
                key.reset();
            } catch (InterruptedException e) {
                log.error("Error watching file system", e);
            }
        }
    }

    void processEvents(WatchKey key) {
        Path dir = (Path) key.watchable();
        List<FileDoc> batch = new ArrayList<>();
        for (WatchEvent<?> event : key.pollEvents()) {
            log.info("Event {}.", event);
            Path path = dir.resolve((Path) event.context());
            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                FileDoc doc = new FileDoc(path, FileDoc.CREATE);
                batch.add(doc);
            } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                FileDoc doc = new FileDoc(path, FileDoc.UPDATE);
                batch.add(doc);
            } else {
                FileDoc doc = new FileDoc(path, FileDoc.DELETE);
                batch.add(doc);
            }
        }
        fileIndexer.indexBatch(batch);
    }

    void processDir(Set<String> currentFileSet, String dirPath) throws IOException {
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) {
            log.error("Invalid dictionary base path, exiting {}", dirPath);
            throw new IOException("Invalid dictionary base path");
        }
        List<FileDoc> batch = new ArrayList<>();
        Files.walk(dir)
                .forEach((filePath) -> {
                    if (Files.isDirectory(filePath)) {
                        try {
                            filePath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                        } catch (IOException e) {
                            log.error("Error registering dir to watcher", e);
                        }
                    } else {
                        currentFileSet.add(filePath.toString());
                        FileDoc doc = new FileDoc(filePath, FileDoc.CREATE);
                        batch.add(doc);
                        if (batch.size() >= 100) {
                            fileIndexer.indexBatch(batch);
                            batch.clear();
                        }
                    }
                });
        fileIndexer.indexBatch(batch);
    }

}
