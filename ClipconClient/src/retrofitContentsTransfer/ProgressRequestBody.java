package retrofitContentsTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Handler;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import userInterface.UserInterface;

public class ProgressRequestBody extends RequestBody {
	private File mFile;
	private String mPath;
//	private UploadCallbacks mListener;

	private static final int DEFAULT_BUFFER_SIZE = 2048;
	
	private UserInterface ui = UserInterface.getInstance();

//	public interface UploadCallbacks {
//		void onProgressUpdate(int percentage);
//		void onError();
//		void onFinish();
//	}
//
//	public ProgressRequestBody(final File file, final UploadCallbacks listener) {
//		mFile = file;
//		mListener = listener;
//	}
	
	public ProgressRequestBody(final File file) {
		mFile = file;
	}

	@Override
	public MediaType contentType() {
		// i want to upload only images
		return MediaType.parse("image/*");
	}

	@Override
	public long contentLength() throws IOException {
		return mFile.length();
	}

	@Override
	public void writeTo(BufferedSink sink) throws IOException {
		long fileLength = mFile.length();
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		FileInputStream in = new FileInputStream(mFile);
		long uploaded = 0;

		
		System.out.println("[ProgressRequestBody] writeTo");
		try {
			int read;
			// Handler handler = new Handler(Looper.getMainLooper());
			while ((read = in.read(buffer)) != -1) {

				// update progress on UI thread
				// handler.post(new ProgressUpdater(uploaded, fileLength));
				double progressValue = (100 * uploaded / fileLength);
				//System.out.println((int) progressValue);
				ui.getProgressBarScene().setProgeress(progressValue, uploaded, fileLength);

				uploaded += read;
				sink.write(buffer, 0, read);
			}
		} finally {
			in.close();
		}
	}

	// private class ProgressUpdater implements Runnable {
	// private long mUploaded;
	// private long mTotal;
	//
	// public ProgressUpdater(long uploaded, long total) {
	// mUploaded = uploaded;
	// mTotal = total;
	// }
	//
	// @Override
	// public void run() {
	// mListener.onProgressUpdate((int) (100 * mUploaded / mTotal));
	// }
	// }
}
