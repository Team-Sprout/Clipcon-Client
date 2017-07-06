package contentsTransfer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import application.Main;
import userInterface.MainScene;
import userInterface.UserInterface;

public class UploadData {

	public final static String SERVER_URL = "http://" + Main.SERVER_ADDR + ":8080/websocketServerModule";
	public final static String SERVER_SERVLET = "/UploadServlet";

	private String charset = "UTF-8";

	private String userName = null;
	private String groupPK = null;
	
	private UserInterface ui = UserInterface.getIntance();
	
	public static String multipleFileListInfo = "";

	/** Constructor
	 * setting userName and groupPK. */
	public UploadData(String userName, String groupPK) {
		this.userName = userName;
		this.groupPK = groupPK;
		
		ui.getMainScene().showProgressBar();
	}

	/** Upload String Data */
	public void uploadStringData(String stringData) {
		try {
			MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
			setCommonParameter(multipart);

			multipart.addFormField("stringData", stringData);
			multipart.finish();
			
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	/** Upload Captured Image Data in Clipboard */
	public void uploadCapturedImageData(Image capturedImageData) {
		try {
			MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
			setCommonParameter(multipart);

			multipart.addImagePart("imageData", capturedImageData);
			multipart.finish();

		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	/** Upload File Data
	 * 
	 * @param fileFullPathLis - file path from clipboard
	 */
	public void uploadMultipartData(ArrayList<String> fileFullPathList) {
		try {
			MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
			setCommonParameter(multipart);

			// create uploading file
			File firstUploadFile = new File(fileFullPathList.get(0));

			/* case: Single file data(not a folder) */
			if (fileFullPathList.size() == 1 && firstUploadFile.isFile()) {
				multipart.addFilePart("fileData", firstUploadFile);
			}
			/* case: Multiple file data, One or more folders */
			else {
				try {
					File uploadRootDir = new File(MainScene.UPLOAD_TEMP_DIR_LOCATION);
					multipleFileListInfo = "";
					String zipFileFillPath = MultipleFileCompress.compress(fileFullPathList);
					multipart.addFormField("multipleFileListInfo", multipleFileListInfo);
					
					File uploadZipFile = new File(zipFileFillPath);
					multipart.addFilePart("multipartFileData", uploadZipFile);

					// Delete child files that already exist after uploading
					if (uploadRootDir.listFiles().length != 0) {
						for (int i = 0; i < uploadRootDir.listFiles().length; i++)
							uploadRootDir.listFiles()[i].delete();
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(!MultipartUtility.outOfMemoryException) {
				multipart.finish();
			}
			
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	/** Parameter to be set in common for all data
	 * userName, groupPK, uploadTime */
	public void setCommonParameter(MultipartUtility multipart) {
		multipart.addHeaderField("User-Agent", "pcProgram");
		multipart.addFormField("userName", userName);
		multipart.addFormField("groupPK", groupPK);
	}
}
