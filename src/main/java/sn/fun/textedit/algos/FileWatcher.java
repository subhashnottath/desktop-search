package sn.fun.textedit.algos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FileWatcher {
    private final FileIndexer fileIndexer;
    private final String[] filePaths;

    public FileWatcher(FileIndexer fileIndexer,
                       @Value("${FileWatcher.filePaths}") String[] filePaths) throws IOException {
        this.fileIndexer = fileIndexer;
        this.filePaths = filePaths;
        for (String dirPath : filePaths) {
            analyzeFiles(dirPath);
        }
    }

    void analyzeFiles(String dirPath) throws IOException {
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) {
            log.error("Invalid dictionary base path, exiting {}", dirPath);
            throw new IOException("Invalid dictionary base path");
        }
        List<Path> batch = new ArrayList<>();
        Files.walk(dir)
                .filter(Files::isRegularFile)
                .forEach((filePath) -> {
                    batch.add(filePath);
                    if (batch.size() >= 100) {
                        fileIndexer.indexBatch(batch);
                        batch.clear();
                    }
                });
        fileIndexer.indexBatch(batch);
    }

}
