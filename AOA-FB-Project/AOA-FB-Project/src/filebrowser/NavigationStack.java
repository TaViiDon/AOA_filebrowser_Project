package filebrowser;

import java.io.File;
import java.util.Stack;

public class NavigationStack {
	
	 private final Stack<File> backStack;
	 private final Stack<File> forwardStack;
	 
	 public NavigationStack() {
	        backStack = new Stack<>();
	        forwardStack = new Stack<>();
	    }
	 
	// Push current directory to back stack and clear forward stack
	public void pushBack(File directory) {
		backStack.push(directory);
		// Clear forward history whenever a new path is added to back
		forwardStack.clear();  
	}

	// Move back to the previous directory
	public File goBack(File currentDirectory) {
		if (!backStack.isEmpty()) {
			// Save current directory to forward stack
			forwardStack.push(currentDirectory);  
			// Move back in history
			return backStack.pop();              
		}
		// No back history available
		return null; 
	}

	// Move forward to the next directory
	public File goForward(File currentDirectory) {
		if (!forwardStack.isEmpty()) {
			// Save current directory to back stack

			backStack.push(currentDirectory);  
			// Move forward in history
			return forwardStack.pop();           
		}
		// No forward history available
		return null;  
	}

	// Check if back navigation is possible
	public boolean canGoBack() {
		return !backStack.isEmpty();
	}

	// Check if forward navigation is possible
	public boolean canGoForward() {
		return !forwardStack.isEmpty();
	}
}