package sn.fun.textedit;

import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileSystemUtils;
import sn.fun.textedit.algos.FileIndexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class SpringConfig {
    @Bean
    Tika tika() {
        return new Tika();
    }

    @Bean
    Directory index(@Value("${FileIndexer.indexPath}") String indexPath) throws IOException {
        Path path = Paths.get(indexPath);
        FileSystemUtils.deleteRecursively(path);
        path = Files.createDirectories(path);
        return new MMapDirectory(path);
    }

    @Bean
    StopwordAnalyzerBase analyzer() {
        return new StandardAnalyzer();
    }
}
