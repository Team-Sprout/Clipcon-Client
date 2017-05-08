package contentsTransfer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class UploadData {

	public final static String SERVER_URL = "http://delf.gonetis.com:8080/websocketServerModule";
	//public final static String SERVER_URL = "http://223.194.152.19:8080/websocketServerModule";

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
			
			/* case: Single file data(not a folder) */
			if (fileFullPathList.size() == 1 && firstUploadFile.isFile()) {
				System.out.println("\nSingle File Uploading~~\n");
				multipart.addFilePart("fileData", firstUploadFile);
			}
			/* case: Multiple file data, One or more folders */
			else{
				System.out.println("\nMultiple File or Directory Uploading~~\n");
				
				// TODO [희정] Compress multiple file and directory
				// 해당 파일들 or folder들을 압축한다.
				// 압축한 .zip 파일의 full path를 받아온다.
				
				String zipFileFullPath = "";
				
				// create uploading file
				File uploadZipFile = new File(zipFileFullPath);

				System.out.println("<<zipFileFullPath>>: "+ zipFileFullPath);
				multipart.addFilePart("multipartFileData", uploadZipFile);
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
		multipart.addFormField("uploadTime", uploadTime());
	}

	// TODO [희정] deal with uploadTime at Server Side 
	/** @return Current Time YYYY-MM-DD HH:MM:SS  */
	public String uploadTime() {
		Calendar cal = Calendar.getInstance();
		String year = Integer.toString(cal.get(Calendar.YEAR));
		String month = Integer.toString(cal.get(Calendar.MONTH) + 1);

		String date = Integer.toString(cal.get(Calendar.DATE));
		String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
		if (Integer.parseInt(hour) < 10) {
			hour = "0" + hour;
		}
		if (Integer.parseInt(hour) > 12) {
			hour = "PM " + Integer.toString(Integer.parseInt(hour) - 12);
		} else {
			hour = "AM " + hour;
		}

		String minute = Integer.toString(cal.get(Calendar.MINUTE));
		if (Integer.parseInt(minute) < 10) {
			minute = "0" + minute;
		}
		String sec = Integer.toString(cal.get(Calendar.SECOND));
		if (Integer.parseInt(sec) < 10) {
			sec = "0" + sec;
		}

		return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + sec;
	}
}
