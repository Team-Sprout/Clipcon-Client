package contentsTransfer;

import java.awt.Image;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;

import controller.ClipboardController;
import controller.Endpoint;
import model.Contents;
import model.FileTransferable;
import model.History;
import model.ImageTransferable;

public class DownloadData {
	// 다운로드 파일을 임시로 저장할 위치
	private final String DOWNLOAD_LOCATION = "C:\\Program Files\\Clipcon";

	public final static String SERVER_URL = "http://delf.gonetis.com:8080:/websocketServerModule";
	//public final static String SERVER_URL = "http://223.194.152.19:8080/websocketServerModule"; // delf's

	public final static String SERVER_SERVLET = "/DownloadServlet";

	private final String charset = "UTF-8";
	private HttpURLConnection httpConn;

	private String userName = null;
	private String groupPK = null;

	private Contents requestContents; // Contents Info to download
	// private String downloadDataPK; // Contents' Primary Key to download
	// private History myhistory; // The Group History to which I belong
	private Map<String, String[]> requestAgainOfFileData = new HashMap<String, String[]>();

	/** 생성자 userName과 groupPK를 설정한다. */
	public DownloadData(String userName, String groupPK) {
		this.userName = userName;
		this.groupPK = groupPK;
	}

	/**
	 * 다운로드하기 원하는 Data를 request 복수 선택은 File Data의 경우만 가능(추후 개선)
	 * 
	 * @param downloadDataPK
	 *            다운로드할 Data의 고유키
	 * @param myhistory
	 *            내가 속한 그룹의 History 정보
	 */
	public void requestDataDownload(String downloadDataPK) throws MalformedURLException {
		
		//나의 히스토리 가져오기. 다른 방법 생각s.
		History myhistory = Endpoint.user.getGroup().getHistory();
		
		// Create a temporary folder to save the imageFile, file
		createFolder(DOWNLOAD_LOCATION);
		// Retrieving Contents from My History
		requestContents = myhistory.getContentsByPK(downloadDataPK);
		// Type of data to download
		String contentsType = requestContents.getContentsType();
		
		// Parameter to be sent by the GET method
		String parameters = "userName=" + userName + "&" + "groupPK=" + groupPK + "&" + "downloadDataPK="
				+ downloadDataPK;

		try {
			URL url = new URL(SERVER_URL + SERVER_SERVLET + "?" + parameters);

			httpConn = (HttpURLConnection) url.openConnection();

			httpConn.setRequestMethod("GET");
			httpConn.setUseCaches(false);
			httpConn.setDoOutput(false); // indicates GET method
			httpConn.setDoInput(true);

			// checks server's status code first
			int status = httpConn.getResponseCode();
			List<String> response = new ArrayList<String>(); // Server의 응답내용

			if (status == HttpURLConnection.HTTP_OK) {
				switch (contentsType) {
				case Contents.TYPE_STRING:
					// response body에 넣은 String 객체를 받아온다.
					String stringData = downloadStringData(httpConn.getInputStream());
					System.out.println("stringData 결과: " + stringData);
					
					StringSelection stringTransferable = new StringSelection(stringData);
					ClipboardController.writeClipboard(stringTransferable);
					
				case Contents.TYPE_IMAGE:
					// response body에 넣은 Image 객체를 받아온다.
					Image imageData = downloadCapturedImageData(httpConn.getInputStream());
					System.out.println("ImageData 결과: " + imageData.toString());
					
					ImageTransferable imageTransferable = new ImageTransferable(imageData);
					ClipboardController.writeClipboard(imageTransferable);

					break;
					
				case Contents.TYPE_FILE:
					String fileOriginName = requestContents.getContentsValue();
					/* Clipcon 폴더에 실제 File(파일명: 원본 파일명) 저장 후 File 객체를 받아온다. */
					File fileData = downloadMultipartData(httpConn.getInputStream(), fileOriginName);
					System.out.println("fileOriginName 결과: " + fileData.getName());
					
					ArrayList<File> fileList = new ArrayList<File>();
					fileList.add(fileData);
					FileTransferable fileTransferable = new FileTransferable(fileList);
					ClipboardController.writeClipboard(fileTransferable);

					break;
					
				case Contents.TYPE_MULTIPLE_FILE:
					// 1. server에서 Json형태로 multipleFileInfo에 대한 String을 받아온다.
					// 2. Json형태를 받아 구조에 맞게 dir들을 생성한다.
					// 3. Json에서 file에 해당하는 것을 GET request로 다시 요청한다.
					// (dir가 없으면 여러 file을 받아오는 것으로 처리한다.)
					// response body에 넣은 String 객체를 받아온다.
					
					String multipleFileInfo = downloadStringData(httpConn.getInputStream());
					System.out.println("multipleFileInfo 결과: " + multipleFileInfo);
					
					requestAgainOfFileData = analyzeMultipartDataInfo(multipleFileInfo);

					break;

				default:
					System.out.println("어떤 형식에도 속하지 않음.");
				}
				System.out.println();

			} else {
				throw new IOException("Server returned non-OK status: " + status);
			}
			httpConn.disconnect();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** String Data를 다운로드 */
	private String downloadStringData(InputStream inputStream) {
		BufferedReader bufferedReader;
		StringBuilder stringBuilder = null;

		try {
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));

			stringBuilder = new StringBuilder();
			String line = null;

			try {
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line + "\n");
				}
				inputStream.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return stringBuilder.toString();
	}

	/**
	 * Captured Image Data를 다운로드 file 형태의 Image Data를 전송받아 Image 객체로 변경
	 */
	private Image downloadCapturedImageData(InputStream inputStream) {
		byte[] imageInByte = null;
		BufferedImage bImageFromConvert = null;

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
			byte[] buffer = new byte[0xFFFF]; // 65536

			for (int len; (len = inputStream.read(buffer)) != -1;)
				byteArrayOutputStream.write(buffer, 0, len);

			byteArrayOutputStream.flush();
			imageInByte = byteArrayOutputStream.toByteArray();

			inputStream.close();

			// convert byte array back to BufferedImage
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageInByte);
			bImageFromConvert = ImageIO.read(byteArrayInputStream);

		} catch (IOException e) {
			e.printStackTrace();
		}
		Image ImageData = (Image) bImageFromConvert;

		return ImageData;
	}

	/** Multiple File Data를 임시폴더에 다운로드 후 File 객체 리턴 */
	private File downloadMultipartData(InputStream inputStream, String fileName) throws FileNotFoundException {
		// opens input stream from the HTTP connection
		// InputStream inputStream = httpConn.getInputStream();
		String saveFileFullPath = DOWNLOAD_LOCATION + File.separator + fileName;
		File fileData;

		try {
			// opens an output stream to save into file
			FileOutputStream fileOutputStream = new FileOutputStream(saveFileFullPath);

			int bytesRead = -1;
			byte[] buffer = new byte[0xFFFF];

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				fileOutputStream.write(buffer, 0, bytesRead);
			}
			fileOutputStream.flush();
			fileOutputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		fileData = new File(saveFileFullPath);
		return fileData;
	}
	
	/** Multiple File Data의 정보를 분석하여 Dir 구조 생성 후 다시 server에 요청할 정보를 return */
	private Map<String, String[]> analyzeMultipartDataInfo(String jsonString){
		Map<String, String[]> multipleFileInfo = new HashMap<String, String[]>(); 
		Map<String, String[]> requestAgainOfFileData = new HashMap<String, String[]>();

        JSONObject jsonObject = new JSONObject(jsonString); // HashMap
        Iterator<?> keyset = jsonObject.keys(); // HM
        String[] value = new String[2];

		/* [희정] Json 구조 확인 후 수정 필요 */
		while (keyset.hasNext()) {
			String key = (String) keyset.next();
			System.out.print("\n Key: " + key);

			JSONArray jsonArray = jsonObject.getJSONArray(key);
			System.out.println(", Value: " + jsonArray.toString());

			for (int i = 0; i < jsonArray.length(); i++) {
				value[i] = (String) jsonArray.get(i);
			}
			System.out.println("value[0]: " + value[0] + ", value[1]: " + value[1]);

			multipleFileInfo.put(key, value);

			// case: directory
			if (value[1].equals(Contents.TYPE_DIRECTORY)) {
				// 적절하게 directory를 생성
				makeDirBasedJsonStruct(value[0]);
			}
			// case: file
			else {
				// 다시 server에 요청할 정보를 저장
				System.out.println("다시 server에 요청할 File 정보 key num: " + key);
				requestAgainOfFileData.put(key, value);
			}
		}
		return requestAgainOfFileData;
	}
	
	/** 구조에 맞게 Directory 생성 */
	private void makeDirBasedJsonStruct(String dirName){
		String dirFullName = DOWNLOAD_LOCATION + File.separator + dirName.replaceAll("\"", File.separator);
		createFolder(dirFullName);
	}
	
	/* 프로그램 실행할 때로 옮겨야 함. */
	/**
	 * Folder 생성 메서드(download한 파일을 저장할 임시 폴더)
	 * 
	 * @param saveFilePath
	 *            이 이름으로 폴더 생성
	 */
	private void createFolder(String folderName) {
		File directory = new File(folderName);

		// 저장할 그룹 폴더가 존재하지 않으면
		if (!directory.exists()) {
			directory.mkdir(); // 폴더 생성
			System.out.println("------------------------------------" + folderName + " 폴더 생성");
		}
	}
}
