package filebrowser;

import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        	FileBrowserWindow filebrowser = new FileBrowserWindow();
        	filebrowser.setVisible(true);
        });
    }

}









/*public class Main {

	public static void main(String[] args) {
        SwingUtilities.invokeLater(FileBrowserWindow::new);
    }

}
*/