package contentsTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Executes the file upload in a background thread and updates progress to
 * listeners that implement the java.beans.PropertyChangeListener interface.
 * @author www.codejava.net
 *
 */

public class UploadTask1 extends SwingWorker<Void, Integer> {
	private String uploadURL;
	private File uploadFile;

	public UploadTask1(String uploadURL, File uploadFile) {
		this.uploadURL = uploadURL;
		this.uploadFile = uploadFile;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			MultipartUtility util = new MultipartUtility(uploadURL, "UTF-8");
			util.addFilePart("uploadFile", uploadFile);

			FileInputStream inputStream = new FileInputStream(uploadFile);
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			long totalBytesRead = 0;
			int percentCompleted = 0;
			long fileSize = uploadFile.length();

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				util.writeFileBytes(buffer, 0, bytesRead);
				totalBytesRead += bytesRead;
				percentCompleted = (int) (totalBytesRead * 100 / fileSize);
				setProgress(percentCompleted);
			}

			inputStream.close();
			util.finish();
		} catch (IOException ex) {
			System.out.println("progress bar error!!!!");
//			JOptionPane.showMessageDialog(null, "Error uploading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			setProgress(0);
			cancel(true);
		}

		return null;
	}

	@Override
	protected void done() {
		if (!isCancelled()) {
			System.out.println("progress bar done!!!!");
//			JOptionPane.showMessageDialog(null, "File has been uploaded successfully!", "Message", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}