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
	private int startIndex = 0;

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

			multipart.addFormField("createFolder", "FALSE");
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

			multipart.addFormField("createFolder", "FALSE");
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
			
			/* case: Single file data(not a folder), createFolder = FALSE */
			if (fileFullPathList.size() == 1 && firstUploadFile.isFile()) {
				System.out.println("\nSingle File Uploading~~\n");
				multipart.addFormField("createFolder", "FALSE");
				multipart.addFilePart("multipartFileData", firstUploadFile, "/");
			}
			/* case: Multiple file data, One or more folders, createFolder = TRUE */
			else{
				System.out.println("\nMultiple File Uploading~~\n");
				multipart.addFormField("createFolder", "TRUE");

				Iterator iterator = fileFullPathList.iterator();

				while (iterator.hasNext()) {
					String fileFullPath = (String) iterator.next();
					
					// create uploading file
					File uploadFile = new File(fileFullPath);

					System.out.println("<<fileFullPathList>>: "+ fileFullPath);

					/* case: File */
					if(uploadFile.isFile()){
						System.out.println("File Data Uploading~~");
						multipart.addFilePart("multipartFileData", uploadFile, "/");
					}
					/* case: Directory */
					else if(uploadFile.isDirectory()){
						System.out.println("Directory Data Uploading~~");
						
						// Initial value for relative path (Set starting index of root directory)
						startIndex = uploadFile.getPath().lastIndexOf(uploadFile.getName());
						
						multipart.addFormField("directoryData", uploadFile.getPath().substring(startIndex));
						System.out.println("Directory name: " + uploadFile.getName() + ", Relative path: " + uploadFile.getPath().substring(startIndex));
						
						subDirList(uploadFile, multipart);
					}
					System.out.println();
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

	/** Setting <Relative path, File name> depending on the structure of the File Data 
	 * case directory: send relative path info using addFormField 
	 * case File: send real file data and relative path info using addFilePart */
	public void subDirList(File uploadFile, MultipartUtility multipart) {
		File[] fileList = uploadFile.listFiles(); // file data list in directory

		for (int i = 0; i < fileList.length; i++) {
			File file = fileList[i];
			try {
				/* case: There is another file inside the file to upload */
				if (file.isFile()) {
					multipart.addFilePart("multipartFileData", file, getFileRelativePath(file));
					System.out.println("File name: " + file.getName() + ", Relative path: " + getFileRelativePath(file));
				} 
				/* case: There is a subdirectory inside the file to upload, Rediscover */
				else if (file.isDirectory()) {
					multipart.addFormField("directoryData", file.getPath().substring(startIndex));
					// subDirList(file.getCanonicalPath().toString());
					subDirList(file, multipart);
					System.out.println("Directory name: " + file.getName() + ", Relative path: " + file.getPath().substring(startIndex));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** Get relative path info */
	public String getFileRelativePath(File file){
		String filePath = file.getPath();
		String fileName = file.getName();
		int endIndex = filePath.lastIndexOf(fileName);
		
		// relative path info except file name
		return filePath.substring(startIndex, endIndex-1); 
	}
	
	/** Parameter to be set in common for all data
	 * userName, groupPK, uploadTime */
	public void setCommonParameter(MultipartUtility multipart) {
		multipart.addHeaderField("User-Agent", "Heeee");
		multipart.addFormField("userName", userName);
		multipart.addFormField("groupPK", groupPK);
		multipart.addFormField("uploadTime", uploadTime());
	}

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
