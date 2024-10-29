package filebrowser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class FileBrowserWindow extends JFrame {
	
	private static final long serialVersionUID = 1L;
    private final NavigationStack navigationStack;
    private final JTextArea fileMetadataArea;
    private JList<File> fileList;
    private final DefaultListModel<File> fileListModel;
    private final JButton backButton;
    private final JButton forwardButton;
    private final JTextField searchField;
    private File currentDirectory;
    
    //private JTree fileTree;
   // private JTextArea fileDetails;
   // private JLabel filePreview;
    
    public FileBrowserWindow() {
        navigationStack = new NavigationStack();
        setTitle("File Browser");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize components
        backButton = new JButton("Back <--- ");
        forwardButton = new JButton("Forward ---> ");
        fileMetadataArea = new JTextArea();
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setLayoutOrientation(JList.VERTICAL);

        searchField = new JTextField(20);
        searchField.addActionListener(e -> {
            String query = searchField.getText(); // Get the text from the search field
            String resultMessage = searchFiles(query); // Call the searchFiles method with the query

            // Display the result message to the user
            JOptionPane.showMessageDialog(this, resultMessage, "Search Result", JOptionPane.INFORMATION_MESSAGE);
        });


        // Disable buttons initially
        backButton.setEnabled(false);
        forwardButton.setEnabled(false);

        // Layout setup
        JPanel navigationPanel = new JPanel();
        navigationPanel.add(backButton);
        navigationPanel.add(forwardButton);
        navigationPanel.add(searchField);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(fileList), new JScrollPane(fileMetadataArea));
        splitPane.setDividerLocation(300);

        add(navigationPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Action listeners
        backButton.addActionListener((ActionEvent e) -> {
            goBack();
        });

        forwardButton.addActionListener((ActionEvent e) -> {
            goForward();
        });

        // Double-click listener on the file list to open folders or preview files
        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    File selectedFile = fileList.getSelectedValue();
                    if (selectedFile != null) {
                        if (selectedFile.isDirectory()) {
                            navigateTo(selectedFile);
                        } 
                        else {
                            displayFilePreview(selectedFile);
                        }
                    }
                }
            }
        });

        // Start browsing in the home directory
        navigateTo(new File(System.getProperty("user.home")));
    }

    // -------------------------------------------------------------------------
    // simple search feature
    // -------------------------------------------------------------------------
    private String searchFiles(String query){
        // This should be able to search all the current folder we are at for a specific file
        // file can be broken down into a few structures:
        // - name, type, preview text, date created
        // enter key to perform search

        // Clear the current file list
        fileListModel.clear();
        
        // Get all files in the current directory
        File[] files = currentDirectory.listFiles();
        int matchCount = 0; // Counter for matching files

        if (files != null) {
            for (File file : files) {
                boolean matches = false;

                // Check if the file name contains the search query
                if (file.getName().toLowerCase().contains(query.toLowerCase())) {
                    matches = true;
                }

                // Check if the file type matches
                String fileType = getFileType(file); 
                if (fileType.toLowerCase().contains(query.toLowerCase())) {
                    matches = true;
                }

                // If the file is a text file, we can also check its preview
                // if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                //     String previewText = getFilePreviewText(file); // Implement this method to read the first few lines
                //     if (previewText.toLowerCase().contains(query.toLowerCase())) {
                //         matches = true;
                //     }
                // }

                // If it matches any criteria, add it to the list
                if (matches) {
                    fileListModel.addElement(file);
                    matchCount++; // Increment match count
                }
            }
        }

        // Return a message based on the search results
        if (matchCount == 0) {
            return "No files found matching \"" + query + "\".";
        } else {
            return matchCount + " file(s) found matching \"" + query + "\".";
        }
    }

    private void navigateTo(File directory) {
        if (currentDirectory != null) {
            navigationStack.pushBack(currentDirectory);
        }
        currentDirectory = directory;
        updateFileList(directory);
        updateNavigationButtons();
    }

    private void updateFileList(File directory) {
        fileListModel.clear();
        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files);
            for (File file : files) {
                fileListModel.addElement(file);
            }
        }
        displayFileMetadata(directory);
    }

    private void goBack() {
        File previousDirectory = navigationStack.goBack(currentDirectory);
        if (previousDirectory != null) {
            currentDirectory = previousDirectory;
            updateFileList(previousDirectory);
        }
        updateNavigationButtons();
    }

    private void goForward() {
        File nextDirectory = navigationStack.goForward(currentDirectory);
        if (nextDirectory != null) {
            currentDirectory = nextDirectory;
            updateFileList(nextDirectory);
        }
        updateNavigationButtons();
    }

    private void updateNavigationButtons() {
        backButton.setEnabled(navigationStack.canGoBack());
        forwardButton.setEnabled(navigationStack.canGoForward());
    }

    // -------------------------------------------------------------------------
    // Creating a hash map to hold a few file types and their coresponding magic numbers
    // -------------------------------------------------------------------------
    private static final Map<String, String> MAGIC_NUMBERS = new HashMap<>();
    static {
        // some basic photo file types
        MAGIC_NUMBERS.put("FFD8FF", "JPEG Image");
        MAGIC_NUMBERS.put("89504E47", "PNG Image");
        MAGIC_NUMBERS.put("47494638", "GIF Image");
        MAGIC_NUMBERS.put("25504446", "PDF Document");
        MAGIC_NUMBERS.put("424D", "BMP Image");
        MAGIC_NUMBERS.put("49492A00", "TIFF Image (little-endian)");
        MAGIC_NUMBERS.put("4D4D002A", "TIFF Image (big-endian)");
  
         // Audio and Video Types
        MAGIC_NUMBERS.put("66747970", "MP4"); // MP4/MOV (ftyp)
        MAGIC_NUMBERS.put("494433", "MP3"); // MP3 (ID3v2)
        MAGIC_NUMBERS.put("FFFB", "MP3"); // MP3 (MPEG Header)
        MAGIC_NUMBERS.put("FFF3", "MP3"); // MP3 (MPEG Header)
        MAGIC_NUMBERS.put("52494646", "WAV"); // WAV/AVI (RIFF)
        MAGIC_NUMBERS.put("41564920", "AVI"); // AVI
        MAGIC_NUMBERS.put("664C6143", "FLAC"); // FLAC (fLaC)
        MAGIC_NUMBERS.put("1A45DFA3", "MKV"); // MKV (EBML)
        MAGIC_NUMBERS.put("494433", "MP3 Audio");
        MAGIC_NUMBERS.put("52494646", "WAV Audio");

        // Basic document types
        MAGIC_NUMBERS.put("D0CF11E0", "Microsoft Office (DOC, XLS, PPT)"); // Compound File Binary Format
        MAGIC_NUMBERS.put("504B0304", "Microsoft Office/ZIP Archive (DOCX, XLSX, PPTX)"); // ZIP-based formats
        MAGIC_NUMBERS.put("0x00000000", "Open Document Format (ODT, ODS, ODP)"); // ODF files (general case)
    }

    // -------------------------------------------------------------------------
    // Creating a method that is able to detect the file type based on it's magic number
    // -------------------------------------------------------------------------
    private String getFileType(File file) {
        // Return "Directory" if the file is actually a directory
        if (file.isDirectory()) {
            return "Directory";
        }

        // Attempt to read the file's magic number
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = new byte[4]; // Read the first 4 bytes (can adjust for different formats)
            if (fis.read(bytes) != -1) {
                // Convert bytes to hex
                StringBuilder hex = new StringBuilder();
                for (byte b : bytes) {
                    hex.append(String.format("%02X", b));
                }

                // Check if the file's magic number matches any known file types
                for (Map.Entry<String, String> entry : MAGIC_NUMBERS.entrySet()) {
                    if (hex.toString().startsWith(entry.getKey())) {
                        return entry.getValue(); // Return the matched file type
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Unknown Type"; // Return this if no match was found
    }

    // -------------------------------------------------------------------------
    // Display metadata such as name, size, type, and last modified date
    // -------------------------------------------------------------------------
    private void displayFileMetadata(File file) {
        StringBuilder metadata = new StringBuilder();
        metadata.append("Name: ").append(file.getName()).append("\n");
        metadata.append("Size: ").append(file.length()).append(" bytes\n");
        metadata.append("Type: ").append(getFileType(file)).append("\n");
        metadata.append("Last Modified: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(file.lastModified())).append("\n");
        fileMetadataArea.setText(metadata.toString());
    }

    // -------------------------------------------------------------------------
    // Display file preview (for text files, show the first few lines)
    // -------------------------------------------------------------------------
    private void displayFilePreview(File file) {

        if (file.isFile() && file.getName().endsWith(".txt")) {
            try (FileReader fr = new FileReader(file); Scanner scanner = new Scanner(fr)) {
                StringBuilder preview = new StringBuilder();
                int lineCount = 0;
                while (scanner.hasNextLine() && lineCount < 10) {
                    preview.append(scanner.nextLine()).append("\n");
                    lineCount++;
                }
                fileMetadataArea.setText(preview.toString());
                
            } catch (IOException ex) {
                fileMetadataArea.setText("Unable to preview file.");
            }
        } else {
            fileMetadataArea.setText("No preview available for this file.");
        }
        displayFileMetadata(file);
    } 
}