package transfer;

import java.awt.Image;
import java.util.ArrayList;

import clipboardManager.ClipboardController;
import server.Endpoint;

public class ContentsUpload {
	public static RetrofitUploadData uploader;

	public void upload() {
		// Endpoint.user.getGroup().getPrimaryKey());
		uploader = new RetrofitUploadData(Endpoint.user.getName(), Endpoint.user.getGroup().getPrimaryKey());

		Object clipboardData = ClipboardController.readClipboard();

		if (clipboardData instanceof String) {
			uploader.uploadStringData((String) clipboardData);
		} else if (clipboardData instanceof Image) {
			uploader.uploadCapturedImageData((Image) clipboardData);
		} else if (clipboardData instanceof ArrayList<?>) {
			uploader.uploadMultipartData((ArrayList<String>) clipboardData);
		}
	}
}