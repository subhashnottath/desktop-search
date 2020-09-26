package sn.fun.textedit.algos;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sn.fun.textedit.data.FileSummary;
import sn.fun.textedit.data.SearchQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SearchService {
    private final Directory index;
    private final StopwordAnalyzerBase analyzer;
    private final int maxResultCount;

    public SearchService(Directory index, StopwordAnalyzerBase analyzer,
                         @Value("${FileIndexer.maxResultCount}") int maxResultCount) {
        this.index = index;
        this.analyzer = analyzer;
        this.maxResultCount = maxResultCount;
    }

    public List<FileSummary> search(SearchQuery query) {
        List<FileSummary> result = new ArrayList<>();
        try (IndexReader reader = DirectoryReader.open(index)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query q = new QueryParser("file", analyzer).parse(query.getQuery());
            TopDocs docs = searcher.search(q, maxResultCount);
            ScoreDoc[] searchDocs = docs.scoreDocs;
            for (ScoreDoc scoreDoc : searchDocs) {
                int docId = scoreDoc.doc;
                Document d = searcher.doc(docId);
                result.add(new FileSummary(d));
            }
            log.info("Search results : {}", result);
        } catch (IOException | ParseException e) {
            log.error("Error processing query", e);
        }
        return result;
    }
}
