package contentsTransfer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import userInterface.MainScene;

public class UploadData {

	public final static String SERVER_URL = "http://delf.gonetis.com:8080/websocketServerModule";
//	public final static String SERVER_URL = "http://114.203.39.200:8080/websocketServerModule";

	public final static String SERVER_SERVLET = "/UploadServlet";
	private String charset = "UTF-8";

	private String userName = null;
	private String groupPK = null;

	/** Constructor
	 * setting userName and groupPK. */
	public UploadData(String userName, String groupPK) {
		this.userName = userName;
		this.groupPK = groupPK;
	}

	/** Upload String Data */
	public void uploadStringData(String stringData) {
		try {
			MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
			setCommonParameter(multipart);

			multipart.addFormField("stringData", stringData);
			System.out.println("stringData: " + stringData);

			List<String> response = multipart.finish();

			for (String line : response) {
				System.out.println(line);
			}
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
			System.out.println("imageData: " + capturedImageData.toString());

			List<String> response = multipart.finish();

			for (String line : response) {
				System.out.println(line);
			}

		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	/** Upload File Data
	 * 
	 * @param fileFullPathList	 file path from clipboard
	 */
	public void uploadMultipartData(ArrayList<String> fileFullPathList) {
		try {
			MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
			setCommonParameter(multipart);

			// create uploading file
			File firstUploadFile = new File(fileFullPathList.get(0));
			
			System.out.println("uploadMultipartData file name :" + firstUploadFile.getName());

			/* case: Single file data(not a folder) */
			if (fileFullPathList.size() == 1 && firstUploadFile.isFile()) {
				System.out.println("\nSingle File Uploading~~\n");
				multipart.addFilePart("fileData", firstUploadFile);
			}
			/* case: Multiple file data, One or more folders */
			else {
				System.out.println("\nMultiple File or Directory Uploading~~\n");
				try {
					File uploadRootDir = new File(MainScene.UPLOAD_TEMP_DIR_LOCATION);
					String zipFileFillPath = MultipleFileCompress.compress(fileFullPathList);
					System.out.println("----------------------<<zipFileFullPath>>: " + zipFileFillPath);
					File uploadZipFile = new File(zipFileFillPath);

					multipart.addFilePart("multipartFileData", uploadZipFile);

					// After Upload 이미 존재하는 ZIP file 삭제
					if (uploadRootDir.listFiles().length != 0) {
						for (int i = 0; i < uploadRootDir.listFiles().length; i++)
							uploadRootDir.listFiles()[i].delete();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			List<String> response = multipart.finish();

			for (String line : response) {
				System.out.println(line);
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	/** Parameter to be set in common for all data
	 * userName, groupPK, uploadTime */
	public void setCommonParameter(MultipartUtility multipart) {
		multipart.addHeaderField("User-Agent", "Heeee");
		multipart.addFormField("userName", userName);
		multipart.addFormField("groupPK", groupPK);
	}
}
