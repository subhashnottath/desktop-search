package sn.fun.textedit.algos;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Component;
import sn.fun.textedit.data.FileSummary;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileAnalyser {
    private final Tika tika;

    FileSummary analyse(Path filePath) {
        FileSummary fileSummary;
        try {
            String content = tika.parseToString(filePath);
            Metadata metadata = new Metadata();
            tika.parse(filePath, metadata);
            fileSummary = new FileSummary(filePath.toString(), metadata, content);
            log.info("Summary  : {}", fileSummary);
        } catch (Throwable e) {
            log.error("Error processing file {}", filePath, e);
            fileSummary = new FileSummary(filePath.toString());
        }
        return fileSummary;
    }
}
