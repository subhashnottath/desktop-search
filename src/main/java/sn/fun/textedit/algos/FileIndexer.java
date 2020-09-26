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
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sn.fun.textedit.data.FileDoc;
import sn.fun.textedit.data.FileSummary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class FileIndexer {
    private final Directory index;
    private final FileAnalyser fileAnalyser;
    private final StopwordAnalyzerBase analyzer;
    private final Map<String, Long> indexedFilesMap;
    private final Path indexPath;
    private final ObjectMapper mapper;

    public FileIndexer(Directory index, FileAnalyser fileAnalyser, StopwordAnalyzerBase analyzer, ObjectMapper mapper,
                       @Value("${FileIndexer.indexedFiles}") String indexedFiles) throws IOException {
        this.fileAnalyser = fileAnalyser;
        this.index = index;
        this.analyzer = analyzer;
        this.indexPath = Paths.get(indexedFiles);
        String content = "{}";
        if (Files.exists(indexPath)) {
            content = Files.readString(indexPath, StandardCharsets.UTF_8);
        }
        indexedFilesMap = mapper.readValue(content, new TypeReference<>() {});
        this.mapper = mapper;
    }

    public void cleanDeleted(Set<String> currentFileSet) {
        List<String> deleted = new ArrayList<>();
        for (String file: indexedFilesMap.keySet()) {
            if (!currentFileSet.contains(file)) {
                deleted.add(file);
            }
        }
        for (String file: deleted) {
            indexedFilesMap.remove(file);
            log.info("Removing deleted file {} from index", file);
        }
    }

    public void indexBatch(List<FileDoc> batch) {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter indexWriter = new IndexWriter(index, config)) {
            for (FileDoc fileDoc : batch) {
                Path filePath = fileDoc.getPath();
                String filePathStr = filePath.toString();
                if (fileDoc.getOp() == FileDoc.DELETE) {
                    indexWriter.deleteDocuments(new Term("id", filePathStr));
                    indexedFilesMap.remove(filePathStr);
                } else {
                    long modifiedOn = Files.getLastModifiedTime(filePath).toMillis();
                    if (indexedFilesMap.containsKey(filePathStr)) {
                        long oldModifiedOn = indexedFilesMap.get(filePathStr);
                        if (modifiedOn == oldModifiedOn) {
                            continue;
                        }
                        fileDoc = new FileDoc(fileDoc.getPath(), FileDoc.UPDATE);
                    }
                    indexedFilesMap.put(filePathStr, modifiedOn);
                    FileSummary fileSummary = fileAnalyser.analyse(filePath);
                    if (fileDoc.getOp() == FileDoc.UPDATE) {
                        update(indexWriter, fileSummary);
                    } else {
                        add(indexWriter, fileSummary);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error updating index", e);
        } finally {
            done();
        }
    }

    public void done() {
        try {
            String res = mapper.writeValueAsString(indexedFilesMap);
            Files.writeString(indexPath, res);
        } catch (IOException e) {
            log.error("Error writing indexFile", e);
        }
    }

    private void add(IndexWriter indexWriter, FileSummary fileSummary) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", fileSummary.getFile(), Field.Store.YES));
        doc.add(new TextField("file", fileSummary.getFile(), Field.Store.YES));
        doc.add(new StringField("length", fileSummary.getContentLength(), Field.Store.YES));
        doc.add(new TextField("type", fileSummary.getContentType(), Field.Store.YES));
        doc.add(new TextField("summary", fileSummary.getSummary(), Field.Store.YES));
        doc.add(new TextField("content", fileSummary.getContent(), Field.Store.NO));
        indexWriter.addDocument(doc);
    }

    private void update(IndexWriter indexWriter, FileSummary fileSummary) throws IOException {
        Document doc = new Document();
        Term term = new Term("id", fileSummary.getFile());
        doc.add(new StringField("id", fileSummary.getFile(), Field.Store.YES));
        doc.add(new TextField("file", fileSummary.getFile(), Field.Store.YES));
        doc.add(new StringField("length", fileSummary.getContentLength(), Field.Store.YES));
        doc.add(new TextField("type", fileSummary.getContentType(), Field.Store.YES));
        doc.add(new TextField("summary", fileSummary.getSummary(), Field.Store.YES));
        doc.add(new TextField("content", fileSummary.getContent(), Field.Store.NO));
        indexWriter.updateDocument(term, doc);
    }
}
