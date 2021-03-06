package com.kartoflane.superluminal2.utils;

import java.io.File;

import net.vhati.modmanager.core.FTLUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.kartoflane.superluminal2.Superluminal;
import com.kartoflane.superluminal2.ui.LoadingDialog;

public class UIUtils {

	/**
	 * Displays a simple dialog to inform the user about an error.
	 * 
	 * @param parentShell
	 *            a shell which will be the parent of the dialog. May be null.
	 * @param title
	 *            the title of the dialog window, or null for default value:<br>
	 *            <code>APP_NAME - Error</code>
	 * @param message
	 *            the message that will be displayed to the user. Must not be null.
	 * 
	 */
	public static void showErrorDialog(Shell parentShell, String title, String message) {
		boolean dispose = false;

		if (title == null)
			title = Superluminal.APP_NAME + " - Error";
		if (message == null)
			throw new IllegalArgumentException("Message must not be null.");

		if (parentShell == null) {
			parentShell = new Shell(Display.getCurrent());
			dispose = true;
		}

		MessageBox box = new MessageBox(parentShell, SWT.ICON_ERROR | SWT.OK);

		box.setText(title);
		box.setMessage(message);

		box.open();

		if (dispose)
			parentShell.dispose();
	}

	/**
	 * Displays a simple dialog to warn the user about something.
	 * 
	 * @param parentShell
	 *            a shell which will be the parent of the dialog. May be null.
	 * @param title
	 *            the title of the dialog window, or null for default value:<br>
	 *            <code>APP_NAME - Warning</code>
	 * @param message
	 *            the message that will be displayed to the user. Must not be null.
	 * 
	 */
	public static void showWarningDialog(Shell parentShell, String title, String message) {
		boolean dispose = false;

		if (title == null)
			title = Superluminal.APP_NAME + " - Warning";
		if (message == null)
			throw new IllegalArgumentException("Message must not be null.");

		if (parentShell == null) {
			parentShell = new Shell(Display.getCurrent());
			dispose = true;
		}

		MessageBox box = new MessageBox(parentShell, SWT.ICON_WARNING | SWT.OK);

		box.setText(title);
		box.setMessage(message);

		box.open();

		if (dispose)
			parentShell.dispose();
	}

	/**
	 * Displays a simple dialog to inform the user about something.
	 * 
	 * @param parentShell
	 *            a shell which will be the parent of the dialog. May be null.
	 * @param title
	 *            the title of the dialog window, or null for default value:<br>
	 *            <code>APP_NAME - Information</code>
	 * @param message
	 *            the message that will be displayed to the user. Must not be null.
	 * 
	 */
	public static void showInfoDialog(Shell parentShell, String title, String message) {
		boolean dispose = false;

		if (title == null)
			title = Superluminal.APP_NAME + " - Information";
		if (message == null)
			throw new IllegalArgumentException("Message must not be null.");

		if (parentShell == null) {
			parentShell = new Shell(Display.getCurrent());
			dispose = true;
		}

		MessageBox box = new MessageBox(parentShell, SWT.ICON_INFORMATION | SWT.OK);

		box.setText(title);
		box.setMessage(message);

		box.open();

		if (dispose)
			parentShell.dispose();
	}

	public static File promptForDirectory(Shell parentShell, String title, String message) {
		File result = null;
		DirectoryDialog dialog = new DirectoryDialog(parentShell);
		dialog.setText(title);
		dialog.setMessage(message);

		String path = dialog.open();
		if (path == null) {
			// User aborted selection
			// Nothing to do here
		} else {
			result = new File(path);
		}

		return result;
	}

	public static File promptForSaveFile(Shell parentShell, String title, String[] extensions) {
		File result = null;
		FileDialog dialog = new FileDialog(parentShell, SWT.SAVE);
		dialog.setFilterExtensions(extensions);
		dialog.setText(title);
		dialog.setOverwrite(true);

		String path = dialog.open();
		if (path == null) {
			// User aborted selection
			// Nothing to do here
		} else {
			result = new File(path);
		}

		return result;
	}

	public static File promptForLoadFile(Shell parentShell, String title, String[] extensions) {
		File result = null;
		FileDialog dialog = new FileDialog(parentShell, SWT.OPEN);
		dialog.setFilterExtensions(extensions);
		dialog.setText(title);

		String path = dialog.open();
		if (path == null) {
			// User aborted selection
			// Nothing to do here
		} else {
			result = new File(path);
		}

		return result;
	}

	/**
	 * Modally prompts the user for the FTL resources dir.
	 * 
	 * @param parentShell
	 *            parent for the SWT dialog
	 * 
	 * @author Vhati - original method wth Swing dialogs
	 * @author kartoFlane - modified to work with SWT dialogs
	 */
	public static File promptForDatsDir(Shell parentShell) {
		File result = null;

		String message = "";
		message += "You will now be prompted to locate FTL manually.\n";
		message += "Select '(FTL dir)/resources/data.dat'.\n";
		message += "Or 'FTL.app', if you're on OSX.";

		MessageBox box = new MessageBox(parentShell, SWT.ICON_INFORMATION | SWT.OK);
		box.setText("Find FTL");
		box.setMessage(message);

		FileDialog fd = new FileDialog(parentShell, SWT.OPEN);
		fd.setText("Find data.dat or FTL.app");
		fd.setFilterExtensions(new String[] { "*.dat", "*.app" });
		fd.setFilterNames(new String[] { "FTL Data File - (FTL dir)/resources/data.dat", "FTL Application Bundle" });

		String filePath = fd.open();

		if (filePath == null) {
			// User aborted selection
			// Nothing to do here
		} else {
			File f = new File(filePath);
			if (f.getName().equals("data.dat")) {
				result = f.getParentFile();
			} else if (f.getName().endsWith(".app")) {
				File contentsPath = new File(f, "Contents");
				if (contentsPath.exists() && contentsPath.isDirectory() && new File(contentsPath, "Resources").exists())
					result = new File(contentsPath, "Resources");
				// TODO test whether this works on OSX
			}
		}

		if (result != null && FTLUtilities.isDatsDirValid(result)) {
			return result;
		}

		return null;
	}

	/**
	 * Displays a simple dialog with an indeterminate progress bar in the UI thread, while
	 * executing the given task in another thread, and waiting for it to finish.<br>
	 * Usage:
	 * 
	 * <pre>
	 * showLoadDialog(shell, title, message, new LoadTask() {
	 * 	public void execute() {
	 * 		// your code here...
	 * 	}
	 * });
	 * </pre>
	 * 
	 * @param parentShell
	 *            a shell which will be the parent of the dialog. Must not be null.
	 * @param title
	 *            the title of the dialog window, or null for default value:<br>
	 *            <code>APP_NAME - Loading...</code>
	 * @param message
	 *            a brief message that will be displayed above the progress bar, or null for default value:<br>
	 *            <code>Loading, please wait...</code>
	 * @param task
	 *            the task that is to be performed in the background, or null to make the method return immediately.
	 * 
	 */
	public static void showLoadDialog(Shell parentShell, String title, String message, final LoadTask task) {
		if (task == null)
			return;

		if (parentShell == null)
			throw new IllegalArgumentException("Parent shell must not be null.");

		final LoadingDialog dialog = new LoadingDialog(parentShell, title, message);
		Thread loadThread = new Thread() {
			@Override
			public void run() {
				try {
					task.execute();
				} finally {
					dialog.dispose();
				}
			}
		};
		loadThread.start();
		dialog.open();
	}

	public interface LoadTask {
		public void execute();
	}
}
