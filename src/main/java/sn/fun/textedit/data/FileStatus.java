package sn.fun.textedit.data;

import lombok.Data;

@Data
public class FileStatus {
    private final String filePath;
    private final long lastModified;
}
