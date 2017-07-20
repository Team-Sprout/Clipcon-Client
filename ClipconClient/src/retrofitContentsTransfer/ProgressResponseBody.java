//package retrofitContentsTransfer;
//
//import java.io.IOException;
//
//import okhttp3.MediaType;
//import okhttp3.ResponseBody;
//import okio.Buffer;
//import okio.BufferedSource;
//import okio.ForwardingSource;
//import okio.Okio;
//import okio.Source;
//
//public class ProgressResponseBody extends ResponseBody {
//	private final ResponseBody responseBody;
//	private final ProgressListener progressListener;
//	private BufferedSource bufferedSource;
//
//	public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
//		this.responseBody = responseBody;
//		this.progressListener = progressListener;
//	}
//
//	// /** Constructor */
//	// public ProgressResponseBody(ResponseBody responseBody) {
//	// this.responseBody = responseBody;
//	// }
//
//	@Override
//	public MediaType contentType() {
//		return responseBody.contentType();
//	}
//
//	@Override
//	public long contentLength() {
//		return responseBody.contentLength();
//	}
//
//	@Override
//	public BufferedSource source() {
//		if (bufferedSource == null) {
//			bufferedSource = Okio.buffer(source(responseBody.source()));
//		}
//		return bufferedSource;
//	}
//
//	private Source source(Source source) {
//		return new ForwardingSource(source) {
//			long totalBytesRead = 0L;
//
//			@Override
//			public long read(Buffer sink, long byteCount) throws IOException {
//
//				// System.out.println("[ProgressResponseBody] response: " + responseBody.string());
//
//				long bytesRead = super.read(sink, byteCount);
//				// read() returns the number of bytes read, or -1 if this source is exhausted.
//				totalBytesRead += bytesRead != -1 ? bytesRead : 0;
//
//				// update(totalBytesRead, contentLength(), bytesRead == -1);
//				// update(totalBytesRead, 146876224, bytesRead == -1);
//
//				if (progressListener != null) {
//					// progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
//					progressListener.update(totalBytesRead, 146876224, bytesRead == -1);
//				}
//
//				return bytesRead;
//			}
//		};
//	}
//
//	// public void update(long bytesRead, long contentLength, boolean done) {
//	// System.out.println("bytesRead: " + bytesRead);
//	// System.out.println("contentLength: " + contentLength);
//	// System.out.println("done: " + done);
//	// int percentage = (int) ((100 * bytesRead) / contentLength);
//	// System.out.format("%d%% done\n", percentage);
//	//
//	// if (!done) {
//	// /* progressUpdate */
//	//
//	// // mNotification.progressUpdate(percentage, contentLength, (int) bytesRead, "");
//	// // mNotification.updateNotification(notificationId);
//	// } else {
//	// /* complete */
//	//
//	// // mNotification.setCompletion();
//	// // mNotification.completed(notificationId);
//	// }
//	//
//	// }
//
//	public interface ProgressListener {
//		void update(long bytesRead, long contentLength, boolean done);
//	}
//}
