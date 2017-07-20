package retrofitContentsTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import lombok.NoArgsConstructor;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import userInterface.UserInterface;

@NoArgsConstructor
public class ProgressRequestBody extends RequestBody {
	@Setter
	private File mFile;

	private final int CHUNKSIZE = 0xFFFF; // 65536

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
		// [TODO] change -- i want to upload only images
		return MediaType.parse("image/*");
	}

	@Override
	public long contentLength() throws IOException {
		return mFile.length();
	}

	@Override
	public void writeTo(BufferedSink sink) throws IOException {
		long fileLength = mFile.length();
		byte[] buffer = new byte[CHUNKSIZE];
		FileInputStream in = new FileInputStream(mFile);
		long uploaded = 0;

		try {
			int read;

			// [TODO] doy_ Apply to ui
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

}
