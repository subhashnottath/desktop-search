package sn.fun.textedit.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.lucene.document.Document;
import org.apache.tika.metadata.Metadata;

@Data
public class FileSummary {
    private final String file;
    private final String contentType;
    private final long contentLength;
    @JsonIgnore
    private final String content;
    private final String summary;

    public FileSummary(String filePath, Metadata metadata, String content) {
        file = filePath;
        contentType = metadata.get("Content-Type");
        long contentLength = -1;
        try {
            contentLength = Long.parseLong(metadata.get("Content-Length"));
        } catch (NumberFormatException ignored) {}
        this.contentLength = contentLength;
        this.content = content;
        if (content.length() > 0) {
            summary = summary(content);
        } else {
            summary = "binary file";
        }
    }

    public FileSummary(String filePath) {
        file = filePath;
        contentType = "Unknown";
        contentLength = -1;
        content = "";
        summary = "Unknown";
    }

    public FileSummary(Document document) {
        file = document.get("file");
        contentType = document.get("type");
        long contentLength = -1;
        try {
            contentLength = document.getField("length").numericValue().longValue();
        } catch (NumberFormatException ignored) {}
        this.contentLength = contentLength;
        content = document.get("content");
        summary = document.get("summary");
    }

    String summary(String s) {
        StringBuilder sb = new StringBuilder();
        int len = 80;
        int pos = 0;
        int next = s.indexOf("\n");
        while (sb.length() <= 240 && next >= 0) {
            String substr = s.substring(pos, next).trim();
            if (substr.length() > 0) {
                sb.append(substr);
                sb.append("  ");
                if (sb.length() >= len) {
                    sb.append("\n");
                    len = sb.length() + 81;
                }
            }
            pos = next;
            next = s.indexOf("\n", pos + 1);
        }
        return sb.toString();
    }

}
