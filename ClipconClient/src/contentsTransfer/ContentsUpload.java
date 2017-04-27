package contentsTransfer;

import java.util.ArrayList;
import java.util.List;

import controller.Endpoint;

public class ContentsUpload {
	public static UploadData uploader;
	
	//testagain

	public void upload() {
		uploader = new UploadData("delf", Endpoint.user.getGroup().getPrimaryKey());

		List<String> clipboardData = new ArrayList<String>();
		clipboardData.add("C:\\Users\\delf\\Desktop\\###\\1.zip");
		uploader.uploadMultipartData((ArrayList<String>) clipboardData);
	}
}