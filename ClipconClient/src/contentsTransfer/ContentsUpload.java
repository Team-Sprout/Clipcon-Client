package contentsTransfer;

import java.util.ArrayList;
import java.util.List;

import controller.Endpoint;

public class ContentsUpload {
	public static UploadData uploader;
	
	// test git commit and push in branch
	public String heejeongTest; 

	public void upload() {
		uploader = new UploadData("delf", Endpoint.user.getGroup().getPrimaryKey());

		List<String> clipboardData = new ArrayList<String>();
		clipboardData.add("C:\\Users\\delf\\Desktop\\###\\1.zip");
		uploader.uploadMultipartData((ArrayList<String>) clipboardData);
	}
}