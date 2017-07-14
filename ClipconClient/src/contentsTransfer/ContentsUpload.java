package contentsTransfer;

import java.awt.Image;
import java.util.ArrayList;

import controller.ClipboardController;
import controller.Endpoint;
import retrofitContentsTransfer.RetrofitUploadTest;

public class ContentsUpload {
	public static UploadData uploader;
	public static RetrofitUploadTest uploader2;
	public static boolean isUpload = false;

	public void upload() {
		isUpload = true;
		uploader = new UploadData(Endpoint.user.getName(), Endpoint.user.getGroup().getPrimaryKey());
		uploader2 = new RetrofitUploadTest(Endpoint.user.getName(), Endpoint.user.getGroup().getPrimaryKey());

		Object clipboardData = ClipboardController.readClipboard();

		if (clipboardData instanceof String) {
			uploader.uploadStringData((String) clipboardData);
		} else if (clipboardData instanceof Image) {
			uploader.uploadCapturedImageData((Image) clipboardData);
		} else if (clipboardData instanceof ArrayList<?>) {
			// uploader.uploadMultipartData((ArrayList<String>) clipboardData);
			System.out.println("uploader2");
			uploader2.uploadFile((ArrayList<String>) clipboardData);

		}
	}
}