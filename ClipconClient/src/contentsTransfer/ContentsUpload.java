package contentsTransfer;

import java.awt.Image;
import java.util.ArrayList;

import controller.ClipboardController;
import controller.Endpoint;
import retrofitContentsTransfer.RetrofitUploadTest;

public class ContentsUpload {
	public static UploadData uploader;
	public static RetrofitUploadTest uploader2;

	public void upload() {
		// uploader = new UploadData(Endpoint.user.getName(), Endpoint.user.getGroup().getPrimaryKey());
		uploader2 = new RetrofitUploadTest(Endpoint.user.getName(), Endpoint.user.getGroup().getPrimaryKey());

		Object clipboardData = ClipboardController.readClipboard();

		if (clipboardData instanceof String) {
			// uploader.uploadStringData((String) clipboardData);
			uploader2.uploadStringData((String) clipboardData);
		} else if (clipboardData instanceof Image) {
			// uploader.uploadCapturedImageData((Image) clipboardData);
		} else if (clipboardData instanceof ArrayList<?>) {
			// uploader.uploadMultipartData((ArrayList<String>) clipboardData);
			uploader2.uploadMultipartData((ArrayList<String>) clipboardData);

		}
	}
}