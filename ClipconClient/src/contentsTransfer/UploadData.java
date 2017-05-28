package contentsTransfer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.EncodeException;

import application.Main;
import controller.Endpoint;
import model.Message;
import userInterface.MainScene;

public class UploadData {

	public final static String SERVER_URL = "http://" + Main.SERVER_ADDR + ":8080/websocketServerModule";
	public final static String SERVER_SERVLET = "/UploadServlet";

	private String charset = "UTF-8";

	private String userName = null;
	private String groupPK = null;
	
	private Endpoint endpoint = Endpoint.getIntance();
	
	public static String multipartFileSize = "";

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
			MainScene.showProgressBarFlag = true;
			//startTime = System.currentTimeMillis();
			
			MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
			setCommonParameter(multipart);

			multipart.addImagePart("imageData", capturedImageData);

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
	 * @param fileFullPathLis - file path from clipboard
	 */
	public void uploadMultipartData(ArrayList<String> fileFullPathList) {
		try {
			MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
			setCommonParameter(multipart);

			// create uploading file
			File firstUploadFile = new File(fileFullPathList.get(0));

			System.out.println("uploadMultipartData file name :" + firstUploadFile.getName());

			
			// send LOG_UPLOAD_TIME Message to server
			MainScene.showProgressBarFlag = true;
			long startTime = System.currentTimeMillis();
			
			Message uploadInfoMsg = new Message().setType(Message.LOG_UPLOAD_INFO);
			uploadInfoMsg.add(Message.UPLOAD_START_TIME, Long.toString(startTime));
			
			
			/* case: Single file data(not a folder) */
			if (fileFullPathList.size() == 1 && firstUploadFile.isFile()) {
				multipart.addFilePart("fileData", firstUploadFile);
				uploadInfoMsg.add(Message.MULTIPLE_CONTENTS_INFO, "");
			}
			/* case: Multiple file data, One or more folders */
			else {
				try {
					File uploadRootDir = new File(MainScene.UPLOAD_TEMP_DIR_LOCATION);
					String zipFileFillPath = MultipleFileCompress.compress(fileFullPathList);
					
					File uploadZipFile = new File(zipFileFillPath);
					multipart.addFilePart("multipartFileData", uploadZipFile);

					// Delete child files that already exist after uploading
					if (uploadRootDir.listFiles().length != 0) {
						for (int i = 0; i < uploadRootDir.listFiles().length; i++)
							uploadRootDir.listFiles()[i].delete();
					}
					
					uploadInfoMsg.add(Message.MULTIPLE_CONTENTS_INFO, multipartFileSize);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			try {
				endpoint.sendMessage(uploadInfoMsg);
			} catch (IOException | EncodeException e) {
				e.printStackTrace();
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
		multipart.addHeaderField("User-Agent", "pcProgram");
		multipart.addFormField("userName", userName);
		multipart.addFormField("groupPK", groupPK);
	}
}
