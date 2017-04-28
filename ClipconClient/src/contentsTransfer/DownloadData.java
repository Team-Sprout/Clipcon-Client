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
import java.util.List;

import javax.imageio.ImageIO;

import controller.ClipboardController;
import model.Contents;
import model.FileTransferable;
import model.History;
import model.ImageTransferable;

public class DownloadData {
	// 다운로드 파일을 임시로 저장할 위치
	private final String DOWNLOAD_LOCATION = "C:\\Users\\Administrator\\Desktop\\Clipcon";

	//public final static String SERVER_URL = "http://182.172.16.118:8080/websocketServerModule";
	public final static String SERVER_URL = "http://223.194.157.244:8080/websocketServerModule";
	public final static String SERVER_SERVLET = "/DownloadServlet";

	private final String charset = "UTF-8";
	private HttpURLConnection httpConn;

	private String userEmail = null;
	private String groupPK = null;

	private Contents requestContents; // Contents Info to download
	// private String downloadDataPK; // Contents' Primary Key to download
	// private History myhistory; // The Group History to which I belong

	/** 생성자 userEmail과 groupPK를 설정한다. */
	public DownloadData(String userEmail, String groupPK) {
		this.userEmail = userEmail;
		this.groupPK = groupPK;
	}

	/** 다운로드하기 원하는 Data를 request 
	 * 복수 선택은 File Data의 경우만 가능(추후 개선) 
	 * @param downloadDataPK 다운로드할 Data의 고유키 
	 * @param myhistory 내가 속한 그룹의 History 정보 */
	public void requestDataDownload(String downloadDataPK, History myhistory) throws MalformedURLException {
		// Create a temporary folder to save the imageFile, file
		createFileReceiveFolder(DOWNLOAD_LOCATION);
		// Retrieving Contents from My History
		requestContents = myhistory.getContentsByPK(downloadDataPK);

		// Parameter to be sent by the GET method
		String parameters = "userEmail=" + userEmail + "&" + "groupPK=" + groupPK + "&" + "downloadDataPK=" + downloadDataPK;
		// Type of data to download
		String contentsType = requestContents.getContentsType();

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
				case "STRING":
					// response body에 넣은 String 객체를 받아온다.
					String stringData = downloadStringData(httpConn.getInputStream());
					System.out.println("stringData 결과: " + stringData);
					StringSelection stringTransferable = new StringSelection(stringData);
					ClipboardController.writeClipboard(stringTransferable);

					break;
				case "IMAGE":
					// response body에 넣은 Image 객체를 받아온다.
					Image imageData = downloadCapturedImageData(httpConn.getInputStream());
					System.out.println("ImageData 결과: " + imageData.toString());
					ImageTransferable imageTransferable = new ImageTransferable(imageData);
					ClipboardController.writeClipboard(imageTransferable);
					
					break;
				case "FILE":
					String fileOriginName = requestContents.getContentsValue();
					/* Clipcon 폴더에 실제 File(파일명: 원본 파일명) 저장 후 File 객체를 받아온다. */
					File fileData = downloadMultipartData(httpConn.getInputStream(), fileOriginName);
					System.out.println("fileOriginName 결과: " + fileData.getName());
					ArrayList<File> fileList = new ArrayList<File>();
					fileList.add(fileData);
					FileTransferable fileTransferable = new FileTransferable(fileList);
					ClipboardController.writeClipboard(fileTransferable);

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

	/** Captured Image Data를 다운로드 
	 * file 형태의 Image Data를 전송받아 Image 객체로 변경 */
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

	/** 여러 File Data를 임시폴더에 다운로드 후 File 객체 리턴 */
	private File downloadMultipartData(InputStream inputStream, String fileName) throws FileNotFoundException {
		// opens input stream from the HTTP connection
		// InputStream inputStream = httpConn.getInputStream();
		String saveFileFullPath = DOWNLOAD_LOCATION + "\\" + fileName;
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

	/* 프로그램 실행할 때로 옮겨야 함. */
	/** 다운로드한 파일을 저장할 임시 폴더 생성 */
	private void createFileReceiveFolder(String saveFilePath) {
		// 다운로드한 파일을 저장할 폴더
		File downFolder;

		downFolder = new File(saveFilePath);

		// 폴더가 존재하지 않으면
		if (!downFolder.exists()) {
			downFolder.mkdir(); // 폴더 생성
			System.out.println("------------------" + saveFilePath + " 폴더 생성");
		}
	}
}
