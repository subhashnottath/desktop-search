package sn.fun.textedit.algos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sn.fun.textedit.data.FileSummary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FileIndexer {
    private final Directory index;
    private final FileAnalyser fileAnalyser;
    private final StopwordAnalyzerBase analyzer;
    private final Map<String, Long> indexedFilesMap;

    public FileIndexer(Directory index, FileAnalyser fileAnalyser, StopwordAnalyzerBase analyzer, ObjectMapper mapper,
                       @Value("${FileIndexer.indexedFiles}") String indexedFiles) throws IOException {
        this.fileAnalyser = fileAnalyser;
        this.index = index;
        this.analyzer = analyzer;
        Path indexPath = Paths.get(indexedFiles);
        String content = "{}";
        if (Files.exists(indexPath)) {
            content = Files.readString(indexPath, StandardCharsets.UTF_8);
        }
        indexedFilesMap = mapper.readValue(content, new TypeReference<>() {});
    }

    public void indexBatch(List<Path> batch) {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter indexWriter = new IndexWriter(index, config)) {
            for (Path filePath : batch) {
                FileSummary fileSummary = fileAnalyser.analyse(filePath);
                add(indexWriter, fileSummary);
            }
        } catch (IOException e) {
            log.error("Error updating index", e);
        }
    }

    private void add(IndexWriter indexWriter, FileSummary fileSummary) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("file", fileSummary.getFile(), Field.Store.YES));
        doc.add(new StringField("length", fileSummary.getContentLength(), Field.Store.YES));
        doc.add(new TextField("type", fileSummary.getContentType(), Field.Store.YES));
        doc.add(new TextField("summary", fileSummary.getSummary(), Field.Store.YES));
        doc.add(new TextField("content", fileSummary.getContent(), Field.Store.NO));
        indexWriter.addDocument(doc);
    }
}
