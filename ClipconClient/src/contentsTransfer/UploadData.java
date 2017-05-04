package contentsTransfer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class UploadData {
	// public final static String SERVER_URL = "http://182.172.16.118:8080/websocketServerModule";
	// public final static String SERVER_URL = "http://223.194.157.244:8080/websocketServerModule";
	public final static String SERVER_URL = "http://223.194.152.19:8080/websocketServerModule";
	public final static String SERVER_SERVLET = "/UploadServlet";
	private String charset = "UTF-8";

	private String userName = null;
	private String groupPK = null;
	private int startIndex = 0;

	/** 생성자 userName과 groupPK를 설정한다. */
	public UploadData(String userName, String groupPK) {
		this.userName = userName;
		this.groupPK = groupPK;
	}

	/** String Data를 업로드 */
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

	/** Clipboard에 있는 Captured Image Data를 업로드 */
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

	/** 여러 File Data를 업로드
	 * 
	 * @param dir 업로드할 파일의 위치
	 * @param dir 업로드할 파일명
	 */
	public void uploadMultipartData(ArrayList<String> fileFullPathList) {
		try {
			MultipartUtility multipart = new MultipartUtility(SERVER_URL + SERVER_SERVLET, charset);
			setCommonParameter(multipart);
			
			// 업로드할 파일 생성
			File firstUploadFile = new File(fileFullPathList.get(0));
			
			/* case: 전송할 파일이 1개인 경우(폴더가 아닌 경우) createFolder = FALSE */
			if (fileFullPathList.size() == 1 && firstUploadFile.isFile()) {
				System.out.println("\n전송할 파일이 하나요~~\n");
				multipart.addFormField("createFolder", "FALSE");
				multipart.addFilePart("multipartFileData", firstUploadFile, "/");
			}
			/* case: 전송할 파일이 2개 이상, 폴더가 하나 이상인 경우 createFolder = TRUE */
			else{
				System.out.println("\n전송할 파일이 여러개요~~\n");
				multipart.addFormField("createFolder", "TRUE");
				// Iterator 통한 전체 조회
				Iterator iterator = fileFullPathList.iterator();

				// 여러 파일을 순서대로 처리
				while (iterator.hasNext()) {
					String fileFullPath = (String) iterator.next();
					
					// 업로드할 파일 생성
					File uploadFile = new File(fileFullPath);

					System.out.println("<<fileFullPathList>>: "+ fileFullPath);

					/* case: File */
					if(uploadFile.isFile()){
						System.out.println("전송할 파일이 File이요~~");
						multipart.addFilePart("multipartFileData", uploadFile, "/");
					}
					/* case: Directory */
					else if(uploadFile.isDirectory()){
						System.out.println("전송할 파일이 Directory요~~");
						
						// 상대경로명을 위한 초기값(처음 root dir의 시작 위치 설정)
						startIndex = uploadFile.getPath().lastIndexOf(uploadFile.getName());
						
						multipart.addFormField("directoryData", uploadFile.getPath().substring(startIndex));
						System.out.println("디렉토리 이름 = " + uploadFile.getName() + ", 상대 경로: " + uploadFile.getPath().substring(startIndex));
						
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

	/** File Data의 구조에 따라 <상대경로명, 파일명> 설정 
	 * directory이면 addFormField로 상대경로 정보 보내기 
	 * file이면 addFilePart로 파일과 상대경로 정보 보내기*/
	public void subDirList(File uploadFile, MultipartUtility multipart) {
		File[] fileList = uploadFile.listFiles(); //directory 안의 file data list

		for (int i = 0; i < fileList.length; i++) {
			File file = fileList[i];
			try {
				/* case: 업로드할 파일 내부에 또 다른 파일이 있는 경우 */
				if (file.isFile()) {
					multipart.addFilePart("multipartFileData", file, getFileRelativePath(file));
					System.out.println("파일 이름 = " + file.getName() + ", 상대 경로: " + getFileRelativePath(file));
				} 
				/* case: 업로드할 파일 내부에 서브디렉토리가 존재하는 경우 다시 탐색 */
				else if (file.isDirectory()) {
					multipart.addFormField("directoryData", file.getPath().substring(startIndex));
					// subDirList(file.getCanonicalPath().toString());
					subDirList(file, multipart);
					System.out.println("디렉토리 이름 = " + file.getName() + ", 상대 경로: " + file.getPath().substring(startIndex));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** relative path 얻어오기 */
	public String getFileRelativePath(File file){
		String filePath = file.getPath();
		String fileName = file.getName();
		int endIndex = filePath.lastIndexOf(fileName);
		
		// 파일명을 제외한 상대경로 정보
		return filePath.substring(startIndex, endIndex-1); 
	}
	
	/** 모든 Data에서 공통으로 설정해야하는 Parameter
	 * userName, groupPK, uploadTime */
	public void setCommonParameter(MultipartUtility multipart) {
		multipart.addHeaderField("User-Agent", "Heeee");
		multipart.addFormField("userName", userName);
		multipart.addFormField("groupPK", groupPK);
		multipart.addFormField("uploadTime", uploadTime());
	}

	/** @return YYYY-MM-DD HH:MM:SS 형식의 현재 시간 */
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
