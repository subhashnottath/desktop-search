package sn.fun.textedit.data;

import lombok.Data;

import java.nio.file.Path;

@Data
public class FileDoc {
    public static final int CREATE = 1;
    public static final int DELETE = 2;
    public static final int UPDATE = 3;

    private final Path path;
    private final int op;
}
