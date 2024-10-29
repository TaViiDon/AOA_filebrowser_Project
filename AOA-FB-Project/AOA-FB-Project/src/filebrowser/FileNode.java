package filebrowser;

import java.io.File;

// FileNode class to wrap File object for tree node

public class FileNode {
	
    private final File file;

    public FileNode(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return file.getName().isEmpty() ? file.getAbsolutePath() : file.getName();
    }
}