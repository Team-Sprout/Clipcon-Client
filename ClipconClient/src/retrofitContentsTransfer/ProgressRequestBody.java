package retrofitContentsTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import lombok.NoArgsConstructor;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import userInterface.ProgressBarScene;
import userInterface.UserInterface;

@NoArgsConstructor
public class ProgressRequestBody extends RequestBody {
	@Setter
	private File mFile;
	private final int CHUNKSIZE = 0xFFFF; // 65536

	private UserInterface ui = UserInterface.getInstance();

	@Override
	public MediaType contentType() {
		// The default content type for each part is 'text / plain' -> error
		// return MediaType.parse("multipart/mixed");

		return MediaType.parse("multipart/form-data");
		// return MediaType.parse("application/octet-stream");
	}

	@Override
	public long contentLength() throws IOException {
		return mFile.length();
	}

	@Override
	public void writeTo(BufferedSink sink) throws IOException {
		int progressBarIndex = ProgressBarScene.getIndex();
		long fileLength = mFile.length();
		byte[] buffer = new byte[CHUNKSIZE];
		FileInputStream in = new FileInputStream(mFile);
		long uploaded = 0;
		
		while(progressBarIndex == -1) {
			progressBarIndex = ProgressBarScene.getIndex();
			System.out.println("while 문 안 : " + progressBarIndex);
		}

		try {
			int read;

			/* update progress on UI thread */
			while ((read = in.read(buffer)) != -1) {
				// handler.post(new ProgressUpdater(uploaded, fileLength));

				double progressValue = (100 * uploaded / fileLength);
				// System.out.println((int) progressValue);
				System.out.println("writeTo index : " + progressBarIndex);
				if(fileLength < CHUNKSIZE) {
					ui.getProgressBarScene().setIndeterminateProgeress(progressBarIndex, false);
				}
				else {
					ui.getProgressBarScene().setProgeress(progressBarIndex, progressValue, uploaded, fileLength, false);
				}

				uploaded += read;
				sink.write(buffer, 0, read);
			}
		} finally {
			in.close();
			if(fileLength < CHUNKSIZE) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			ui.getProgressBarScene().completeProgress(progressBarIndex);
			ui.getMainScene().closeProgressBarStage(progressBarIndex);
		}
	}

}
