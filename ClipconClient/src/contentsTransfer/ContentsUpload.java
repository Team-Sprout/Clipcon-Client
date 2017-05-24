package contentsTransfer;

import java.awt.Image;
import java.util.ArrayList;

import controller.ClipboardController;
import controller.Endpoint;

public class ContentsUpload {
	 public static UploadData uploader;
	 
	 public void upload() {
		 uploader = new UploadData(Endpoint.user.getName(), Endpoint.user.getGroup().getPrimaryKey());
		 
		 Object clipboardData = ClipboardController.readClipboard();
		 
		 if (clipboardData instanceof String) {
			 System.out.println("instanceof String");
			 uploader.uploadStringData((String) clipboardData);
	     } 
		 else if (clipboardData instanceof Image) {
			 System.out.println("instanceof Image");
			 uploader.uploadCapturedImageData((Image) clipboardData);
	     }
		 else if (clipboardData instanceof ArrayList<?>) {
			 System.out.println("instanceof ArrayList");
			 uploader.uploadMultipartData((ArrayList<String>) clipboardData);
		 }
	 }
}